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
import uk.gov.hmrc.crypto.{AesGcmAdCrypto, EncryptedValue}
import utils.Utils.emptyString

import java.util.UUID

object CashTransactionsEncrypter {

  def encryptCashTransactions(cashTransactions: CashTransactions, key: String): EncryptedCashTransactions =
    EncryptedCashTransactions(
      cashTransactions.pendingTransactions.map(declaration => encryptDeclaration(declaration, key)),
      cashTransactions.cashDailyStatements.map(dailyStatement =>
        EncryptedDailyStatements(
          dailyStatement.date,
          dailyStatement.openingBalance,
          dailyStatement.closingBalance,
          dailyStatement.declarations.map(encryptDeclaration(_, key)),
          dailyStatement.otherTransactions
        )
      ),
      cashTransactions.maxTransactionsExceeded
    )

  def decryptCashTransactions(encryptedCashTransactions: EncryptedCashTransactions, key: String): CashTransactions =
    CashTransactions(
      encryptedCashTransactions.pendingTransactions.map(encryptedDeclaration =>
        decryptDeclaration(encryptedDeclaration, key)
      ),
      encryptedCashTransactions.cashDailyStatement.map(encryptedDailyStatement =>
        CashDailyStatement(
          encryptedDailyStatement.date,
          encryptedDailyStatement.openingBalance,
          encryptedDailyStatement.closingBalance,
          encryptedDailyStatement.declarations.map(decryptDeclaration(_, key)),
          encryptedDailyStatement.otherTransactions
        )
      ),
      encryptedCashTransactions.maxTransactionsExceeded
    )

  private def encryptDeclaration(declaration: Declaration, key: String): EncryptedDeclaration = {
    val crypto                                 = new AesGcmAdCrypto(key)
    def encrypt(field: String): EncryptedValue = crypto.encrypt(field, key)

    def encryptSome(field: Option[String]): EncryptedValue = crypto.encrypt(field.getOrElse(emptyString), key)

    EncryptedDeclaration(
      encrypt(declaration.movementReferenceNumber),
      encryptSome(declaration.importerEori),
      encrypt(declaration.declarantEori),
      declaration.declarantReference.map(encrypt),
      declaration.date,
      declaration.amount,
      declaration.taxGroups,
      declaration.secureMovementReferenceNumber.getOrElse(UUID.randomUUID().toString)
    )
  }

  private def decryptDeclaration(encryptedDeclaration: EncryptedDeclaration, key: String): Declaration = {
    val crypto                                 = new AesGcmAdCrypto(key)
    def decrypt(field: EncryptedValue): String = crypto.decrypt(field, key)

    def decryptSome(field: EncryptedValue): Option[String] = Some(crypto.decrypt(field, key))

    Declaration(
      decrypt(encryptedDeclaration.movementReferenceNumber),
      decryptSome(encryptedDeclaration.importerEori),
      decrypt(encryptedDeclaration.declarantEori),
      encryptedDeclaration.declarantReference.map(decrypt),
      encryptedDeclaration.date,
      encryptedDeclaration.amount,
      encryptedDeclaration.taxGroups,
      Some(encryptedDeclaration.secureMovementReferenceNumber)
    )
  }
}
