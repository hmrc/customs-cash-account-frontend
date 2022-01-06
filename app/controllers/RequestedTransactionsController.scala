/*
 * Copyright 2022 HM Revenue & Customs
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
import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested}
import controllers.actions.IdentifierAction
import models.request.IdentifierRequest
import models.{CashAccount, CashAccountViewModel}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.ResultsPageSummary
import views.html.{cash_account_transactions_not_available, cash_transactions_no_result, cash_transactions_result_page, cash_transactions_too_many_results}
import cats.implicits._
import play.api.Logging

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestedTransactionsController @Inject()(resultView: cash_transactions_result_page,
                                                apiConnector: CustomsFinancialsApiConnector,
                                                transactionsUnavailable: cash_account_transactions_not_available,
                                                tooManyResults: cash_transactions_too_many_results,
                                                noResults: cash_transactions_no_result,
                                                identify: IdentifierAction,
                                                eh: ErrorHandler,
                                                cache: RequestedTransactionsCache,
                                                mcc: MessagesControllerComponents)(implicit executionContext: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    val result: EitherT[Future, Result, Result] = for {
      dates <- fromOptionF(cache.get(request.eori), Redirect(routes.RequestTransactionsController.onPageLoad()))
      account <- fromOptionF(apiConnector.getCashAccount(request.eori), NotFound(eh.notFoundTemplate))
      page <- EitherT.liftF(showAccountWithTransactionDetails(account, dates.start, dates.end))
    } yield page

    result.merge.recover {
      case e =>
        logger.error(s"Unable to retrieve account details requested: ${e.getMessage}")
        Redirect(routes.CashAccountController.showAccountUnavailable)
    }
  }

  private def showAccountWithTransactionDetails(account: CashAccount, from: LocalDate, to: LocalDate)(implicit req: IdentifierRequest[AnyContent]): Future[Result] = {
    apiConnector.retrieveHistoricCashTransactions(account.number, from, to).map {
      case Left(errorResponse) =>
        errorResponse match {
          case NoTransactionsAvailable => Ok(noResults(new ResultsPageSummary(from, to)))
          case TooManyTransactionsRequested => Ok(tooManyResults(new ResultsPageSummary(from, to), controllers.routes.RequestTransactionsController.onPageLoad.url))
          case _ => Ok(transactionsUnavailable(CashAccountViewModel(req.eori, account)))
        }
      case Right(_) =>
        Ok(resultView(new ResultsPageSummary(from, to), controllers.routes.CashAccountController.showAccountDetails(None).url))
    }
  }
}
