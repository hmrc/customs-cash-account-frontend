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
import play.api.data.{Form, FormError}
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
        case None              => Ok(view(form, minTaxYear(using clock).startYear, hintExampleDate))
      }
      .recover { case e =>
        log.error(s"Unable to retrieve cached data: ${e.getMessage}")
        Ok(view(form, minTaxYear(using clock).startYear, hintExampleDate))
      }
  }

  def onSubmit(): Action[AnyContent] = identify.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, minTaxYear(using clock).startYear, hintExampleDate))),
        value => processCustomValidation(value)
      )
  }

  private def hintExampleDate: LocalDate = LocalDateTime.now(clock).toLocalDate.minusYears(1)

  private def processCustomValidation(
    value: CashTransactionDates
  )(implicit request: IdentifierRequest[AnyContent]): Future[Result] =
    customValidation(value, form) match {
      case Some(formWithErrors) =>
        Future.successful(BadRequest(view(formWithErrors, minTaxYear(using clock).startYear, hintExampleDate)))
      case None                 =>
        cache.set(request.eori, value).map { _ =>
          Redirect(routes.SelectedTransactionsController.onPageLoad())
        }
    }

  private def customValidation(
    dates: CashTransactionDates,
    form: Form[CashTransactionDates]
  )(implicit messages: Messages): Option[Form[CashTransactionDates]] = {

    val CashTransactionDates(start, end) = dates

    val futureDateErrors   = Seq(futureDateError("start", start), futureDateError("end", end)).flatten
    val tooFarInPastErrors =
      Seq(tooFarInPastError("start", start), tooFarInPastError("end", end)).flatten

    val errors: Seq[FormError] =
      if (futureDateErrors.nonEmpty) {
        futureDateErrors
      } else if (end.isBefore(start)) {
        Seq(FormError("end", "cf.form.error.end-before-start"))
      } else {
        tooFarInPastErrors
      }

    if (errors.isEmpty) None else Some(errors.foldLeft(form)(_.withError(_)).fill(dates))

  }

  private def today: LocalDate = LocalDateTime.now(clock).toLocalDate

  private def futureDateError(field: String, date: LocalDate): Option[FormError] =
    if (date.isAfter(today)) {
      Some(FormError(field, s"cf.form.error.$field-future-date"))
    } else {
      None
    }

  private def tooFarInPastError(field: String, date: LocalDate)(implicit
    messages: Messages
  ): Option[FormError] =
    if (isBeforeEarliestDate(date)) {
      val earliest = minTaxYear(using clock).starts.withDayOfMonth(1)

      Some(
        FormError(
          field,
          messages(
            s"cf.form.error.$field.date-too-far-in-past",
            earliest.getYear.toString
          )
        )
      )
    } else {
      None
    }

  private def isBeforeEarliestDate(requestedDate: LocalDate): Boolean =
    requestedDate.withDayOfMonth(1).isBefore(minTaxYear(using clock).starts.withDayOfMonth(1))

  private def isDateMoreThanSixTaxYearsOld(requestedDate: LocalDate): Boolean = {
    val dayOfMonthThatTaxYearStartsOn = 6
    minTaxYear(using clock).starts.isAfter(requestedDate.withDayOfMonth(dayOfMonthThatTaxYearStartsOn))
  }
}
