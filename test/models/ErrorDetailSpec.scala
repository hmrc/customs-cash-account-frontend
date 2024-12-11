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

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class ErrorDetailSpec extends SpecBase {

  "ErrorDetail.format" should {

    "return correct object for Json Reads" in new Setup {

      import ErrorDetail.format

      Json.fromJson(Json.parse(errorDetailsJsonString)) mustBe JsSuccess(errorDetailObject)
    }

    "write correctly for Json Writes" in new Setup {
      Json.toJson(errorDetailObject) mustBe Json.parse(errorDetailsJsonString)
    }

  }

  "SourceFaultDetail.format" should {

    "return correct object for Json Reads" in new Setup {

      import SourceFaultDetail.format

      Json.fromJson(Json.parse(sourceFaultDetailJsonString)) mustBe JsSuccess(sourceFaultDetail)
    }

    "write correctly for Json Writes" in new Setup {
      Json.toJson(sourceFaultDetail) mustBe Json.parse(sourceFaultDetailJsonString)
    }
  }

  trait Setup {
    val timestamp     = "2019-08-1618:15:41"
    val correlationId = "3jh1f6b3-f8b1-4f3c-973a-05b4720e-4567899"
    val errorCode     = "400"
    val errorMessage  = "Bad request received"
    val source        = "CDS Financials"

    val sourceFaultDetailJsonString: String =
      """{
        |"detail": [
        |"Invalid value supplied for field statementRequestId: 32"
        |]
        |}""".stripMargin

    val errorDetailsJsonString: String =
      """{
        |"timestamp": "2019-08-1618:15:41",
        | "correlationId": "3jh1f6b3-f8b1-4f3c-973a-05b4720e-4567899",
        | "errorCode": "400",
        | "errorMessage": "Bad request received",
        | "source": "CDS Financials",
        | "sourceFaultDetail": {
        |    "detail": [
        |        "Invalid value supplied for field statementRequestId: 32"
        |      ]
        |   }
        | }""".stripMargin

    val sourceFaultDetail: SourceFaultDetail =
      SourceFaultDetail(Seq("Invalid value supplied for field statementRequestId: 32"))

    val errorDetailObject: ErrorDetail =
      ErrorDetail(timestamp, correlationId, errorCode, errorMessage, source, sourceFaultDetail)
  }
}
