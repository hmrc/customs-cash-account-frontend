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
import play.api.libs.json.{JsError, JsObject, JsResult, JsString, JsSuccess, JsValue, Json}
import uk.gov.hmrc.crypto.Crypted
import utils.TestData.AMOUNT

import java.time.LocalDate

class CashDailyStatementSpec extends SpecBase {

  "taxGroupTypeReads" must {

    "read ImportVat correctly as jsSuccess ImportVat" in new Setup {
      val res: JsResult[TaxGroupType] = CashDailyStatement.taxGroupTypeReads.reads(jsImport)
      res mustBe JsSuccess(ImportVat)
    }

    "read Excise correctly as jsSuccess Excise" in new Setup {
      val res: JsResult[TaxGroupType] = CashDailyStatement.taxGroupTypeReads.reads(jsExcise)
      res mustBe JsSuccess(ExciseDuty)
    }

    "read Customs correctly as jsSuccess Customs" in new Setup {
      val res: JsResult[TaxGroupType] = CashDailyStatement.taxGroupTypeReads.reads(jsCustoms)
      res mustBe JsSuccess(CustomsDuty)
    }

    "throw MatchError when reading unknown TaxGroupType" in new Setup {
      intercept[MatchError] {
        CashDailyStatement.taxGroupTypeReads.reads(JsString("Random"))
      }
    }

  }

  "cashTransactionTypeReads" must {

    "read Payment correctly as jsSuccess Payment" in new Setup {
      val res: JsResult[CashTransactionType] = CashDailyStatement.cashTransactionTypeReads.reads(jsPayment)
      res mustBe JsSuccess(Payment)
    }

    "read Withdrawal correctly as jsSuccess Withdrawal" in new Setup {
      val res: JsResult[CashTransactionType] = CashDailyStatement.cashTransactionTypeReads.reads(jsWithdrawal)
      res mustBe JsSuccess(Withdrawal)
    }

    "read Transfer correctly as jsSuccess Transfer" in new Setup {
      val res: JsResult[CashTransactionType] = CashDailyStatement.cashTransactionTypeReads.reads(jsTransfer)
      res mustBe JsSuccess(Transfer)
    }

    "throw MatchError when reading unknown CashTransactionType" in new Setup {
      intercept[MatchError] {
        CashDailyStatement.cashTransactionTypeReads.reads(JsString("UnknownType"))
      }
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

  "EncryptedDailyStatements.format" must {
    "serialize and deserialize EncryptedDailyStatements successfully" in new Setup {
      val input: EncryptedDailyStatements = EncryptedDailyStatements(
        LocalDate.now(),
        AMOUNT,
        AMOUNT,
        Seq(encryptedDeclaration),
        Seq(transaction)
      )

      val json: JsValue                              = Json.toJson(input)(EncryptedDailyStatements.format)
      val result: JsResult[EncryptedDailyStatements] =
        json.validate[EncryptedDailyStatements](EncryptedDailyStatements.format)

      result mustBe JsSuccess(input)
    }

    "fail to deserialize when fields are missing" in {
      val invalidJson = Json.obj(
        "date"           -> LocalDate.now.toString,
        "openingBalance" -> AMOUNT
      )

      val result = invalidJson.validate[EncryptedDailyStatements](EncryptedDailyStatements.format)
      result mustBe a[JsError]
    }

    "fail to deserialize when amount is string instead of number" in {
      val json   = Json.obj(
        "date"              -> LocalDate.now.toString,
        "openingBalance"    -> "not_a_number",
        "closingBalance"    -> AMOUNT,
        "declarations"      -> Json.arr(),
        "otherTransactions" -> Json.arr()
      )
      val result = json.validate[EncryptedDailyStatements](EncryptedDailyStatements.format)
      result mustBe a[JsError]
    }
  }

  "taxGroupFormatReads" must {
    "read a valid TaxGroup JSON successfully" in new Setup {
      val json: JsObject             =
        Json.obj("taxGroupDescription" -> taxGroupType, "amount" -> AMOUNT, "taxTypes" -> Seq(taxType))
      val result: JsResult[TaxGroup] = json.validate[TaxGroup](CashDailyStatement.taxGroupFormatReads)
      result mustBe a[JsSuccess[_]]
    }

    "fail to read TaxGroup from invalid JSON" in {
      val invalidJson = Json.obj("invalidField" -> "invalid")
      val result      = invalidJson.validate[TaxGroup](CashDailyStatement.taxGroupFormatReads)
      result mustBe a[JsError]
    }
  }

  "compare works correctly" in new Setup {
    val today: LocalDate     = LocalDate.now()
    val yesterday: LocalDate = today.minusDays(1)

    val todayStmt: CashDailyStatement = CashDailyStatement(today, AMOUNT, AMOUNT, Nil, Nil)
    val yestStmt: CashDailyStatement  = CashDailyStatement(yesterday, AMOUNT, AMOUNT, Nil, Nil)

    (todayStmt compare yestStmt) mustBe <(0) // because reversed order
    (yestStmt compare todayStmt) mustBe >(0)
    (todayStmt compare todayStmt) mustBe 0
  }

  trait Setup {
    val jsPayment: JsString    = JsString("Payment")
    val jsWithdrawal: JsString = JsString("Withdrawal")
    val jsTransfer: JsString   = JsString("Transfer")

    val jsImport: JsString  = JsString("Import VAT")
    val jsExcise: JsString  = JsString("Excise")
    val jsCustoms: JsString = JsString("Customs")

    val taxType: TaxType           = TaxType(Some("test_reason"), "Customs", AMOUNT)
    val taxGroupType: TaxGroupType = CustomsDuty
    val taxGroup: TaxGroup         = TaxGroup(taxGroupType, AMOUNT, Seq(taxType))

    val encryptedDeclaration: EncryptedDeclaration = EncryptedDeclaration(
      movementReferenceNumber = Crypted("test_ref"),
      importerEori = Crypted("test_importer"),
      declarantEori = Crypted("test_dec_eori"),
      declarantReference = Some(Crypted("test_dec_ref")),
      date = LocalDate.now(),
      amount = AMOUNT,
      taxGroups = Seq(taxGroup),
      secureMovementReferenceNumber = "Test_123"
    )

    val transaction: Transaction = Transaction(AMOUNT, Payment, Some("ACC123"))
  }
}
