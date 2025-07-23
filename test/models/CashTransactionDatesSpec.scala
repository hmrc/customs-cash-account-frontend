/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, JsResultException, JsSuccess, Json}
import utils.SpecBase
import utils.TestData.{DAY_1, MONTH_10, MONTH_8, YEAR_2023}

import java.time.LocalDate

class CashTransactionDatesSpec extends SpecBase {

  "CashTransactionDates Reads" should {

    "read valid JSON" in new Setup {
      Json.parse(validJson).validate[CashTransactionDates] mustBe JsSuccess(model)
    }

    "throw error on invalid JSON when missing a field" in {
      val invalidJson = Json.obj("start" -> "2023-01-01")

      intercept[JsResultException] {
        invalidJson.as[CashTransactionDates]
      }
    }
  }

  "CashTransactionDates Writes" should {

    "write model to JSON" in new Setup {
      Json.toJson(model) mustBe expectedJson
    }
  }

  trait Setup {
    val model: CashTransactionDates = CashTransactionDates(
      LocalDate.of(YEAR_2023, MONTH_8, DAY_1),
      LocalDate.of(YEAR_2023, MONTH_10, DAY_1)
    )

    val expectedJson: JsObject = Json.obj(
      "start" -> "2023-08-01",
      "end"   -> "2023-10-01"
    )

    val validJson: String = Json.stringify(expectedJson)
  }
}
