/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.data.EitherT._
import cats.instances.future._
import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested}
import controllers.actions._
import forms.CashTransactionsRequestPageFormProvider
import models._
import models.request.IdentifierRequest
import org.slf4j.LoggerFactory
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.ResultsPageSummary
import views.html._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CashTransactionsRequestPageController @Inject()(
                                                       identify: IdentifierAction,
                                                       formProvider: CashTransactionsRequestPageFormProvider,
                                                       view: cash_transactions_request_page,
                                                       resultView: cash_transactions_result_page,
                                                       apiConnector: CustomsFinancialsApiConnector,
                                                       transactionsUnavailable: cash_account_transactions_not_available,
                                                       tooManyResults: cash_transactions_too_many_results,
                                                       noResults: cash_transactions_no_result,
                                                       implicit val mcc: MessagesControllerComponents)
                                                     (implicit ec: ExecutionContext,
                                                      eh: ErrorHandler,
                                                      messagesApi: MessagesApi,
                                                      appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def form: Form[CashTransactionDates] = formProvider()

  def onPageLoad(): Action[AnyContent] = identify {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = identify.async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        value =>
          customValidation(value, form) match {
            case Some(formWithErrors) =>
              Future.successful(BadRequest(view(formWithErrors)))
            case None => {
              val eventualMaybeCashAccount = apiConnector.getCashAccount(request.eori)
              val result = for {
                cashAccount <- fromOptionF[Future, Result, CashAccount](eventualMaybeCashAccount, NotFound(eh.notFoundTemplate))
                (from, to) = (value.start, value.end)
                page <- liftF[Future, Result, Result](showAccountWithTransactionDetails(cashAccount, from, to))
              } yield page
              result.merge.recover {
                case e =>
                  logger.error(s"Unable to retrieve account details: ${e.getMessage}")
                  Redirect(routes.CashAccountController.showAccountUnavailable())
              }
            }
          }
      )
  }

  private def customValidation(dates: CashTransactionDates, form: Form[CashTransactionDates])(implicit messages: Messages): Option[Form[CashTransactionDates]] = {
    def populateErrors(startMessage: String, endMessage: String): Form[CashTransactionDates] = {
      form.withError("start", startMessage)
        .withError("end", endMessage).fill(dates)
    }

    dates match {
      case CashTransactionDates(start, end) if start.isAfter(end) =>
        Some(populateErrors("cf.form.error.start-after-end", "cf.form.error.end-before-start"))
      case _ => None
    }
  }

  private def showAccountWithTransactionDetails(account: CashAccount, from: LocalDate, to: LocalDate)(implicit req: IdentifierRequest[AnyContent]): Future[Result] = {
    apiConnector.retrieveHistoricCashTransactions(account.number, from, to).map {
      case Left(errorResponse) =>
        errorResponse match {
          case NoTransactionsAvailable => Ok(noResults(new ResultsPageSummary(from, to)))
          case TooManyTransactionsRequested => Ok(tooManyResults(new ResultsPageSummary(from, to), controllers.routes.CashTransactionsRequestPageController.onPageLoad().url))
          case _ => Ok(transactionsUnavailable(CashAccountViewModel(req.eori, account)))
        }
      case Right(_) =>
        Ok(resultView(new ResultsPageSummary(from, to), controllers.routes.CashAccountController.showAccountDetails(None).url))
    }
  }
}
