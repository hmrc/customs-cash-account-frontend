/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.{AppConfig, ErrorHandler}
import connectors.CustomsFinancialsApiConnector
import controllers.actions.{EmailAction, IdentifierAction}
import helpers.CashAccountUtils
import models.CashAccount
import models.request.{CashAccountPaymentDetails, DeclarationDetailsSearch, IdentifierRequest, ParamName, SearchType}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{cash_account_declaration_details, cash_transactions_no_result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging
import utils.RegexPatterns.{mrnUCRRegex, paymentRegex, superMRNUCRRegex}
import utils.Utils.{emptyString, poundSymbol}
import viewmodels.{DeclarationDetailViewModel, ResultsPageSummary}

import java.time.LocalDate

class DeclarationDetailController @Inject()(authenticate: IdentifierAction,
                                            verifyEmail: EmailAction,
                                            apiConnector: CustomsFinancialsApiConnector,
                                            errorHandler: ErrorHandler,
                                            mcc: MessagesControllerComponents,
                                            view: cash_account_declaration_details,
                                            cashAccountUtils: CashAccountUtils,
                                            noTransactionsView: cash_transactions_no_result
                                           )(implicit executionContext: ExecutionContext,
                                             appConfig: AppConfig
                                           ) extends FrontendController(mcc) with I18nSupport with Logging {

  def displaySearchDetails(ref: String, page: Option[Int], searchInput: String): Action[AnyContent] =
    displayDetails(ref, page, cameViaSearch = true, searchInput)

  def displayDetails(ref: String,
                     page: Option[Int],
                     cameViaSearch: Boolean = false,
                     searchInput: String = emptyString
                    ): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    for {
      accountOpt <- apiConnector.getCashAccount(request.eori)
      result <- accountOpt match {
        case Some(account) =>
          val (from, to) = cashAccountUtils.transactionDateRange()
          retrieveCashAccountTransactionAndDisplay(account, from, to, ref, page, cameViaSearch, searchInput)
        case None => Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
    } yield result
  }

  def handleSearchRequest(page: Option[Int], searchInput: String)
                         (implicit request: IdentifierRequest[AnyContent]): Future[Result] = {

    if (isValidMRNUCR(searchInput)) {
      val paramName = determineSearchType(searchInput)
      val declarationDetailsSearch = DeclarationDetailsSearch(paramName, searchInput)

      searchDeclarations(page, searchInput, SearchType.D, Some(declarationDetailsSearch), None)
    } else if (isValidPayment(searchInput)) {
      val paymentAmount = parsePaymentAmount(searchInput)
      val cashAccountPaymentDetails = CashAccountPaymentDetails(amount = paymentAmount.toDouble)

      searchDeclarations(page, searchInput, SearchType.P, None, Some(cashAccountPaymentDetails))
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }
  }

  def determineSearchType(searchInput: String): ParamName.Value = {
    if (mrnUCRRegex.findFirstIn(searchInput).isDefined) ParamName.MRN else ParamName.UCR
  }

  def isValidMRNUCR(value: String): Boolean =
    mrnUCRRegex.findFirstIn(value).isDefined || superMRNUCRRegex.findFirstIn(value).isDefined

  def isValidPayment(value: String): Boolean = paymentRegex.findFirstIn(value).isDefined

  def parsePaymentAmount(value: String): BigDecimal = BigDecimal(value.replace(poundSymbol, emptyString).trim)

  def searchDeclarations(page: Option[Int],
                         searchInput: String,
                         searchType: SearchType.Value,
                         declarationDetails: Option[DeclarationDetailsSearch],
                         cashAccountPaymentDetails: Option[CashAccountPaymentDetails]
                        )(implicit request: IdentifierRequest[AnyContent]): Future[Result] = {

    val (from, to) = cashAccountUtils.transactionDateRange()

    for {
      maybeAccount <- apiConnector.getCashAccount(request.eori)
      transactions <- maybeAccount match {
        case Some(account) =>
          apiConnector.retrieveCashTransactionsBySearch(
            account.number,
            request.eori,
            searchType,
            declarationDetails,
            cashAccountPaymentDetails)
        case None => Future.successful(Left(noTransactionsView))
      }
    } yield {
      transactions match {
        case Right(transactionSearchResponse) =>
          transactionSearchResponse.declarations
            .getOrElse(Seq.empty)
            .find { wrapper =>
              declarationDetails match {
                case Some(details) =>
                  wrapper.declaration.declarationID == details.paramValue ||
                    wrapper.declaration.declarantRef.contains(details.paramValue)
                case None => true
              }
            }
            .map(declaration => Redirect(
              routes.DeclarationDetailController
                .displaySearchDetails(declaration.declaration.declarationID, page, searchInput))
            ).getOrElse(NotFound(errorHandler.notFoundTemplate))

        case Left(_) => Ok(noTransactionsView(ResultsPageSummary(from, to)))
      }
    }
  }

  private def retrieveCashAccountTransactionAndDisplay(account: CashAccount,
                                                       from: LocalDate,
                                                       to: LocalDate,
                                                       ref: String,
                                                       page: Option[Int],
                                                       cameViaSearch: Boolean,
                                                       searchInput: String
                                                      )(implicit request: IdentifierRequest[_]): Future[Result] = {
    apiConnector.retrieveCashTransactions(account.number, from, to).map {
      case Right(transactions) =>
        transactions.cashDailyStatements
          .flatMap(_.declarations)
          .find(_.secureMovementReferenceNumber.contains(ref))
          .map { declaration =>
            val viewModel = DeclarationDetailViewModel(request.eori, account, cameViaSearch, searchInput, declaration)
            Ok(view(viewModel, page))
          }
          .getOrElse(NotFound(errorHandler.notFoundTemplate))

      case Left(_) => Ok(noTransactionsView(ResultsPageSummary(from, to)))
    }
  }
}
