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
import play.api.libs.json.{JsValue, JsString, JsSuccess}

class CashDailyStatementSpec extends SpecBase {

  "taxGroupTypeReads" must {
    "read ImportVat correctly as jsSuccess ImportVat" in new Setup {
      val res = CashDailyStatement.taxGroupTypeReads.reads(jsImport)
      res mustBe JsSuccess(ImportVat)
    }

    "read Excise correctly as jsSuccess Excise" in new Setup {
      val res = CashDailyStatement.taxGroupTypeReads.reads(jsExcise)
      res mustBe JsSuccess(ExciseDuty)
    }

    "read Customs correctly as jsSuccess Customs" in new Setup {
      val res = CashDailyStatement.taxGroupTypeReads.reads(jsCustoms)
      res mustBe JsSuccess(CustomsDuty)
    }
  }

  "cashTransactionTypeReads" must {
    "read Payment correctly as jsSuccess Payment" in new Setup {
      val res = CashDailyStatement.cashTransactionTypeReads.reads(jsPayment)
      res mustBe JsSuccess(Payment)
    }

    "read Withdrawal correctly as jsSuccess Withdrawal" in new Setup {
      val res = CashDailyStatement.cashTransactionTypeReads.reads(jsWithdrawal)
      res mustBe JsSuccess(Withdrawal)
    }

    "read Transfer correctly as jsSuccess Transfer" in new Setup {
      val res = CashDailyStatement.cashTransactionTypeReads.reads(jsTransfer)
      res mustBe JsSuccess(Transfer)
    }
  }

  "cashTransactionTypeWrites" must {
    "write Payment correctly as JsString Payment" in new Setup {
      val res: JsValue = CashDailyStatement.cashTransactionTypeWrites.writes(Payment)
      res mustBe jsPayment
    }

    "write Withdrawal correctly as JsString Withdrawal" in new Setup {
      val res: JsValue = CashDailyStatement.cashTransactionTypeWrites.writes(Withdrawal)
      res mustBe jsWithdrawal
    }

    "write Transfer correctly as JsString Transfer" in new Setup {
      val res: JsValue = CashDailyStatement.cashTransactionTypeWrites.writes(Transfer)
      res mustBe jsTransfer
    }
  }

  trait Setup {
    val jsPayment: JsString = JsString("Payment")
    val jsWithdrawal: JsString = JsString("Withdrawal")
    val jsTransfer: JsString = JsString("Transfer")

    val jsImport: JsString = JsString("Import VAT")
    val jsExcise: JsString = JsString("Excise")
    val jsCustoms: JsString = JsString("Customs")
  }
}
