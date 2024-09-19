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

import cats.data.EitherT
import cats.data.EitherT.fromOptionF
import cats.implicits.*
import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, ErrorResponse, NoTransactionsAvailable, TooManyTransactionsRequested}
import controllers.actions.IdentifierAction
import models.*
import models.request.IdentifierRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.ResultsPageSummary
import views.html.*

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectedTransactionsController @Inject()(resultView: selected_transactions,
                                               apiConnector: CustomsFinancialsApiConnector,
                                               transactionsUnavailable: cash_account_transactions_not_available,
                                               tooManyResults: cash_transactions_too_many_results,
                                               noResults: cash_transactions_no_result,
                                               identify: IdentifierAction,
                                               eh: ErrorHandler,
                                               cache: RequestedTransactionsCache,
                                               mcc: MessagesControllerComponents)
                                              (implicit executionContext: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>

    val result: EitherT[Future, Result, Result] = for {
      dates <- fromOptionF(cache.get(request.eori), Redirect(routes.SelectTransactionsController.onPageLoad()))
      account <- fromOptionF(apiConnector.getCashAccount(request.eori), NotFound(eh.notFoundTemplate))
      page <- EitherT.liftF(showAccountWithTransactionDetails(account, dates.start, dates.end))
    } yield page

    result.merge.recover {
      case e =>
        logger.error(s"Unable to retrieve account details requested: ${e.getMessage}")
        Redirect(routes.CashAccountController.showAccountUnavailable)
    }
  }

  def onSubmit(): Action[AnyContent] = identify.async { implicit request =>

      val result: Future[Future[Result]] = for {
        optionalAccount: Option[CashAccount] <- apiConnector.getCashAccount(request.eori)
        optionalDates: Option[CashTransactionDates] <- cache.get(request.eori)
      } yield {
        checkAccountAndDatesThenRedirect(optionalAccount, optionalDates)
      }

      result.flatten.recover {
        case _: Exception =>
          logger.error("failed to submit data for SelectedTransactionsController")
          Redirect(routes.CashAccountController.showAccountDetails(None))
      }
  }

  private def checkAccountAndDatesThenRedirect(optionalAccount: Option[CashAccount],
                                               optionalDates: Option[CashTransactionDates])
                                              (implicit request: IdentifierRequest[AnyContent]) = {
    (optionalAccount, optionalDates) match {
      case (Some(cashAcc), Some(dates)) =>

        apiConnector.postCashAccountStatements(request.eori, cashAcc.number, dates.start, dates.end).map {
          case Right(_) => Redirect(routes.ConfirmationPageController.onPageLoad())
          case _ => Redirect(routes.CashAccountController.showAccountDetails(None))
        }

      case _ => Future.successful(Redirect(routes.CashAccountController.showAccountDetails(None)))
    }
  }

  private def showAccountWithTransactionDetails(account: CashAccount,
                                                from: LocalDate,
                                                to: LocalDate)
                                               (implicit req: IdentifierRequest[AnyContent],
                                                appConfig: AppConfig): Future[Result] = {
    apiConnector.retrieveHistoricCashTransactions(account.number, from, to).map {

      case Left(errorResponse) => processErrorResponse(account, from, to, errorResponse)

      case Right(_) =>
        Ok(resultView(
          new ResultsPageSummary(from, to, false),
          controllers.routes.CashAccountController.showAccountDetails(None).url,
          account.number)
        )
    }
  }

  private def processErrorResponse(account: CashAccount,
                                   from: LocalDate,
                                   to: LocalDate,
                                   errorResponse: ErrorResponse)(
                                    implicit request: IdentifierRequest[AnyContent], appConfig: AppConfig): Result = {

    errorResponse match {

      case NoTransactionsAvailable => Ok(noResults(new ResultsPageSummary(from, to)))

      case TooManyTransactionsRequested => Redirect(
        routes.SelectedTransactionsController.tooManyTransactionsSelected(RequestedDateRange(from, to)))

      case _ => Ok(transactionsUnavailable(CashAccountViewModel(request.eori, account), appConfig.transactionsTimeoutFlag))
    }
  }

  def tooManyTransactionsSelected(dateRange: RequestedDateRange): Action[AnyContent] =
    identify {
      implicit req =>
        Ok(tooManyResults(
          new ResultsPageSummary(dateRange.from, dateRange.to),
          controllers.routes.SelectTransactionsController.onPageLoad().url)
        )
    }
}
