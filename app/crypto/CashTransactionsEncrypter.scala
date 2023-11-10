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

import models.{CashDailyStatement, CashTransactions, Declaration, EncryptedCashTransactions, EncryptedDailyStatements, EncryptedDeclaration}

import javax.inject.Inject

class CashTransactionsEncrypter @Inject()(crypto: AesGCMCrypto){

  def encryptCashTransactions(cashTransactions: CashTransactions, key: String): EncryptedCashTransactions = {
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
      )
    )
  }

  def decryptCashTransactions(encryptedCashTransactions: EncryptedCashTransactions, key: String): CashTransactions = {
    CashTransactions(
      encryptedCashTransactions.pendingTransactions.map(encryptedDeclaration => decryptDeclaration(encryptedDeclaration, key)),
      encryptedCashTransactions.cashDailyStatement.map( encryptedDailyStatement =>
        CashDailyStatement(
          encryptedDailyStatement.date,
          encryptedDailyStatement.openingBalance,
          encryptedDailyStatement.closingBalance,
          encryptedDailyStatement.declarations.map(decryptDeclaration(_, key)),
          encryptedDailyStatement.otherTransactions
        )
      )
    )
  }

  def encryptDeclaration(declaration: Declaration, key: String): EncryptedDeclaration = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, key)

    EncryptedDeclaration(
      e(declaration.movementReferenceNumber),
      e(declaration.importerEori),
      e(declaration.declarantEori),
      declaration.declarantReference.map(e),
      declaration.date,
      declaration.amount,
      declaration.taxGroups
    )
  }

  def decryptDeclaration(encryptedDeclaration: EncryptedDeclaration, key: String) = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, key)

    Declaration(
      d(encryptedDeclaration.movementReferenceNumber),
      d(encryptedDeclaration.importerEori),
      d(encryptedDeclaration.declarantEori),
      encryptedDeclaration.declarantReference.map(d),
      encryptedDeclaration.date,
      encryptedDeclaration.amount,
      encryptedDeclaration.taxGroups
    )
  }
}
