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

package models.response

import play.api.libs.json.{JsSuccess, JsValue, Json}
import utils.SpecBase

class CashTransactionSearchResponseSpec extends SpecBase {

  "CashAccountTransactionSearchResponseDetail" must {

    "generate correct output using Reads with all the fields" in new Setup {
      Json
        .parse(responseJsValue)
        .validate[CashAccountTransactionSearchResponseDetail] mustBe JsSuccess(expectedResponseDetail)
    }

    "generate correct output using Writes with all fields" in new Setup {
      Json.toJson(expectedResponseDetail) mustBe Json.parse(responseJsValue)
    }
  }

  "CashAccountTransactionSearchResponseContainer" must {

    "deserialize full JSON with responseCommon and responseDetail" in new Setup {
      val jsonStr: String =
        s"""
           |{
           |  "cashAccountTransactionSearchResponse": {
           |    "responseCommon": {
           |      "status": "OK",
           |      "statusText": "Success",
           |      "processingDate": "2022-07-23",
           |      "returnParameters": [
           |        { "paramName": "PARAM1", "paramValue": "VALUE1" }
           |      ]
           |    },
           |    "responseDetail": $responseJsValue
           |  }
           |}
           |""".stripMargin

      val expectedContainer: CashAccountTransactionSearchResponseContainer =
        CashAccountTransactionSearchResponseContainer(
          CashAccountTransactionSearchResponse(
            responseCommon = CashTransactionsResponseCommon(
              status = "OK",
              statusText = Some("Success"),
              processingDate = "2022-07-23",
              returnParameters = Some(Array(ReturnParameter("PARAM1", "VALUE1")))
            ),
            responseDetail = Some(expectedResponseDetail)
          )
        )

      Json.toJson(Json.parse(jsonStr).validate[CashAccountTransactionSearchResponseContainer].get) mustBe Json.toJson(
        expectedContainer
      )
    }

    "serialize full object to JSON correctly" in new Setup {
      val container: CashAccountTransactionSearchResponseContainer = CashAccountTransactionSearchResponseContainer(
        CashAccountTransactionSearchResponse(
          responseCommon = CashTransactionsResponseCommon(
            status = "OK",
            statusText = Some("Success"),
            processingDate = "2022-07-23",
            returnParameters = Some(Array(ReturnParameter("PARAM1", "VALUE1")))
          ),
          responseDetail = Some(expectedResponseDetail)
        )
      )

      val expectedJson: JsValue = Json.parse(
        s"""
           |{
           |  "cashAccountTransactionSearchResponse": {
           |    "responseCommon": {
           |      "status": "OK",
           |      "statusText": "Success",
           |      "processingDate": "2022-07-23",
           |      "returnParameters": [
           |        { "paramName": "PARAM1", "paramValue": "VALUE1" }
           |      ]
           |    },
           |    "responseDetail": $responseJsValue
           |  }
           |}
           |""".stripMargin
      )

      Json.toJson(container) mustBe expectedJson
    }
  }

  "ReturnParameter" must {
    "deserialize valid JSON" in {
      val json = Json.parse("""{"paramName": "foo", "paramValue": "bar"}""")
      json.validate[ReturnParameter] mustBe JsSuccess(ReturnParameter("foo", "bar"))
    }

    "fail on missing required fields" in {
      val json = Json.parse("""{"paramName": "foo"}""")
      json.validate[ReturnParameter].isError mustBe true
    }

    "serialize to correct JSON" in {
      Json.toJson(ReturnParameter("foo", "bar")) mustBe Json.parse(
        """{"paramName":"foo","paramValue":"bar"}"""
      )
    }
  }

  "EoriData" must {
    "serialize and deserialize correctly" in {
      val data = EoriData("GB123", "name")
      Json.toJson(data).validate[EoriData] mustBe JsSuccess(data)
    }

    "fail on missing fields" in {
      val json = Json.parse("""{"eoriNumber": "GB123"}""")
      json.validate[EoriData].isError mustBe true
    }
  }

  "EoriDataContainer" must {
    "serialize and deserialize correctly" in {
      val container = EoriDataContainer(EoriData("GB123", "name"))
      Json.toJson(container).validate[EoriDataContainer] mustBe JsSuccess(container)
    }
  }

  "TaxTypeWithSecurity" must {
    "handle optional reasonForSecurity" in {
      val json = Json.parse("""{"taxTypeID": "A10", "amount": 123.45}""")
      json.validate[TaxTypeWithSecurity].isSuccess mustBe true
    }

    "fail with missing taxTypeID" in {
      val json = Json.parse("""{"amount": 123.45}""")
      json.validate[TaxTypeWithSecurity].isError mustBe true
    }
  }

  "TaxGroupSearch" must {
    "serialize/deserialize taxTypes properly" in {
      val tax =
        TaxGroupSearch("VAT", 200.0, Seq(TaxTypeWithSecurityContainer(TaxTypeWithSecurity(Some("Duty"), "A10", 200.0))))
      Json.toJson(tax).validate[TaxGroupSearch] mustBe JsSuccess(tax)
    }
  }

  "DeclarationSearch" must {
    "serialize/deserialize with optional fields omitted" in {
      val declaration =
        DeclarationSearch("id", "declarant", None, None, "importer", "2022-01-01", "2022-01-01", 100.0, Nil)
      val json        = Json.toJson(declaration)
      json.validate[DeclarationSearch] mustBe JsSuccess(declaration)
    }

    "fail on missing required fields" in {
      val json = Json.parse("""{"declarationID": "id"}""")
      json.validate[DeclarationSearch].isError mustBe true
    }
  }

  "DeclarationWrapper" must {
    "wrap and unwrap correctly" in {
      val wrapper = DeclarationWrapper(
        DeclarationSearch("id", "decl", None, None, "imp", "2022-01-01", "2022-01-01", 0.0, Nil)
      )
      Json.toJson(wrapper).validate[DeclarationWrapper] mustBe JsSuccess(wrapper)
    }
  }

  "PaymentsWithdrawalsAndTransfer" must {
    "serialize/deserialize with all fields" in {
      val payment = PaymentsWithdrawalsAndTransfer(
        "2022-07-20",
        "2022-07-21",
        "REF123456",
        1500.0,
        PaymentType.Payment,
        Some("12345678"),
        Some("12-34-56")
      )
      Json.toJson(payment).validate[PaymentsWithdrawalsAndTransfer] mustBe JsSuccess(payment)
    }

    "fail with invalid type enum" in {
      val invalidJson = Json.parse(
        """{
          |  "valueDate": "2022-07-20",
          |  "postingDate": "2022-07-21",
          |  "paymentReference": "REF123456",
          |  "amount": 1500.0,
          |  "type": "InvalidType"
          |}""".stripMargin
      )

      intercept[NoSuchElementException] {
        invalidJson.as[PaymentsWithdrawalsAndTransfer]
      }
    }

    "handle missing optional fields gracefully" in {
      val json = Json.parse(
        """{
          |  "valueDate": "2022-07-20",
          |  "postingDate": "2022-07-21",
          |  "paymentReference": "REF123456",
          |  "amount": 1500.0,
          |  "type": "Payment"
          |}""".stripMargin
      )
      json.validate[PaymentsWithdrawalsAndTransfer].isSuccess mustBe true
    }
  }

  "PaymentsWithdrawalsAndTransferContainer" must {
    "serialize/deserialize correctly" in {
      val container = PaymentsWithdrawalsAndTransferContainer(
        PaymentsWithdrawalsAndTransfer("2022-07-20", "2022-07-21", "REF123456", 1500.0, PaymentType.Payment)
      )
      Json.toJson(container).validate[PaymentsWithdrawalsAndTransferContainer] mustBe JsSuccess(container)
    }
  }

  "CashAccountTransactionSearchResponse" must {
    "serialize and deserialize full structure with Some(responseDetail)" in new Setup {
      val response: CashAccountTransactionSearchResponse = CashAccountTransactionSearchResponse(
        CashTransactionsResponseCommon("OK", Some("text"), "2022-07-23"),
        Some(expectedResponseDetail)
      )
      Json.toJson(response).validate[CashAccountTransactionSearchResponse] mustBe JsSuccess(response)
    }

    "handle None for optional responseDetail" in {
      val response = CashAccountTransactionSearchResponse(
        CashTransactionsResponseCommon("OK", None, "2022-07-23"),
        None
      )
      Json.toJson(response).validate[CashAccountTransactionSearchResponse] mustBe JsSuccess(response)
    }
  }

  "CashAccountTransactionSearchResponseContainer" must {
    "serialize and deserialize full structure" in new Setup {
      val container: CashAccountTransactionSearchResponseContainer = CashAccountTransactionSearchResponseContainer(
        CashAccountTransactionSearchResponse(
          CashTransactionsResponseCommon("OK", Some("Success"), "2022-07-23", Some(Array(ReturnParameter("p", "v")))),
          Some(expectedResponseDetail)
        )
      )

      val json: JsValue                                         = Json.toJson(container)
      val parsed: CashAccountTransactionSearchResponseContainer = json.as[CashAccountTransactionSearchResponseContainer]

      Json.toJson(parsed) mustBe Json.toJson(container)
    }

    "fail on missing required field in responseCommon" in {
      val json = Json.parse("""{ "cashAccountTransactionSearchResponse": { "responseDetail": {} } }""")
      json.validate[CashAccountTransactionSearchResponseContainer].isError mustBe true
    }
  }

  trait Setup {

    val can                                 = "123456789"
    val eoriDetails: Seq[EoriDataContainer] = Seq(
      EoriDataContainer(EoriData(eoriNumber = "GB123456789", name = "eori name")),
      EoriDataContainer(EoriData(eoriNumber = "GB987654321", name = "eori name two"))
    )

    val declarations: Seq[DeclarationWrapper] = Seq(
      DeclarationWrapper(
        DeclarationSearch(
          declarationID = "18GB9JLC3CU1LFGVR8",
          declarantEORINumber = "GB123456789",
          importersEORINumber = "GB987654321",
          postingDate = "2022-07-15",
          acceptanceDate = "2022-07-01",
          amount = 2500.0,
          taxGroups = Seq(
            TaxGroupWrapper(
              TaxGroupSearch(
                taxGroupDescription = "VAT",
                amount = 2000.0,
                taxTypes = Seq(
                  TaxTypeWithSecurityContainer(
                    TaxTypeWithSecurity(
                      reasonForSecurity = Some("Duty"),
                      taxTypeID = "A10",
                      amount = 2000.0
                    )
                  )
                )
              )
            )
          )
        )
      )
    )

    val paymentsWithdrawalsAndTransfers: Seq[PaymentsWithdrawalsAndTransferContainer] = Seq(
      PaymentsWithdrawalsAndTransferContainer(
        PaymentsWithdrawalsAndTransfer(
          valueDate = "2022-07-20",
          postingDate = "2022-07-21",
          paymentReference = "REF123456",
          amount = 1500.0,
          `type` = PaymentType.Payment,
          bankAccount = Some("12345678"),
          sortCode = Some("12-34-56")
        )
      )
    )

    val expectedResponseDetail: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        can = can,
        eoriDetails = eoriDetails,
        declarations = Some(declarations),
        paymentsWithdrawalsAndTransfers = Some(paymentsWithdrawalsAndTransfers)
      )

    val responseJsValue: String =
      s"""
         |{
         |  "can": "$can",
         |  "eoriDetails": [
         |    {"eoriData": {"eoriNumber": "GB123456789", "name": "eori name"}},
         |    {"eoriData": {"eoriNumber": "GB987654321", "name": "eori name two"}}
         |  ],
         |  "declarations": [
         |    {
         |      "declaration": {
         |        "declarationID": "18GB9JLC3CU1LFGVR8",
         |        "declarantEORINumber": "GB123456789",
         |        "importersEORINumber": "GB987654321",
         |        "postingDate": "2022-07-15",
         |        "acceptanceDate": "2022-07-01",
         |        "amount": 2500.0,
         |        "taxGroups": [
         |          {
         |            "taxGroup": {
         |              "taxGroupDescription": "VAT",
         |              "amount": 2000.0,
         |              "taxTypes": [
         |                {
         |                  "taxType": {
         |                    "reasonForSecurity": "Duty",
         |                    "taxTypeID": "A10",
         |                    "amount": 2000.0
         |                  }
         |                }
         |              ]
         |            }
         |          }
         |        ]
         |      }
         |    }
         |  ],
         |  "paymentsWithdrawalsAndTransfers": [
         |    {
         |      "paymentsWithdrawalsAndTransfer": {
         |        "valueDate": "2022-07-20",
         |        "postingDate": "2022-07-21",
         |        "paymentReference": "REF123456",
         |        "amount": 1500.0,
         |        "type": "Payment",
         |        "bankAccount": "12345678",
         |        "sortCode": "12-34-56"
         |      }
         |    }
         |  ]
         |}
      """.stripMargin
  }
}
