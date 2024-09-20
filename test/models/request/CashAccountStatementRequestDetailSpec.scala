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
import play.api.libs.json.{JsSuccess, Json}

class CashAccountStatementRequestDetailSpec extends SpecBase {

  "CashAccountStatementRequestDetailSpec" must {

    "populate correctly" in new Setup {
      val result = CashAccountStatementRequestDetail("GB123456789", "123456789", "July 2022", "August 2022")
      result mustBe expectedRes
    }

    "generate correct output using the Reads" in new Setup {
      Json.parse(requestJsValue).validate[CashAccountStatementRequestDetail] mustBe JsSuccess(requestDetails)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(requestDetails) mustBe Json.parse(requestJsValue)
    }
  }

  trait Setup {

    val eori = "GB123456789"
    val can = "123456789"
    val dateFrom = "July 2022"
    val dateTo = "August 2022"

    val expectedRes = CashAccountStatementRequestDetail(eori, can, dateFrom, dateTo)

    val requestJsValue: String =
     """{"eori": "GB123456789", "can": "123456789", "dateFrom": "July 2022","dateTo": "August 2022"}""".stripMargin

    val requestDetails: CashAccountStatementRequestDetail = CashAccountStatementRequestDetail(
      eori = "GB123456789", can = "123456789", dateFrom = "July 2022", dateTo = "August 2022")
  }
}
