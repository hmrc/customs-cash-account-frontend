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

import java.time.Clock
import javax.inject.Inject

class SelectTransactionsFormProvider @Inject()(implicit clock: Clock)
  extends SelectMappings {

  def apply(): Form[CashTransactionDates] = {
    Form(
      mapping(
        "start" -> localDate(
          invalidKey = "cf.form.error.start.date-number-invalid",
          monthKey = "cf.form.error.start.date.invalid.month",
          yearKey = "cf.form.error.start.date.invalid.year",
          invalidDateKey = "cf.form.error.start.date.invalid.real-date"
        ).verifying(
          beforeCurrentDate(errorKey = "cf.form.error.start-future-date")
        ).verifying(
          checkDates(
            systemStartDateErrorKey = "cf.form.error.startDate.date-earlier-than-system-start-date",
            taxYearErrorKey = "cf.form.error.start.date-too-far-in-past",
            invalidLength = "cf.form.error.year.length"
          )
        ),
        "end" -> localDate(
          invalidKey = "cf.form.error.end.date-number-invalid",
          monthKey = "cf.form.error.end.date.invalid.month",
          yearKey = "cf.form.error.end.date.invalid.year",
          invalidDateKey = "cf.form.error.end.date.invalid.real-date",
          useLastDayOfMonth = true
        ).verifying(
          beforeCurrentDate(errorKey = "cf.form.error.end-future-date")
        ).verifying(
          checkDates(
            systemStartDateErrorKey = "cf.form.error.endDate.date-earlier-than-system-start-date",
            taxYearErrorKey = "cf.form.error.end.date-too-far-in-past",
            invalidLength = "cf.form.error.year.length"
          )
        )
      )(CashTransactionDates.apply)(ctd => Some(Tuple.fromProductTyped(ctd)))
    )
  }
}
