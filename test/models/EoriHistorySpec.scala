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

package models

import play.api.libs.json.*
import utils.SpecBase

import java.time.LocalDate

class EoriHistorySpec extends SpecBase {

  import EoriHistory._

  "EoriHistory serialization and deserialization" should {

    "serialize correctly" in new Setup {
      Json.toJson(fullEoriHistory) mustBe fullEoriHistoryJson
    }

    "serialize correctly with missing fields" in new Setup {
      Json.toJson(missingOptionalEoriHistory) mustBe missingOptionalEoriHistoryJson
    }

    "deserialize correctly" in new Setup {
      fullEoriHistoryJson.as[EoriHistory] mustBe fullEoriHistory
    }

    "deserialize correctly with missing fields" in new Setup {
      missingOptionalEoriHistoryJson.as[EoriHistory] mustBe missingOptionalEoriHistory
    }

    "fail to deserialize when date fields have invalid format" in new Setup {
      val result: JsResult[EoriHistory] = jsonInvalidDates.validate[EoriHistory]
      result mustBe a[JsError]
    }
  }

  trait Setup {

    val year = 2024
    val month = 1
    val day = 1
    val someEori = "GB1234567892002"

    val fullEoriHistory: EoriHistory = EoriHistory(
      eori = someEori,
      validFrom = Some(LocalDate.of(year, month, day)),
      validUntil = Some(LocalDate.of(year + 1, month + 1, day + 1)))

    val missingOptionalEoriHistory: EoriHistory = EoriHistory(
      eori = someEori,
      validFrom = None,
      validUntil = None)

    val fullEoriHistoryJson: JsValue = Json.obj(
      "eori" -> someEori,
      "validFrom" -> "2024-01-01",
      "validUntil" -> "2025-02-02")

    val missingOptionalEoriHistoryJson: JsValue = Json.obj(
      "eori" -> someEori)

    val jsonInvalidDates: JsValue = Json.obj(
      "eori" -> someEori,
      "validFrom" -> "01-01-2020",
      "validUntil" -> "31-12-2025")
  }
}
