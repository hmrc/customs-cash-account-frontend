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
import controllers.actions.*
import forms.SelectTransactionsFormProvider
import models.{CashTransactionDates, *}
import models.request.IdentifierRequest
import org.slf4j.{Logger, LoggerFactory}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.*
import forms.mappings.ConstraintsV2

import java.time.{Clock, LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectTransactionsController @Inject() (
  identify: IdentifierAction,
  formProvider: SelectTransactionsFormProvider,
  view: select_transactions,
  cache: RequestedTransactionsCache,
  clock: Clock,
  implicit val mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc)
    with I18nSupport
    with ConstraintsV2 {

  private val log: Logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def form: Form[CashTransactionDates] = formProvider()

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    cache
      .get(request.eori)
      .map {
        case Some(cachedDates) => Ok(view(form.fill(cachedDates), minTaxYear(using clock).startYear, hintExampleDate))
        case None              => Ok(view(form,minTaxYear(using clock).startYear, hintExampleDate))
      }
      .recover { case e =>
        log.error(s"Unable to retrieve cached data: ${e.getMessage}")
        Ok(view(form,minTaxYear(using clock).startYear, hintExampleDate))
      }
  }

  def onSubmit(): Action[AnyContent] = identify.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, minTaxYear(using clock).startYear, hintExampleDate))),
        value => processCustomValidation(value)
      )
  }

  private def hintExampleDate: LocalDate = LocalDateTime.now(clock).toLocalDate.minusYears(1)

  private def processCustomValidation(
    value: CashTransactionDates
  )(implicit request: IdentifierRequest[AnyContent]): Future[Result] =
    customValidation(value, form) match {
      case Some(formWithErrors) =>
        Future.successful(BadRequest(view(formWithErrors,minTaxYear(using clock).startYear, hintExampleDate)))
      case None                 =>
        cache.set(request.eori, value).map { _ =>
          Redirect(routes.SelectedTransactionsController.onPageLoad())
        }
    }

  private def customValidation(
    dates: CashTransactionDates,
    form: Form[CashTransactionDates]
  )(implicit messages: Messages): Option[Form[CashTransactionDates]] = {

    def populateErrors(message: String) : Form[CashTransactionDates] =
      form
        .withError("start", message)
        .withError("end", message)
        .fill(dates)

    dates match {
      case CashTransactionDates(start, end) if start.isAfter(end) =>
        Some(form.withError("end","cf.form.error.end-before-start", minTaxYear(using clock).startYear.toString).fill(dates))

      case CashTransactionDates(start, end) if isDateMoreThanSixTaxYearsOld(start) || isDateMoreThanSixTaxYearsOld(end) =>
        val message = messages("cf.form.error.start.date-too-far-in-past", minTaxYear(using clock).startYear.toString)
        Some(
          populateErrors(
              messages(
                message
              )
          )
            .fill(dates)
        )
      case _ => None
    }
  }

  private def isDateMoreThanSixTaxYearsOld(requestedDate: LocalDate): Boolean = {
    val dayOfMonthThatTaxYearStartsOn = 6
    minTaxYear(using clock).starts.isAfter(requestedDate.withDayOfMonth(dayOfMonthThatTaxYearStartsOn))
  }
}
