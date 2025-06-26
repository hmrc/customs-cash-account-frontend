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

package forms

import forms.mappings.Mappings
import models.CashTransactionDates
import play.api.data.Form
import play.api.data.Forms.mapping

import java.time.{Clock, LocalDate, YearMonth}
import javax.inject.Inject

class SelectTransactionsFormProvider @Inject() (implicit clock: Clock) extends Mappings {

  def apply(): Form[CashTransactionDates] =
    Form(
      mapping(
        "start" -> localDate(
          emptyStartMonth = "cf.form.error.start.date.missing.month",
          emptyStartYear = "cf.form.error.start.date.missing.year",
          emptyEndMonth = "cf.form.error.end.date.missing.month",
          emptyEndYear = "cf.form.error.end.date.missing.year",
          emptyStartDate = "cf.form.error.start.date-number-missing",
          emptyEndDate = "cf.form.error.end.date-number-missing",
          invalidMonth = "cf.form.error.start.date.invalid.month",
          invalidYear = "cf.form.error.start.date.invalid.year",
          invalidDate = "cf.form.error.start.date.invalid.real-date"
        ).transform(_.withDayOfMonth(1), date => LocalDate.from(date))
          .verifying(
            beforeCurrentDate(errorKey = "cf.form.error.start-future-date")
          )
          .verifying(
            checkDates(
              systemStartDateErrorKey = "cf.form.error.startDate.date-earlier-than-system-start-date",
              taxYearErrorKey = "cf.form.error.start.date-too-far-in-past"
            )
          ),
        "end"   -> localDate(
          emptyStartMonth = "cf.form.error.start.date.missing.month",
          emptyStartYear = "cf.form.error.start.date.missing.year",
          emptyEndMonth = "cf.form.error.end.date.missing.month",
          emptyEndYear = "cf.form.error.end.date.missing.year",
          emptyStartDate = "cf.form.error.start.date-number-missing",
          emptyEndDate = "cf.form.error.end.date-number-missing",
          invalidMonth = "cf.form.error.end.date.invalid.month",
          invalidYear = "cf.form.error.end.date.invalid.year",
          invalidDate = "cf.form.error.end.date.invalid.real-date"
        ).transform(transformToEndDate, date => LocalDate.from(date))
          .verifying(
            beforeCurrentDate(errorKey = "cf.form.error.end-future-date")
          )
          .verifying(
            checkDates(
              systemStartDateErrorKey = "cf.form.error.endDate.date-earlier-than-system-start-date",
              taxYearErrorKey = "cf.form.error.end.date-too-far-in-past"
            )
          )
      )(CashTransactionDates.apply)(ctd => Some(Tuple.fromProductTyped(ctd)))
    )

  private def transformToEndDate(date: LocalDate) = {
    val today      = LocalDate.now
    val todayMonth = today.getMonthValue
    val todayYear  = today.getYear

    if ((todayYear == date.getYear) && (todayMonth == date.getMonthValue)) { today.minusDays(1) }
    else { YearMonth.of(date.getYear, date.getMonthValue).atEndOfMonth() }
  }
}
