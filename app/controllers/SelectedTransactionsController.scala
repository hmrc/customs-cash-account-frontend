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
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested}
import controllers.actions.IdentifierAction
import models.request.IdentifierRequest
import models.{CashAccount, CashAccountViewModel, RequestedDateRange}
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

class SelectedTransactionsController @Inject()(resultView: selected_transactions_view,
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

  //TODO - Ticket 4900
  // def onSubmit(): Action[AnyContent] = {
  // Submit data to be added in ticket 4900
  // Redirect we have recieved your requested statements page when it exists

  private def showAccountWithTransactionDetails(account: CashAccount,
                                                from: LocalDate,
                                                to: LocalDate)
                                               (implicit req: IdentifierRequest[AnyContent],
                                                appConfig: AppConfig): Future[Result] = {
    apiConnector.retrieveHistoricCashTransactions(account.number, from, to).map {
      case Left(errorResponse) =>
        errorResponse match {
          case NoTransactionsAvailable => Ok(noResults(new ResultsPageSummary(from, to)))

          case TooManyTransactionsRequested =>
            Redirect(routes.SelectedTransactionsController.tooManyTransactionsSelected(RequestedDateRange(from, to)))

          case _ =>
            Ok(transactionsUnavailable(CashAccountViewModel(req.eori, account), appConfig.transactionsTimeoutFlag))
        }

      case Right(_) =>
        Ok(
          resultView(
            new ResultsPageSummary(from, to),
            controllers.routes.CashAccountController.showAccountDetails(None).url,
            account.number)
        )
    }
  }

  def tooManyTransactionsSelected(dateRange: RequestedDateRange): Action[AnyContent] =
    identify {
      implicit req =>

        Ok(
          tooManyResults(
            new ResultsPageSummary(dateRange.from, dateRange.to),
            controllers.routes.SelectTransactionsController.onPageLoad().url)
        )
    }
}
