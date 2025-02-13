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
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested}
import controllers.actions.IdentifierAction
import models.request.IdentifierRequest
import models.{CashAccount, CashAccountViewModel, RequestedDateRange}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.ResultsPageSummary
import views.html.{
  cash_account_transactions_not_available, cash_transactions_no_result, cash_transactions_result_page,
  cash_transactions_too_many_results
}
import play.api.Logging

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestedTransactionsController @Inject() (
  resultView: cash_transactions_result_page,
  apiConnector: CustomsFinancialsApiConnector,
  transactionsUnavailable: cash_account_transactions_not_available,
  tooManyResults: cash_transactions_too_many_results,
  noResults: cash_transactions_no_result,
  identify: IdentifierAction,
  eh: ErrorHandler,
  cache: RequestedTransactionsCache,
  mcc: MessagesControllerComponents
)(implicit executionContext: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    getCachedDatesAndDisplayRequestedTransactions(request.eori)
      .recover { case e =>
        logger.error(s"Unable to retrieve account details requested: ${e.getMessage}")
        Redirect(routes.CashAccountController.showAccountUnavailable)
      }
  }

  private def getCachedDatesAndDisplayRequestedTransactions(
    eori: String
  )(implicit request: IdentifierRequest[AnyContent]): Future[Result] =
    cache.get(request.eori).flatMap {
      case Some(dates) =>
        apiConnector.getCashAccount(request.eori).flatMap {
          case Some(account) =>
            showAccountWithTransactionDetails(account, dates.start, dates.end)
          case None          => eh.notFoundTemplate.map(NotFound(_))
        }
      case None        => Future.successful(Redirect(routes.RequestTransactionsController.onPageLoad()))
    }

  private def showAccountWithTransactionDetails(account: CashAccount, from: LocalDate, to: LocalDate)(implicit
    req: IdentifierRequest[AnyContent],
    appConfig: AppConfig
  ): Future[Result] =
    apiConnector.retrieveHistoricCashTransactions(account.number, from, to).map {
      case Left(errorResponse) =>
        errorResponse match {
          case NoTransactionsAvailable => Ok(noResults(new ResultsPageSummary(from, to)))

          case TooManyTransactionsRequested =>
            Redirect(routes.RequestedTransactionsController.tooManyTransactionsRequested(RequestedDateRange(from, to)))

          case _ =>
            Ok(transactionsUnavailable(CashAccountViewModel(req.eori, account), appConfig.transactionsTimeoutFlag))
        }

      case Right(_) =>
        Ok(
          resultView(
            new ResultsPageSummary(from, to),
            controllers.routes.CashAccountController.showAccountDetails(None).url
          )
        )
    }

  def tooManyTransactionsRequested(dateRange: RequestedDateRange): Action[AnyContent] =
    identify { implicit req =>
      Ok(
        tooManyResults(
          new ResultsPageSummary(dateRange.from, dateRange.to),
          controllers.routes.RequestTransactionsController.onPageLoad().url
        )
      )
    }
}
