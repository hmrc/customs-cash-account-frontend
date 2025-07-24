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

import utils.SpecBase
import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json}

class AccountsAndBalancesSpec extends SpecBase {

  "CDSAccountStatusReads" must {

    "read AccountStatusOpen correctly as jsSuccess AccountStatusOpen" in new Setup {
      val res: JsResult[CDSAccountStatus] = CDSAccountStatus.CDSAccountStatusReads.reads(jsOpen)
      res mustBe JsSuccess(AccountStatusOpen)
    }

    "read AccountStatusSuspended correctly as jsSuccess AccountStatusSuspended" in new Setup {
      val res: JsResult[CDSAccountStatus] = CDSAccountStatus.CDSAccountStatusReads.reads(jsSuspended)
      res mustBe JsSuccess(AccountStatusSuspended)
    }

    "read AccountStatusClosed correctly as jsSuccess AccountStatusClosed" in new Setup {
      val res: JsResult[CDSAccountStatus] = CDSAccountStatus.CDSAccountStatusReads.reads(jsClosed)
      res mustBe JsSuccess(AccountStatusClosed)
    }

    "read Unknown correctly as jsSuccess AccountStatusOpen" in new Setup {
      val res: JsResult[CDSAccountStatus] = CDSAccountStatus.CDSAccountStatusReads.reads(jsFailure)
      res mustBe JsSuccess(AccountStatusOpen)
    }

    "serialize and deserialize AccountsAndBalancesRequestContainer correctly" in new Setup {
      val accountsAndBalancesRequest: AccountsAndBalancesRequest = AccountsAndBalancesRequest(
        AccountsRequestCommon.generate,
        requestDetail
      )

      val container: AccountsAndBalancesRequestContainer           =
        AccountsAndBalancesRequestContainer(accountsAndBalancesRequest)
      val json: JsValue                                            = Json.toJson(container)
      val validated: JsResult[AccountsAndBalancesRequestContainer] = json.validate[AccountsAndBalancesRequestContainer]

      validated mustBe JsSuccess(container)
    }

    "deserialize AccountsAndBalancesResponseContainer from valid JSON" in new Setup {
      val result: JsResult[AccountsAndBalancesResponseContainer] =
        validJson.validate[AccountsAndBalancesResponseContainer]
      result mustBe a[JsSuccess[_]]
    }

    "deserialize ReturnParameters from valid JSON" in {
      import AccountsAndBalancesResponseContainer.returnParametersReads

      val json   = Json.parse("""{ "paramName": "foo", "paramValue": "bar" }""")
      val result = json.validate[ReturnParameters]
      result mustBe JsSuccess(ReturnParameters("foo", "bar"))
    }

    "deserialize Limits from valid JSON" in {
      import AccountsAndBalancesResponseContainer.limitsReads

      val json   = Json.parse("""{ "periodGuaranteeLimit": "1000", "periodAccountLimit": "500" }""")
      val result = json.validate[Limits]
      result mustBe JsSuccess(Limits("1000", "500"))
    }

    "deserialize DefermentBalances from valid JSON" in {
      import AccountsAndBalancesResponseContainer.balancesReads

      val json   = Json.parse("""{ "periodAvailableGuaranteeBalance": "800", "periodAvailableAccountBalance": "300" }""")
      val result = json.validate[DefermentBalances]
      result mustBe JsSuccess(DefermentBalances("800", "300"))
    }

    "deserialize AccountsAndBalancesRequestContainer with missing optional fields" in new Setup {
      val result: JsResult[AccountsAndBalancesRequestContainer] =
        missingFieldsJson.validate[AccountsAndBalancesRequestContainer]
      result mustBe a[JsSuccess[_]]
    }

    "deserialize AccountsAndBalancesResponseContainer with null option fields" in new Setup {
      val result: JsResult[AccountsAndBalancesResponseContainer] =
        nullFieldsJson.validate[AccountsAndBalancesResponseContainer]
      result mustBe a[JsSuccess[_]]
    }

    "fail to deserialize AccountResponseCommon if required fields are null" in new Setup {
      val result: JsResult[AccountsAndBalancesResponseContainer] =
        nullFieldsRequiredJson.validate[AccountsAndBalancesResponseContainer]
      result mustBe a[JsError]
    }

    "convert to domain cash accounts from container" in new Setup {
      val container: AccountsAndBalancesResponseContainer = AccountsAndBalancesResponseContainer(
        AccountsAndBalancesResponse(
          responseCommon = Some(accountResCommon),
          responseDetail = responseDetail
        )
      )
      val result: Seq[CashAccount]                        = container.toCashAccounts

      result mustBe Seq(cdsCashAccount.toDomain)
    }

    "return an empty sequence from toCashAccounts when cdsCashAccount in None" in {
      val emptyDetail = AccountResponseDetail(Some("EORI"), Some("2024-01-01"), None)
      val container   = AccountsAndBalancesResponseContainer(
        AccountsAndBalancesResponse(None, emptyDetail)
      )

      container.toCashAccounts mustBe Seq.empty
    }

    "fail to deserialize AccountsAndBalanceResponseContainer from invalid JSON" in {
      val invalidJson = Json.parse("""{ "accountsAndBalancesResponse": {"invalidField": "invalid" } }""")
      val result      = invalidJson.validate[AccountsAndBalancesRequestContainer]

      result mustBe a[JsError]
    }
  }

  trait Setup {
    val jsOpen: JsString          = JsString("Open")
    val jsSuspended: JsString     = JsString("Suspended")
    val jsClosed: JsString        = JsString("Closed")
    val jsFailure: JsString       = JsString("123")
    private val cashAccountNumber = "987654"
    private val traderEori        = "12345678"

    val cdsCashAccount: CdsCashAccount = CdsCashAccount(
      Account(cashAccountNumber, emptyString, traderEori, Some(AccountStatusOpen), false, Some(false)),
      Some("999.99")
    )

    val requestDetail: AccountsRequestDetail = AccountsRequestDetail(
      EORINo = "GB123456789",
      accountType = Some("123456789"),
      accountNumber = Some("July 2022"),
      referenceDate = Some("August 2022")
    )

    val responseDetail: AccountResponseDetail = AccountResponseDetail(
      EORINo = Some("GB123456789"),
      referenceDate = Some("August 2022"),
      cdsCashAccount = Some(Seq(cdsCashAccount))
    )

    val accountResCommon: AccountResponseCommon =
      AccountResponseCommon("OK", Some("602-Exceeded maximum threshold of transactions"), "2021-12-17T09:30:47Z", None)

    val validJson: JsValue = Json.parse(s"""
         |{
         |  "accountsAndBalancesResponse": {
         |    "responseCommon": {
         |      "status": "OK",
         |      "statusText": "All good",
         |      "processingDate": "2024-01-01T00:00:00Z"
         |    },
         |    "responseDetail": {
         |      "EORINo": "GB123456789",
         |      "referenceDate": "2024-01-01",
         |      "cdsCashAccount": [
         |        {
         |          "account": {
         |            "number": "ACC123",
         |            "type": "cash",
         |            "owner": "GB123456789",
         |            "accountStatus": "Open",
         |            "viewBalanceIsGranted": true,
         |            "isleOfManFlag": false
         |          },
         |          "availableAccountBalance": "999.99"
         |        }
         |      ]
         |    }
         |  }
         |}
         |""".stripMargin)

    val missingFieldsJson: JsValue = Json.parse("""
        |{
        |  "accountsAndBalancesRequest": {
        |    "requestCommon": {
        |      "receiptDate": "2024-01-01T00:00:00Z",
        |      "acknowledgementReference": "12345678901234567890123456789012",
        |      "regime": "CDS"
        |    },
        |    "requestDetail": {
        |      "EORINo": "GB123456789"
        |    }
        |  }
        |}
        |""".stripMargin)

    val nullFieldsJson: JsValue = Json.parse("""
        |{
        |  "accountsAndBalancesResponse": {
        |    "responseCommon": {
        |      "status": "OK",
        |      "statusText": null,
        |      "processingDate": "2024-01-01T00:00:00Z",
        |      "returnParameters": null
        |    },
        |    "responseDetail": {
        |      "EORINo": "GB123456789",
        |      "referenceDate": "2024-01-01",
        |      "cdsCashAccount": []
        |    }
        |  }
        |}
        |""".stripMargin)

    val nullFieldsRequiredJson: JsValue = Json.parse("""
        |{
        |  "accountsAndBalancesResponse": {
        |    "responseCommon": {
        |      "status": null,
        |      "statusText": "All good",
        |      "processingDate": "2024-01-01T00:00:00Z",
        |      "returnParameters": null
        |    },
        |    "responseDetail": {
        |      "EORINo": "GB123456789",
        |      "referenceDate": "2024-01-01",
        |      "cdsCashAccount": []
        |    }
        |  }
        |}
        |""".stripMargin)
  }
}
