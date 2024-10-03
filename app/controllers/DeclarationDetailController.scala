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
import models.{CashAccount, Declaration}
import models.request.IdentifierRequest
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

    if (isValidMRNUCR(searchInput) || isValidPayment(searchInput)) {
      val searchType = determineSearchType(searchInput)
      searchDeclarations(page, searchInput, searchType)
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }
  }

  def determineSearchType(searchInput: String): Declaration => Boolean = {
    if (isValidMRNUCR(searchInput)) {
      declaration =>
        declaration.movementReferenceNumber.contains(searchInput) ||
          declaration.declarantReference.contains(searchInput)
    } else {
      val paymentAmount = parsePaymentAmount(searchInput)
      (declaration: Declaration) => declaration.amount == paymentAmount
    }
  }

  def isValidMRNUCR(value: String): Boolean =
    mrnUCRRegex.findFirstIn(value).isDefined || superMRNUCRRegex.findFirstIn(value).isDefined

  def isValidPayment(value: String): Boolean = paymentRegex.findFirstIn(value).isDefined

  def parsePaymentAmount(value: String): BigDecimal = BigDecimal(value.replace(poundSymbol, emptyString).trim)

  def searchDeclarations(page: Option[Int],
                         searchInput: String,
                         matchDeclaration: Declaration => Boolean
                        )(implicit request: IdentifierRequest[AnyContent]): Future[Result] = {

    val (from, to) = cashAccountUtils.transactionDateRange()

    for {
      maybeAccount <- apiConnector.getCashAccount(request.eori)
      transactions <- maybeAccount match {
        case Some(account) => apiConnector.retrieveCashTransactions(account.number, from, to)
        case None => Future.successful(Left(noTransactionsView))
      }
    } yield {
      transactions match {
        case Right(transactions) =>
          transactions.cashDailyStatements
            .flatMap(_.declarations)
            .find(matchDeclaration)
            .flatMap(_.secureMovementReferenceNumber)
            .filter(_.nonEmpty)
            .map(uuid => Redirect(routes.DeclarationDetailController.displaySearchDetails(uuid, page, searchInput)))
            .getOrElse(NotFound(errorHandler.notFoundTemplate))

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
