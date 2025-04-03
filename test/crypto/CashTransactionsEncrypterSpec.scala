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

package crypto

import models.*
import play.api.Configuration
import utils.SpecBase

import java.time.LocalDate

class CashTransactionsEncrypterSpec extends SpecBase {

  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val sMRN      = "ic62zbad-75fa-445f-962b-cc92311686b8e"

  private val config    = Configuration("mongodb.encryptionKey" -> secretKey)
  private val crypto    = new Crypto(config)
  private val encrypter = new CashTransactionsEncrypter(crypto)

  "encrypt / decrypt cashTransactions" must {

    "encrypt cashTransactions" in new Setup {
      val encryptedCashTransactions: EncryptedCashTransactions =
        encrypter.encryptCashTransactions(cashTransactions)

      val decryptedCashTransactions: CashTransactions =
        encrypter.decryptCashTransactions(encryptedCashTransactions)
      decryptedCashTransactions mustEqual cashTransactions
    }

    "encrypt cashTransactions with maxTransactionsExceeded is set to true" in new Setup {
      val encryptedCashTransactions: EncryptedCashTransactions =
        encrypter.encryptCashTransactions(cashTransactions02)

      val decryptedCashTransactions: CashTransactions =
        encrypter.decryptCashTransactions(encryptedCashTransactions)

      decryptedCashTransactions mustEqual cashTransactions02
      decryptedCashTransactions.maxTransactionsExceeded mustBe Some(true)
    }

    "decrypt cashTransactions" in new Setup {
      val encryptedCashTransactions: EncryptedCashTransactions =
        encrypter.encryptCashTransactions(cashTransactions)

      val decryptedCashTransactions: CashTransactions =
        encrypter.decryptCashTransactions(encryptedCashTransactions)
      decryptedCashTransactions mustEqual cashTransactions
    }
  }

  trait Setup {
    val listOfPendingTransactions: Seq[Declaration] =
      Seq(
        Declaration(
          "pendingDeclarationID",
          Some("pendingImporterEORI"),
          "pendingDeclarantEORINumber",
          Some("pendingDeclarantReference"),
          LocalDate.parse("2020-07-21"),
          -100.00,
          Nil,
          Some(sMRN)
        )
      )

    val cashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(
        LocalDate.parse("2020-07-18"),
        0.0,
        1000.00,
        Seq(
          Declaration(
            "mrn1",
            Some("importer EORI"),
            "Declarant EORI",
            Some("Declarant Reference"),
            LocalDate.parse("2020-07-18"),
            -84.00,
            Nil,
            Some(sMRN)
          ),
          Declaration(
            "mrn2",
            Some("Importer EORI"),
            "Declarant EORI",
            Some("Declarant Reference"),
            LocalDate.parse("2020-07-18"),
            -65.00,
            Nil,
            Some(sMRN)
          )
        ),
        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))
      ),
      CashDailyStatement(
        LocalDate.parse("2020-07-20"),
        0.0,
        1200.00,
        Seq(
          Declaration(
            "mrn3",
            Some("Importer EORI"),
            "Declarant EORI",
            Some("Declarant Reference"),
            LocalDate.parse("2020-07-20"),
            -90.00,
            Nil,
            Some(sMRN)
          ),
          Declaration(
            "mrn4",
            Some("Importer EORI"),
            "Declarant EORI",
            Some("Declarant Reference"),
            LocalDate.parse("2020-07-20"),
            -30.00,
            Nil,
            Some(sMRN)
          )
        ),
        Seq(Transaction(67.89, Payment, None))
      )
    )

    val cashTransactions: CashTransactions = CashTransactions(listOfPendingTransactions, cashDailyStatements)

    val cashTransactions02: CashTransactions =
      CashTransactions(listOfPendingTransactions, cashDailyStatements, Some(true))
  }
}
