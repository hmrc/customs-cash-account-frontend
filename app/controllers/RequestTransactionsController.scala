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

import config.AppConfig
import controllers.actions._
import forms.CashTransactionsRequestPageFormProvider
import models._
import org.slf4j.LoggerFactory
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestTransactionsController @Inject()(
                                               identify: IdentifierAction,
                                               formProvider: CashTransactionsRequestPageFormProvider,
                                               view: cash_transactions_request_page,
                                               cache: RequestedTransactionsCache,
                                               implicit val mcc: MessagesControllerComponents)
                                             (implicit ec: ExecutionContext,
                                              appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  val log = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def form: Form[CashTransactionDates] = formProvider()

  def onPageLoad: Action[AnyContent] = identify.async {
    implicit request => for {
        _ <- cache.clear(request.eori)
      } yield Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = identify.async {
    implicit request =>
      form.bindFromRequest().fold(formWithErrors => {
        logMessageForAnalytics(request.eori,
          formWithErrors.data.getOrElse("start.day", " ")+"-"+formWithErrors.data.getOrElse("start.month", " ")+"-"+formWithErrors.data.getOrElse("start.year", " "),
          formWithErrors.data.getOrElse("end.day", " ")+"-"+formWithErrors.data.getOrElse("end.month", " ")+"-"+formWithErrors.data.getOrElse("end.year", " "),
          formWithErrors.errors)
        Future.successful(BadRequest(view(formWithErrors)))
      },
        value => customValidation(value, form)() match {
            case Some(formWithErrors) =>
              logMessageForAnalytics(request.eori,
                formWithErrors.data.getOrElse("start.year", " ")+"-"+formWithErrors.data.getOrElse("start.month", " ")+"-"+formWithErrors.data.getOrElse("start.day", " "),
                formWithErrors.data.getOrElse("end.year", " ")+"-"+formWithErrors.data.getOrElse("end.month", " ")+"-"+formWithErrors.data.getOrElse("end.day", " "),
                formWithErrors.errors)
              Future.successful(BadRequest(view(formWithErrors)))
            case None =>
              cache.set(request.eori, value).map { _ =>
                Redirect(routes.RequestedTransactionsController.onPageLoad())
              }
          }
      )
  }

  private def customValidation(dates: CashTransactionDates, form: Form[CashTransactionDates])(): Option[Form[CashTransactionDates]] = {
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

  private def logMessageForAnalytics(eori: String, startDate: String, endDate: String, errors: Seq[FormError])
                                    (implicit messages: Messages): Unit= {
    val errorMessages = errors.map( e =>messages(e.message)).mkString(",")
    log.info(s"Cash account, transaction request service, eori number: $eori, " +
      s"start date: $startDate, end date: $endDate, error: $errorMessages")
  }
}
