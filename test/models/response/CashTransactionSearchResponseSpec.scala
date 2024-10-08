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

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase
import play.api.libs.json._

class CashTransactionSearchResponseSpec extends SpecBase {

  "CashAccountTransactionSearchResponseDetailSpec" must {

    "populate correctly with all the fields" in new Setup {
      val result: CashAccountTransactionSearchResponseDetail = CashAccountTransactionSearchResponseDetail(
        can = can,
        eoriDetails = eoriDetails,
        declarations = Some(declarations),
        paymentsWithdrawalsAndTransfers = Some(paymentsWithdrawalsAndTransfers))

      result mustBe expectedResponseDetail
    }

    "generate correct output using Reads with all the fields" in new Setup {
      Json.parse(responseJsValue)
        .validate[CashAccountTransactionSearchResponseDetail] mustBe JsSuccess(expectedResponseDetail)
    }

    "generate correct output using Writes with all fields" in new Setup {
      Json.toJson(expectedResponseDetail) mustBe Json.parse(responseJsValue)
    }
  }

  trait Setup {

    val can = "123456789"
    val eoriDetails: Seq[EoriDataContainer] = Seq(
      EoriDataContainer(EoriData(eoriNumber = "GB123456789", name = "eori name")),
      EoriDataContainer(EoriData(eoriNumber = "GB987654321", name = "eori name two")))

    val declarations: Seq[DeclarationWrapper] = Seq(
      DeclarationWrapper(DeclarationSearch(
        declarationID = "18GB9JLC3CU1LFGVR8",
        declarantEORINumber = "GB123456789",
        importersEORINumber = "GB987654321",
        postingDate = "2022-07-15",
        acceptanceDate = "2022-07-01",
        amount = 2500.0,
        taxGroups = Seq(
          TaxGroupWrapper(TaxGroupSearch(
            taxGroupDescription = "VAT",
            amount = 2000.0,
            taxTypes = Seq(TaxTypeWithSecurityContainer(TaxTypeWithSecurity(
              reasonForSecurity = Some("Duty"),
              taxTypeID = "A10",
              amount = 2000.0
            )))
          ))
        )
      ))
    )

    val paymentsWithdrawalsAndTransfers: Seq[PaymentsWithdrawalsAndTransferContainer] = Seq(
      PaymentsWithdrawalsAndTransferContainer(PaymentsWithdrawalsAndTransfer(
        valueDate = "2022-07-20",
        postingDate = "2022-07-21",
        paymentReference = "REF123456",
        amount = 1500.0,
        `type` = PaymentType.Payment,
        bankAccount = Some("12345678"),
        sortCode = Some("12-34-56"))))

    val expectedResponseDetail: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        can = can,
        eoriDetails = eoriDetails,
        declarations = Some(declarations),
        paymentsWithdrawalsAndTransfers = Some(paymentsWithdrawalsAndTransfers))

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
