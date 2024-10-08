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
import models.{CashAccount, CashTransactions}
import models.request.{DeclarationDetailsSearch, IdentifierRequest, ParamName, SearchType}
import models.response.DeclarationWrapper
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{cash_account_declaration_details, cash_account_declaration_details_search, cash_transactions_no_result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging
import utils.RegexPatterns.{mrnRegex, paymentRegex}
import viewmodels.{DeclarationDetailSearchViewModel, DeclarationDetailViewModel, ResultsPageSummary}

import java.time.LocalDate

class DeclarationDetailController @Inject()(authenticate: IdentifierAction,
                                            verifyEmail: EmailAction,
                                            apiConnector: CustomsFinancialsApiConnector,
                                            errorHandler: ErrorHandler,
                                            mcc: MessagesControllerComponents,
                                            view: cash_account_declaration_details,
                                            searchView: cash_account_declaration_details_search,
                                            cashAccountUtils: CashAccountUtils,
                                            noTransactionsView: cash_transactions_no_result
                                           )(implicit executionContext: ExecutionContext,
                                             appConfig: AppConfig
                                           ) extends FrontendController(mcc) with I18nSupport with Logging {

  def displaySearchDetails(page: Option[Int], searchInput: String): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>

      apiConnector.getCashAccount(request.eori).flatMap {
        case Some(account) => prepareTransactionSearch(account, page, searchInput)
        case None => Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
    }

  private def prepareTransactionSearch(account: CashAccount,
                                       page: Option[Int],
                                       searchInput: String)(implicit request: IdentifierRequest[_]): Future[Result] = {

    val (paramName, searchType) = determineParamNameAndSearchType(searchInput)
    val declarationDetails = Some(DeclarationDetailsSearch(paramName, searchInput))

    apiConnector.retrieveCashTransactionsBySearch(account.number, request.eori, searchType, declarationDetails).map {
      case Right(transactions) => processTransactions(transactions.declarations, searchInput, account, page)
      case Left(_) => NotFound(errorHandler.notFoundTemplate)
    }
  }

  private def processTransactions(declarationsOpt: Option[Seq[DeclarationWrapper]],
                                  searchValue: String,
                                  account: CashAccount,
                                  page: Option[Int])(implicit request: IdentifierRequest[_]): Result = {

    declarationsOpt.flatMap(_.headOption.map(_.declaration)) match {
      case Some(declarationSearch) =>
        Ok(searchView(DeclarationDetailSearchViewModel(searchValue, account, declarationSearch), page))
      case None => NotFound(errorHandler.notFoundTemplate)
    }
  }

  private def determineParamNameAndSearchType(searchInput: String): (ParamName.Value, SearchType.Value) = {
    searchInput match {
      case input if isValidMRN(input) => (ParamName.MRN, SearchType.D)
      case input if isValidPayment(input) => (ParamName.MRN, SearchType.P)
      case _ => (ParamName.UCR, SearchType.D)
    }
  }

  private def isValidMRN(value: String): Boolean = mrnRegex.matches(value)

  private def isValidPayment(value: String): Boolean = paymentRegex.matches(value)

  def displayDetails(ref: String,
                     page: Option[Int]
                    ): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    for {
      accountOpt <- apiConnector.getCashAccount(request.eori)
      result <- accountOpt match {
        case Some(account) =>
          val (from, to) = cashAccountUtils.transactionDateRange()
          retrieveCashAccountTransactionAndDisplay(account, from, to, ref, page)
        case None => Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
    } yield result
  }

  private def retrieveCashAccountTransactionAndDisplay(account: CashAccount,
                                                       from: LocalDate,
                                                       to: LocalDate,
                                                       ref: String,
                                                       page: Option[Int])
                                                      (implicit request: IdentifierRequest[_]): Future[Result] = {
    apiConnector.retrieveCashTransactions(account.number, from, to).map {
      case Right(transactions) =>
        transactions.cashDailyStatements
          .flatMap(_.declarations)
          .find(_.secureMovementReferenceNumber.contains(ref))
          .map { declaration =>
            val viewModel = DeclarationDetailViewModel(account, request.eori, declaration)
            Ok(view(viewModel, page))
          }
          .getOrElse(NotFound(errorHandler.notFoundTemplate))

      case Left(_) => Ok(noTransactionsView(ResultsPageSummary(from, to)))
    }
  }
}
