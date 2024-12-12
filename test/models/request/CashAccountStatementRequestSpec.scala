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

package models.request

import utils.SpecBase

import java.time.LocalDate

class CashAccountStatementRequestSpec extends SpecBase {

  "Populating CashStatementRequests" should {
    "return correct values" when {

      "cash daily statement request is populated correctly" in new Setup {
        val res = CashDailyStatementRequest("can", fromDate, toDate)
        res mustBe cashDailyStatementRequest
      }

      "cash account statement request is populated correctly" in new Setup {
        val res = CashAccountStatementRequestDetail(eori, "someCan", fromDate.toString, toDate.toString)
        res mustBe cashAccountStatementRequestDetail
      }
    }
  }

  trait Setup {
    val fromDate: LocalDate = LocalDate.parse("2019-10-08")
    val toDate: LocalDate   = LocalDate.parse("2020-04-08")
    val eori                = "123456789"

    val cashDailyStatementRequest: CashDailyStatementRequest = CashDailyStatementRequest("can", fromDate, toDate)

    val cashAccountStatementRequestDetail: CashAccountStatementRequestDetail =
      CashAccountStatementRequestDetail(eori, "someCan", fromDate.toString, toDate.toString)
  }
}
