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

package models.request

import play.api.libs.json.{JsResultException, JsValue, Json}
import utils.SpecBase
import utils.TestData.DAY_1

import java.time.LocalDate

class CashDailyStatementRequestSpec extends SpecBase {

  "CashDailyStatementRequest" should {
    "serialize and deserialize correctly" in new Setup {
      Json.toJson(cashDailyStatementRequest) mustBe cashDailyStatementRequestJson
      cashDailyStatementRequestJson.as[CashDailyStatementRequest] mustBe cashDailyStatementRequest
    }

    "fail to parse when required field is missing" in new Setup {
      intercept[JsResultException] {
        invalidJson.as[CashDailyStatementRequest]
      }
    }
  }

  "CashAccountStatementRequestDetail" should {
    "serialize and deserialize correctly" in {
      val detail = CashAccountStatementRequestDetail("GB123456789000", "test_can", "2023-01-01", "2023-01-31")
      val json   = Json.toJson(detail)
      json.as[CashAccountStatementRequestDetail] mustBe detail
    }

    "fail to parse invalid JSON" in {
      val invalidJson = Json.parse("""{ "eori": "GB123456789000", "dateFrom": "2023-01-01", "dateTo": "2023-01-31" }""")
      intercept[JsResultException] {
        invalidJson.as[CashAccountStatementRequestDetail]
      }
    }

    "CashAccountStatementRequestDetail" should {
      "handle empty strings in fields" in {
        val detail = CashAccountStatementRequestDetail("", "", "", "")
        val json   = Json.toJson(detail)
        json.as[CashAccountStatementRequestDetail] mustBe detail
      }
    }
  }

  trait Setup {
    val cashDailyStatementRequest: CashDailyStatementRequest = CashDailyStatementRequest(
      can = "test_can",
      from = LocalDate.now().minusDays(DAY_1),
      to = LocalDate.now()
    )

    val cashDailyStatementRequestJson: JsValue = Json.parse(
      s"""
        |{
        |  "can": "test_can",
        |  "from": "${LocalDate.now().minusDays(DAY_1)}",
        |  "to": "${LocalDate.now()}"
        |}
        |""".stripMargin
    )

    val invalidJson: JsValue = Json.parse(
      s"""
         |{
         |  "can": "test_can",
         |  "to": "${LocalDate.now()}"
         |}
         |""".stripMargin
    )
  }
}
