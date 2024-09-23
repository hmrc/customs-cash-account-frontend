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

import models._
import utils.SpecBase

import java.time.LocalDate

class CashTransactionsEncrypterSpec extends SpecBase {

  private val cipher = new AesGCMCrypto
  private val encrypter = new CashTransactionsEncrypter(cipher)
  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"

  trait Setup {
    val listOfPendingTransactions: Seq[Declaration] =
      Seq(Declaration("pendingDeclarationID", Some("pendingImporterEORI"),
        "pendingDeclarantEORINumber", Some("pendingDeclarantReference"),
        LocalDate.parse("2020-07-21"), -100.00, Nil, Some(sMRN)))

    val cashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 0.0, 1000.00,
        Seq(Declaration("mrn1", Some("importer EORI"), "Declarant EORI",
          Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil, Some(sMRN)),
          Declaration("mrn2", Some("Importer EORI"), "Declarant EORI",
            Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -65.00, Nil, Some(sMRN))),
        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))),

      CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 1200.00,
        Seq(Declaration("mrn3", Some("Importer EORI"), "Declarant EORI",
          Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil, Some(sMRN)),
          Declaration("mrn4", Some("Importer EORI"), "Declarant EORI",
            Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil, Some(sMRN))),
        Seq(Transaction(67.89, Payment, None))))

    val cashTransactions: CashTransactions = CashTransactions(listOfPendingTransactions, cashDailyStatements)

    val cashTransactions02: CashTransactions = CashTransactions(
      listOfPendingTransactions, cashDailyStatements, Some(true))
  }

  "encrypt / decrypt cashTransactions" must {

    "encrypt cashTransactions" in new Setup {
      val encryptedCashTransactions: EncryptedCashTransactions =
        encrypter.encryptCashTransactions(cashTransactions, secretKey)

      val decryptedCashTransactions: CashTransactions =
        encrypter.decryptCashTransactions(encryptedCashTransactions, secretKey)
      decryptedCashTransactions mustEqual cashTransactions
    }

    "encrypt cashTransactions with maxTransactionsExceeded is set to true" in new Setup {
      val encryptedCashTransactions: EncryptedCashTransactions =
        encrypter.encryptCashTransactions(cashTransactions02, secretKey)

      val decryptedCashTransactions: CashTransactions =
        encrypter.decryptCashTransactions(encryptedCashTransactions, secretKey)

      decryptedCashTransactions mustEqual cashTransactions02
      decryptedCashTransactions.maxTransactionsExceeded mustEqual Some(true)
    }

    "decrypt cashTransactions" in new Setup {
      val encryptedCashTransactions: EncryptedCashTransactions =
        encrypter.encryptCashTransactions(cashTransactions, secretKey)

      val decryptedCashTransactions: CashTransactions =
        encrypter.decryptCashTransactions(encryptedCashTransactions, secretKey)
      decryptedCashTransactions mustEqual cashTransactions
    }
  }
}
