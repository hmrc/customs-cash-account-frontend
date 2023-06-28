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

package helpers

object FormHelper {

  /**
   * Updates the key as per below validation
   * If key is either start or end and error msg is among future date,etmp date or tax year date
   *  Updated key - start.day
   *
   *  If key is either start or end and error msg is of invalid year length
   *   Updated key - start.year
   *
   *  If key is other than start or end
   *   Updated key is unchanged
   *
   * @return Updated FormError key
   */
  def updateFormErrorKeyForStartAndEndDate(): (String, String) => String = (key: String, errorMsg: String) => {
    val futureStartDateMsgKey = "cf.form.error.start-future-date"
    val etmpStartDateMsgKey = "cf.form.error.startDate.date-earlier-than-system-start-date"
    val taxYearStartDateMsgKey = "cf.form.error.start.date-too-far-in-past"

    val futureEndDateMsgKey = "cf.form.error.end-future-date"
    val etmpEndDateMsgKey = "cf.form.error.endDate.date-earlier-than-system-start-date"
    val taxYearEndDateMsgKey = "cf.form.error.end.date-too-far-in-past"

    val startDateMsgKeyList = List(futureStartDateMsgKey, etmpStartDateMsgKey, taxYearStartDateMsgKey)
    val endDateMsgKeyList = List(futureEndDateMsgKey, etmpEndDateMsgKey, taxYearEndDateMsgKey)

    if ((key.equals("start") || key.equals("end"))) {
      if (startDateMsgKeyList.contains(errorMsg) || endDateMsgKeyList.contains(errorMsg))
        s"$key.day"
      else
        s"$key.year"
    } else {
      key
    }
  }
}
