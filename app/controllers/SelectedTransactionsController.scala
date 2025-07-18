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
import connectors.{
  CustomsFinancialsApiConnector, EntryAlreadyExists, ErrorResponse, ExceededMaximum, NoAssociatedDataFound
}
import controllers.actions.IdentifierAction
import helpers.Formatters.dateAsMonthAndYear
import models.*
import models.request.IdentifierRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.{RequestedTooManyTransactionsViewModel, ResultsPageSummary}
import views.html.*

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectedTransactionsController @Inject() (
  selectedTransactionsView: selected_transactions,
  apiConnector: CustomsFinancialsApiConnector,
  tooManyResults: cash_transactions_too_many_results,
  duplicateDatesView: cash_transactions_duplicate_dates,
  requestTooManyTransactionsView: cash_account_requested_too_many_transactions,
  identify: IdentifierAction,
  eh: ErrorHandler,
  cache: RequestedTransactionsCache,
  mcc: MessagesControllerComponents
)(implicit executionContext: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    getCachedDatesAndDisplaySelectedTransactions(request.eori)
      .recover { case e =>
        logger.error(s"Unable to retrieve account details requested: ${e.getMessage}")
        Redirect(routes.CashAccountController.showAccountUnavailable)
      }
  }

  private def getCachedDatesAndDisplaySelectedTransactions(
    eori: String
  )(implicit request: IdentifierRequest[AnyContent]): Future[Result] =
    cache.get(request.eori).flatMap {
      case Some(dates) =>
        apiConnector.getCashAccount(request.eori).flatMap {
          case Some(account) =>
            displayResultView(account, dates.start, dates.end)
          case None          => eh.notFoundTemplate.map(NotFound(_))
        }
      case None        => Future.successful(Redirect(routes.SelectTransactionsController.onPageLoad()))
    }

  def onSubmit(): Action[AnyContent] = identify.async { implicit request =>

    val result: Future[Future[Result]] = for {
      optionalAccount: Option[CashAccount]        <- apiConnector.getCashAccount(request.eori)
      optionalDates: Option[CashTransactionDates] <- cache.get(request.eori)
    } yield checkAccountAndDatesThenRedirect(optionalAccount, optionalDates)

    result.flatten.recover { case _: Exception =>
      logger.error("failed to submit data for SelectedTransactionsController")
      Redirect(routes.CashAccountController.showAccountDetails(None))
    }
  }

  def requestedTooManyTransactions(): Action[AnyContent] = identify.async { implicit request =>
    val result: Future[Result] = cache.get(request.eori).map { optionalDates =>
      optionalDates
        .map { dates =>
          Ok(
            requestTooManyTransactionsView(
              RequestedTooManyTransactionsViewModel(
                dates.start,
                dates.end,
                routes.SelectTransactionsController.onPageLoad().url,
                routes.SelectedTransactionsController.onPageLoad().url
              )
            )
          )
        }
        .getOrElse {
          Redirect(routes.CashAccountController.showAccountDetails(None))
        }
    }

    result.recover { case _: Exception =>
      logger.error("failed to get dates from cache for tooManyTransactionsRequested")
      Redirect(routes.CashAccountController.showAccountDetails(None))
    }
  }

  def duplicateDates(displayMsg: String, startDate: String, endDate: String): Action[AnyContent] =
    identify.async { implicit req =>
      Future.successful(Ok(duplicateDatesView(displayMsg, startDate, endDate)))
    }

  def tooManyTransactionsSelected(dateRange: RequestedDateRange): Action[AnyContent] =
    identify { implicit req =>
      Ok(
        tooManyResults(
          new ResultsPageSummary(dateRange.from, dateRange.to),
          controllers.routes.SelectTransactionsController.onPageLoad().url
        )
      )
    }

  private def checkAccountAndDatesThenRedirect(
    optionalAccount: Option[CashAccount],
    optionalDates: Option[CashTransactionDates]
  )(implicit request: IdentifierRequest[AnyContent]) =
    (optionalAccount, optionalDates) match {
      case (Some(cashAcc), Some(dates)) =>
        apiConnector.postCashAccountStatementRequest(request.eori, cashAcc.number, dates.start, dates.end).map {

          case Right(_) => Redirect(routes.ConfirmationPageController.onPageLoad())

          case Left(EntryAlreadyExists) =>
            Redirect(
              routes.SelectedTransactionsController.duplicateDates(
                "cf.cash-account.duplicate.header",
                dateAsMonthAndYear(dates.start),
                dateAsMonthAndYear(dates.end)
              )
            )

          case Left(ExceededMaximum) => Redirect(routes.SelectedTransactionsController.requestedTooManyTransactions())

          case Left(errorResponse: ErrorResponse) =>
            logErrorMessage(errorResponse)
            Redirect(routes.CashAccountController.showAccountDetails(None))
        }

      case _ => Future.successful(Redirect(routes.CashAccountController.showAccountDetails(None)))
    }

  private def logErrorMessage(errorResponse: ErrorResponse): Unit =
    if (errorResponse == NoAssociatedDataFound) {
      logger.warn("Data not found for the request")
    } else {
      logger.error("Error posting cash account statements")
    }

  private def displayResultView(account: CashAccount, from: LocalDate, to: LocalDate)(implicit
    req: IdentifierRequest[AnyContent],
    appConfig: AppConfig
  ): Future[Result] =
    Future.successful(
      Ok(
        selectedTransactionsView(new ResultsPageSummary(from = from, to = to, isDay = false), account.number)
      )
    )
}
