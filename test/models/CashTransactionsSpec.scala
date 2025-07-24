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

import crypto.Crypto
import play.api.Configuration
import play.api.libs.json.{JsUndefined, JsValue, Json}
import utils.SpecBase

import java.time.LocalDate

class CashTransactionsSpec extends SpecBase {

  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val config    = Configuration("mongodb.encryptionKey" -> secretKey)
  private val crypto    = new Crypto(config)

  "cashTransactionTypeWrites" must {

    "write CashTransaction values without maxTransactionsExceeded field" in new Setup {
      val res: JsValue = Json.toJson(cashTxn01)
      (res \ "pendingTransactions").as[Seq[Declaration]] mustBe Seq(declarations)
      (res \ "cashDailyStatements").as[Seq[CashDailyStatement]] mustBe Seq(cashDailyStatement)
      (res \ "maxTransactionsExceeded") mustBe a[JsUndefined]
    }

    "write CashTransaction values with maxTransactionsExceeded as true" in new Setup {
      val res: JsValue = Json.toJson(cashTxn02)
      (res \ "pendingTransactions").as[Seq[Declaration]] mustBe Seq(declarations)
      (res \ "cashDailyStatements").as[Seq[CashDailyStatement]] mustBe Seq(cashDailyStatement)
      (res \ "maxTransactionsExceeded").as[Boolean] mustBe true
    }

    "serialize to JSON correctly" in new Setup {
      val model = CashTransactions(
        pendingTransactions = Seq(declarations),
        cashDailyStatements = Seq(cashDailyStatement),
        maxTransactionsExceeded = Some(true)
      )

      val json = Json.toJson(model)
      (json \ "pendingTransactions").as[Seq[Declaration]] mustBe Seq(declarations)
      (json \ "cashDailyStatements").as[Seq[CashDailyStatement]] mustBe Seq(cashDailyStatement)
      (json \ "maxTransactionsExceeded").as[Boolean] mustBe true
    }

    "deserialize from JSON correctly" in new Setup {
      val json = Json.obj(
        "pendingTransactions"     -> Json.arr(Json.toJson(declarations)),
        "cashDailyStatements"     -> Json.arr(Json.toJson(cashDailyStatement)),
        "maxTransactionsExceeded" -> true
      )

      val result = json.as[CashTransactions]
      result mustBe CashTransactions(Seq(declarations), Seq(cashDailyStatement), Some(true))
    }

    "serialize correctly when maxTransactionsExceeded is None" in new Setup {
      val model = CashTransactions(
        pendingTransactions = Seq(declarations),
        cashDailyStatements = Seq(cashDailyStatement),
        maxTransactionsExceeded = None
      )

      val json = Json.toJson(model)
      (json \ "maxTransactionsExceeded").toOption mustBe empty
    }

    "availableTransactions returns true when pendingTransactions is non-empty" in new Setup {
      CashTransactions(Seq(declarations), Seq.empty).availableTransactions mustBe true
    }

    "availableTransactions returns true when cashDailyStatements is non-empty" in new Setup {
      CashTransactions(Seq.empty, Seq(cashDailyStatement)).availableTransactions mustBe true
    }

    "availableTransactions returns false when both pendingTransactions and cashDailyStatements are empty" in new Setup {
      CashTransactions(Seq.empty, Seq.empty).availableTransactions mustBe false
    }
  }

  "EncryptedCashTransactions" must {

    "write EncryptedCashTransactions values without maxTransactionsExceeded field" in new Setup {
      val res: JsValue = Json.toJson(encryptedCashTxn01)
      (res \ "pendingTransactions").as[Seq[EncryptedDeclaration]] mustBe Seq(encryptedDeclaration)
      (res \ "cashDailyStatements") mustBe a[JsUndefined]
      (res \ "maxTransactionsExceeded") mustBe a[JsUndefined]
    }

    "write EncryptedCashTransactions values with maxTransactionsExceeded as true" in new Setup {
      val res: JsValue = Json.toJson(encryptedCashTxn02)
      (res \ "pendingTransactions").as[Seq[EncryptedDeclaration]] mustBe Seq(encryptedDeclaration)
      (res \ "cashDailyStatements") mustBe a[JsUndefined]
      (res \ "maxTransactionsExceeded").as[Boolean] mustBe false
    }

    "serialize to JSON correctly" in new Setup {
      val model = EncryptedCashTransactions(
        pendingTransactions = Seq(encryptedDeclaration),
        cashDailyStatement = Seq(encryptedDailyStatements),
        maxTransactionsExceeded = Some(false)
      )

      val json = Json.toJson(model)
      (json \ "pendingTransactions").as[Seq[EncryptedDeclaration]] mustBe Seq(encryptedDeclaration)
      (json \ "cashDailyStatement").as[Seq[EncryptedDailyStatements]] mustBe Seq(encryptedDailyStatements)
      (json \ "maxTransactionsExceeded").as[Boolean] mustBe false
    }

    "deserialize EncryptedCashTransactions from JSON correctly" in new Setup {
      val json = Json.obj(
        "pendingTransactions"     -> Json.arr(Json.toJson(encryptedDeclaration)),
        "cashDailyStatement"      -> Json.arr(Json.toJson(encryptedDailyStatements)),
        "maxTransactionsExceeded" -> false
      )

      val result = json.as[EncryptedCashTransactions]
      result mustBe EncryptedCashTransactions(Seq(encryptedDeclaration), Seq(encryptedDailyStatements), Some(false))
    }

    "serialize EncryptedCashTransactions correctly when maxTransactionsExceeded is None" in new Setup {
      val model = EncryptedCashTransactions(
        pendingTransactions = Seq(encryptedDeclaration),
        cashDailyStatement = Seq(encryptedDailyStatements),
        maxTransactionsExceeded = None
      )

      val json = Json.toJson(model)
      (json \ "maxTransactionsExceeded").toOption mustBe empty
    }

  }

  trait Setup {

    val year  = 2024
    val month = 5
    val day   = 5

    val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"

    val nonce = "someNone"

    val date: LocalDate            = LocalDate.of(year, month, day)
    val thousand: BigDecimal       = BigDecimal(1000.00)
    val twoThousand: BigDecimal    = BigDecimal(2000.00)
    val minusNinety: BigDecimal    = BigDecimal(-90.00)
    val openingBalance: BigDecimal = BigDecimal(600.00)
    val closingBalance: BigDecimal = BigDecimal(1200.00)

    val movementReferenceNumber: String       = "MRN1234567890"
    val importerEori: String                  = "GB123456789000"
    val declarantEori: String                 = "GB987654321000"
    val declarantReference: String            = "UCR12345"
    val secureMovementReferenceNumber: String = "5a71a767"

    val taxTypes: Seq[TaxType] = Seq(TaxType(reasonForSecurity = Some("Reason"), taxTypeID = "50", amount = thousand))

    val declarations: Declaration = Declaration(
      movementReferenceNumber = movementReferenceNumber,
      importerEori = Some(importerEori),
      declarantEori = declarantEori,
      declarantReference = Some(declarantReference),
      date = date,
      amount = twoThousand,
      taxGroups = Seq(
        TaxGroup(CustomsDuty, twoThousand, taxTypes),
        TaxGroup(ImportVat, thousand, taxTypes),
        TaxGroup(ExciseDuty, twoThousand, taxTypes)
      ),
      secureMovementReferenceNumber = Some(secureMovementReferenceNumber)
    )

    val declaration02: Declaration = Declaration(
      "mrn4",
      Some("Importer EORI"),
      "Declarant EORI",
      Some("Declarant Reference"),
      LocalDate.parse("2020-07-20"),
      minusNinety,
      Nil,
      Some("mrn2")
    )

    val cashDailyStatement: CashDailyStatement =
      CashDailyStatement(LocalDate.parse("2020-07-20"), openingBalance, closingBalance, Seq(declaration02), Nil)

    val cashTxn01: CashTransactions = CashTransactions(Seq(declarations), Seq(cashDailyStatement), None)

    val cashTxn02: CashTransactions = CashTransactions(Seq(declarations), Seq(cashDailyStatement), Some(true))

    val encryptedDeclaration: EncryptedDeclaration = EncryptedDeclaration(
      movementReferenceNumber = crypto.encrypt(movementReferenceNumber),
      importerEori = crypto.encrypt(importerEori),
      declarantEori = crypto.encrypt(declarantEori),
      declarantReference = Some(crypto.encrypt(declarantReference)),
      date = date,
      amount = thousand,
      taxGroups = Seq(
        TaxGroup(CustomsDuty, twoThousand, taxTypes),
        TaxGroup(ImportVat, thousand, taxTypes),
        TaxGroup(ExciseDuty, twoThousand, taxTypes)
      ),
      secureMovementReferenceNumber = secureMovementReferenceNumber
    )

    val encryptedDailyStatements: EncryptedDailyStatements = EncryptedDailyStatements(
      LocalDate.parse("2020-07-18"),
      0.0,
      1000.00,
      Seq(
        encryptedDeclaration
      ),
      Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))
    )

    val encryptedCashTxn01: EncryptedCashTransactions = EncryptedCashTransactions(Seq(encryptedDeclaration), Nil, None)

    val encryptedCashTxn02: EncryptedCashTransactions =
      EncryptedCashTransactions(Seq(encryptedDeclaration), Nil, Some(false))

  }
}
