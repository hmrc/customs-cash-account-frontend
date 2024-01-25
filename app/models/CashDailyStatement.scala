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

import play.api.libs.json._

import java.time.LocalDate

case class CashDailyStatement(date: LocalDate,
                              openingBalance: BigDecimal,
                              closingBalance: BigDecimal,
                              declarations: Seq[Declaration],
                              otherTransactions: Seq[Transaction]
                             ) extends Ordered[CashDailyStatement] {
  override def compare(that: CashDailyStatement): Int = that.date.compareTo(date)

  val withdrawals: Seq[Transaction] = otherTransactions.filter(_.transactionType == Withdrawal)
  val topUps: Seq[Transaction] = otherTransactions.filter(_.transactionType == Payment)
  val transfersOut: Seq[Transaction] = otherTransactions.filter { transaction =>
    transaction.transactionType == Transfer && transaction.amount < 0
  }

  val transfersIn: Seq[Transaction] = otherTransactions.filter { transaction =>
    transaction.transactionType == Transfer && transaction.amount > 0
  }

  val hasTransactions: Boolean =
    withdrawals.nonEmpty || topUps.nonEmpty || transfersOut.nonEmpty || transfersIn.nonEmpty
}


case class EncryptedDailyStatements(date: LocalDate,
                                    openingBalance: BigDecimal,
                                    closingBalance: BigDecimal,
                                    declarations: Seq[EncryptedDeclaration],
                                    otherTransactions: Seq[Transaction]
                                   )

object EncryptedDailyStatements {
  implicit val taxGroupTypeReads: Reads[TaxGroupType] = CashDailyStatement.taxGroupTypeReads
  implicit val taxGroupFormatReads: Reads[TaxGroup] = CashDailyStatement.taxGroupFormatReads
  implicit val declarationFormat: OFormat[EncryptedDeclaration] = EncryptedDeclaration.format
  implicit val cashTransactionTypeReads: Reads[CashTransactionType] = CashDailyStatement.cashTransactionTypeReads
  implicit val cashTransactionTypeWrites: Writes[CashTransactionType] = CashDailyStatement.cashTransactionTypeWrites
  implicit val transactionReads: OFormat[Transaction] = CashDailyStatement.transactionReads
  implicit val cashDailyStatementReads: OFormat[CashDailyStatement] = CashDailyStatement.cashDailyStatementReads
  implicit val format: OFormat[EncryptedDailyStatements] = Json.format[EncryptedDailyStatements]
}


object CashDailyStatement {
  implicit val taxGroupTypeReads: Reads[TaxGroupType] = new Reads[TaxGroupType] {
    override def reads(json: JsValue): JsResult[TaxGroupType] = {
      json.as[String] match {
        case incomingValue if incomingValue.equalsIgnoreCase(ImportVat.onWire) => JsSuccess(ImportVat)
        case incomingValue if incomingValue.equalsIgnoreCase(ExciseDuty.onWire) => JsSuccess(ExciseDuty)
        case incomingValue if incomingValue.equalsIgnoreCase(CustomsDuty.onWire) => JsSuccess(CustomsDuty)
      }
    }
  }

  implicit val taxGroupFormatReads: Reads[TaxGroup] = Json.reads[TaxGroup]

  implicit val declarationReads: Reads[Declaration] = Json.reads[Declaration]

  implicit val cashTransactionTypeReads: Reads[CashTransactionType] = new Reads[CashTransactionType] {
    override def reads(json: JsValue): JsResult[CashTransactionType] = {
      json.as[String] match {
        case status if status.equalsIgnoreCase("Payment") => JsSuccess(Payment)
        case status if status.equalsIgnoreCase("Withdrawal") => JsSuccess(Withdrawal)
        case status if status.equalsIgnoreCase("Transfer") => JsSuccess(Transfer)
      }
    }
  }

  implicit val cashTransactionTypeWrites: Writes[CashTransactionType] = new Writes[CashTransactionType] {
    override def writes(o: CashTransactionType): JsString = JsString(
      o match {
        case Payment => "Payment"
        case Withdrawal => "Withdrawal"
        case Transfer => "Transfer"
      }
    )
  }

  implicit val transactionReads: OFormat[Transaction] = Json.format[Transaction]
  implicit val cashDailyStatementReads: OFormat[CashDailyStatement] = Json.format[CashDailyStatement]
  implicit val cashTransactionsReads: OFormat[CashTransactions] = Json.format[CashTransactions]
}
