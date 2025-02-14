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
import uk.gov.hmrc.crypto.Crypted
import utils.Utils.emptyString

import java.util.UUID
import javax.inject.Inject

class CashTransactionsEncrypter @Inject() (crypto: CryptoAdapter) {

  def encryptCashTransactions(cashTransactions: CashTransactions): EncryptedCashTransactions =
    EncryptedCashTransactions(
      cashTransactions.pendingTransactions.map(declaration => encryptDeclaration(declaration)),
      cashTransactions.cashDailyStatements.map(dailyStatement =>
        EncryptedDailyStatements(
          dailyStatement.date,
          dailyStatement.openingBalance,
          dailyStatement.closingBalance,
          dailyStatement.declarations.map(encryptDeclaration),
          dailyStatement.otherTransactions
        )
      ),
      cashTransactions.maxTransactionsExceeded
    )

  def decryptCashTransactions(encryptedCashTransactions: EncryptedCashTransactions): CashTransactions =
    CashTransactions(
      encryptedCashTransactions.pendingTransactions.map(encryptedDeclaration =>
        decryptDeclaration(encryptedDeclaration)
      ),
      encryptedCashTransactions.cashDailyStatement.map(encryptedDailyStatement =>
        CashDailyStatement(
          encryptedDailyStatement.date,
          encryptedDailyStatement.openingBalance,
          encryptedDailyStatement.closingBalance,
          encryptedDailyStatement.declarations.map(decryptDeclaration),
          encryptedDailyStatement.otherTransactions
        )
      ),
      encryptedCashTransactions.maxTransactionsExceeded
    )

  private def encryptDeclaration(declaration: Declaration): EncryptedDeclaration = {
    def encrypt(field: String): Either[EncryptedValue, Crypted] = crypto.encrypt(field)

    def encryptSome(field: Option[String]): Either[EncryptedValue, Crypted] =
      crypto.encrypt(field.getOrElse(emptyString))

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

  private def decryptDeclaration(encryptedDeclaration: EncryptedDeclaration): Declaration = {
    def decrypt(field: Either[EncryptedValue, Crypted]): String = crypto.decrypt(field)

    def decryptSome(field: Either[EncryptedValue, Crypted]): Option[String] = Some(crypto.decrypt(field))

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
