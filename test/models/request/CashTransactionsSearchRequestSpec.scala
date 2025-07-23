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

import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.SpecBase

class CashTransactionsSearchRequestSpec extends SpecBase {

  "CashAccountTransactionSearchRequestDetails" must {

    "generate correct output using the Reads" in new Setup {
      Json.parse(requestJsValue).validate[CashAccountTransactionSearchRequestDetails] mustBe JsSuccess(expectedDetails)
    }

    "generate correct output using the Writes" in new Setup {
      Json.toJson(expectedDetails) mustBe Json.parse(requestJsValue)
    }

    "fail to deserialize when fields are missing" in new Setup {
      Json.parse(missingFieldsJson).validate[CashAccountTransactionSearchRequestDetails].isError mustBe true
    }

    "fail to deserialize when JSON contains invalid values" in new Setup {
      Json.parse(invalidJson).validate[CashAccountTransactionSearchRequestDetails].isError mustBe true
    }

    "throw exception when paramName is missing in DeclarationDetailsSearch" in {
      val missingFieldJson =
        """{
          | "paramValue": "18GB9JLC3CU1LFGVR8"
          |}""".stripMargin

      intercept[JsResultException] {
        Json.parse(missingFieldJson).as[DeclarationDetailsSearch]
      }
    }

    "serialize and deserialize DeclarationDetailsSearch" in {
      val details = DeclarationDetailsSearch(ParamName.MRN, "18GB9JLC3CU1LFGVR8")
      val json    = Json.toJson(details)
      json mustBe Json.parse("""{"paramName":"MRN","paramValue":"18GB9JLC3CU1LFGVR8"}""")
      json.validate[DeclarationDetailsSearch] mustBe JsSuccess(details)
    }

    "throw exception for invalid paramName in DeclarationDetailsSearch" in {
      val invalidJson = """{ "paramName": "INVALID", "paramValue": "someValue" }"""

      intercept[NoSuchElementException] {
        Json.parse(invalidJson).as[DeclarationDetailsSearch]
      }
    }

    "serialize and deserialize CashAccountPaymentDetails" in {
      val payment = CashAccountPaymentDetails(1500.0, Some("2022-07-01"), Some("2022-07-31"))
      val json    = Json.toJson(payment)
      json mustBe Json.parse("""{"amount":1500.0,"dateFrom":"2022-07-01","dateTo":"2022-07-31"}""")
      json.validate[CashAccountPaymentDetails] mustBe JsSuccess(payment)
    }

    "deserialize CashAccountPaymentDetails with missing optional dates" in {
      val json = Json.parse("""{"amount":1500.0}""")
      json.validate[CashAccountPaymentDetails] mustBe JsSuccess(CashAccountPaymentDetails(1500.0, None, None))
    }
  }

  trait Setup {

    val can                                          = "123456789"
    val ownerEORI                                    = "GB123456789"
    val searchType: SearchType.Value                 = SearchType.D
    val declarationDetails: DeclarationDetailsSearch = DeclarationDetailsSearch(ParamName.MRN, "18GB9JLC3CU1LFGVR8")

    val paymentDetails: CashAccountPaymentDetails =
      CashAccountPaymentDetails(amount = 1500.0, dateFrom = Some("2022-07-01"), dateTo = Some("2022-07-31"))

    val expectedDetails: CashAccountTransactionSearchRequestDetails = CashAccountTransactionSearchRequestDetails(
      can = can,
      ownerEORI = ownerEORI,
      searchType = searchType,
      declarationDetails = Some(declarationDetails),
      cashAccountPaymentDetails = Some(paymentDetails)
    )

    val requestJsValue: String =
      s"""{
         |"can": "$can",
         |"ownerEORI": "$ownerEORI",
         |"searchType": "$searchType",
         |"declarationDetails": {
         |  "paramName": "MRN",
         |  "paramValue": "18GB9JLC3CU1LFGVR8"
         |},
         |"cashAccountPaymentDetails": {
         |  "amount": 1500.0,
         |  "dateFrom": "2022-07-01",
         |  "dateTo": "2022-07-31"
         |}
         |}""".stripMargin

    val missingFieldsJson: String =
      s"""
         |{
         |"ownerEORI": "$ownerEORI",
         |"searchType": "$searchType"
         |}
         |""".stripMargin

    val invalidJson: String =
      s"""{
         |"can": "$can",
         |"ownerEORI": "$ownerEORI",
         |"searchType": "$searchType",
         |"declarationDetails": {
         |  "paramName": "MRN",
         |  "paramValue": "18GB9JLC3CU1LFGVR8"
         |},
         |"cashAccountPaymentDetails": {
         |  "amount": "not-a-number",
         |  "dateFrom": "2022-07-01",
         |  "dateTo": "2022-07-31"
         |}
         |}""".stripMargin
  }
}
