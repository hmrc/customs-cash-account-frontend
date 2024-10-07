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

package forms.mappings

import play.api.data.FieldMapping
import play.api.data.Forms.of

import java.time.LocalDate

trait SelectMappings extends Formatters with Constraints {

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey))

  protected def localDate(invalidKey: String,
                          monthKey: String,
                          yearKey: String,
                          invalidDateKey: String,
                          useLastDayOfMonth: Boolean = false): FieldMapping[LocalDate] = {

    of(new SelectLocalDateFormatter(invalidKey, monthKey, yearKey, invalidDateKey, Seq.empty, useLastDayOfMonth))
  }

  protected def decimal(requiredKey: String = "error.required",
                        nonNumericKey: String = "error.nonNumeric"): FieldMapping[String] =
    of(decimalFormatter(requiredKey, nonNumericKey))
}
