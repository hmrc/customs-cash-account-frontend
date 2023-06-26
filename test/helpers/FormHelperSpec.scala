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

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import utils.SpecBase

class FormHelperSpec extends SpecBase {
  "updateFormErrorKeyForTheMessage" must {
    "append .day in the FormError key when key is either start or end and error msg key is " +
      "among future date, ETMP or Tax year" in new SetUp {

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        startKey, "cf.form.error.start-future-date") shouldBe s"$startKey.day"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        startKey, "cf.form.error.startDate.date-earlier-than-system-start-date") shouldBe s"$startKey.day"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        startKey, "cf.form.error.start.date-too-far-in-past") shouldBe s"$startKey.day"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        endKey, "cf.form.error.end-future-date") shouldBe s"$endKey.day"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        endKey, "cf.form.error.endDate.date-earlier-than-system-start-date") shouldBe s"$endKey.day"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        endKey, "cf.form.error.end.date-too-far-in-past") shouldBe s"$endKey.day"
    }

    "append .year in the FormError key when key is either start or end and " +
      "error msg key is of invalid year length" in new SetUp {

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        startKey, "cf.form.error.year.length") shouldBe s"$startKey.year"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        endKey, "cf.form.error.year.length") shouldBe s"$endKey.year"
    }

    "return the unchanged key when key in neither start or end" in new SetUp {
      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        defaultKey, "cf.form.error.year.length") shouldBe defaultKey

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        defaultKey, "cf.form.error.year.length") shouldBe defaultKey
    }
  }
}

trait SetUp {
  val startKey = "start"
  val endKey = "end"
  val defaultKey = "default"
}
