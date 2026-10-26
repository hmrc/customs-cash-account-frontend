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

import forms.mappings.SelectMappings
import models.CashTransactionDates
import play.api.data.Form
import play.api.data.Forms.mapping

import java.time.{Clock, LocalDate, YearMonth}
import javax.inject.Inject

class SelectTransactionsFormProvider @Inject() (implicit clock: Clock) extends SelectMappings {

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
        )
      )(CashTransactionDates.apply)(ctd => Some(Tuple.fromProductTyped(ctd)))
    )
}
