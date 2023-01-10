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

import java.time.Clock
import javax.inject.Inject

class CashTransactionsRequestPageFormProvider @Inject()(implicit clock: Clock) extends Mappings {

  def apply(): Form[CashTransactionDates] = {
    Form(mapping(
      "start" -> localDate(
        invalidKey = "cf.form.error.start.date-number-invalid",
        endOfMonth = false
      ).verifying(beforeCurrentDate("cf.form.error.start-future-date"))
       .verifying(checkDates("cf.form.error.startDate.date-earlier-than-system-start-date","cf.form.error.start.date-too-far-in-past")),

      "end" -> localDate(
        invalidKey = "cf.form.error.end.date-number-invalid",
        endOfMonth = true
      ).verifying(beforeCurrentMonth("cf.form.error.end-future-date"))
        .verifying(checkDates("cf.form.error.endDate.date-earlier-than-system-start-date","cf.form.error.end.date-too-far-in-past"))
    )(CashTransactionDates.apply)(CashTransactionDates.unapply)
    )
  }
}
