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

package viewmodels

import helpers.Formatters.yyyyMMddDateFormatter
import models._
import play.api.i18n.Messages

case class CashTransactionCsvRow(
  date: Option[String],
  transactionType: Option[String],
  movementReferenceNumber: Option[String],
  uniqueConsignmentReference: Option[String],
  declarantEori: Option[String],
  importerEori: Option[String],
  duty: Option[BigDecimal],
  vat: Option[BigDecimal],
  excise: Option[BigDecimal],
  credit: Option[BigDecimal],
  debit: Option[BigDecimal],
  balance: Option[BigDecimal]
) extends CSVWritable
    with FieldNames {
  override def fieldNames: Seq[String] = Seq(
    "date",
    "transactionType",
    "movementReferenceNumber",
    "uniqueConsignmentReference",
    "declarantEori",
    "importerEori",
    "duty",
    "vat",
    "excise",
    "credit",
    "debit",
    "balance"
  )
}

object CashTransactionCsvRow {

  implicit class DailyStatementCsvRowsViewModel(cashDailyStatement: CashDailyStatement)(implicit messages: Messages) {

    val closingBalance: CashTransactionCsvRow = CashTransactionCsvRow(
      date = Some(yyyyMMddDateFormatter.format(cashDailyStatement.date)),
      transactionType = Some(messages("cf.cash-account.csv.closing-balance")),
      movementReferenceNumber = None,
      uniqueConsignmentReference = None,
      declarantEori = None,
      importerEori = None,
      duty = None,
      vat = None,
      excise = None,
      credit = None,
      debit = None,
      balance = Some(cashDailyStatement.closingBalance)
    )

    val declarations: Seq[CashTransactionCsvRow] = cashDailyStatement.declarations.sorted.map { declaration =>

      val groups = declaration.taxGroups
      CashTransactionCsvRow(
        date = Some(yyyyMMddDateFormatter.format(cashDailyStatement.date)),
        transactionType = Some(messages("cf.cash-account.csv.declaration")),
        movementReferenceNumber = Some(declaration.movementReferenceNumber),
        uniqueConsignmentReference = declaration.declarantReference,
        declarantEori = Some(declaration.declarantEori),
        importerEori = declaration.importerEori,
        duty = findTaxGroups(CustomsDuty, groups),
        vat = findTaxGroups(ImportVat, groups),
        excise = findTaxGroups(ExciseDuty, groups),
        credit = None,
        debit = Some(declaration.amount.abs),
        balance = None
      )
    }

    private def findTaxGroups(taxGroupType: TaxGroupType, groups: Seq[TaxGroup]): Option[BigDecimal] =
      groups.find(_.taxGroupDescription == taxGroupType).map(_.amount.abs).orElse(Some(BigDecimal(0)))

    val withdrawals: Seq[CashTransactionCsvRow] = cashDailyStatement.withdrawals.map { withdrawal =>
      val withdrawalText = withdrawal.bankAccountNumberLastFourDigits
        .map(digits =>
          s"""${messages("cf.cash-account.detail.withdrawal")} ${messages(
              "cf.cash-account.detail.withdrawal.account-ending",
              digits
            )}"""
        )
        .orElse(Some(messages("cf.cash-account.detail.withdrawal")))

      CashTransactionCsvRow(
        date = Some(yyyyMMddDateFormatter.format(cashDailyStatement.date)),
        transactionType = withdrawalText,
        movementReferenceNumber = None,
        uniqueConsignmentReference = None,
        declarantEori = None,
        importerEori = None,
        duty = None,
        vat = None,
        excise = None,
        credit = None,
        debit = Some(withdrawal.amount.abs),
        balance = None
      )
    }

    val transfersOut: Seq[CashTransactionCsvRow] = cashDailyStatement.transfersOut.map { transfer =>
      CashTransactionCsvRow(
        date = Some(yyyyMMddDateFormatter.format(cashDailyStatement.date)),
        transactionType = Some(messages("cf.cash-account.csv.transfer-out")),
        movementReferenceNumber = None,
        uniqueConsignmentReference = None,
        declarantEori = None,
        importerEori = None,
        duty = None,
        vat = None,
        excise = None,
        credit = None,
        debit = Some(transfer.amount.abs),
        balance = None
      )
    }

    val topUps: Seq[CashTransactionCsvRow] = cashDailyStatement.topUps.map { topUp =>
      CashTransactionCsvRow(
        date = Some(yyyyMMddDateFormatter.format(cashDailyStatement.date)),
        transactionType = Some(messages("cf.cash-account.csv.top-up")),
        movementReferenceNumber = None,
        uniqueConsignmentReference = None,
        declarantEori = None,
        importerEori = None,
        duty = None,
        vat = None,
        excise = None,
        credit = Some(topUp.amount),
        debit = None,
        balance = None
      )
    }

    val transfersIn: Seq[CashTransactionCsvRow] = cashDailyStatement.transfersIn.map { transfer =>
      CashTransactionCsvRow(
        date = Some(yyyyMMddDateFormatter.format(cashDailyStatement.date)),
        transactionType = Some(messages("cf.cash-account.csv.transfer-in")),
        movementReferenceNumber = None,
        uniqueConsignmentReference = None,
        declarantEori = None,
        importerEori = None,
        duty = None,
        vat = None,
        excise = None,
        credit = Some(transfer.amount),
        debit = None,
        balance = None
      )
    }

    val openingBalance: CashTransactionCsvRow = CashTransactionCsvRow(
      date = Some(yyyyMMddDateFormatter.format(cashDailyStatement.date)),
      transactionType = Some(messages("cf.cash-account.csv.opening-balance")),
      movementReferenceNumber = None,
      uniqueConsignmentReference = None,
      declarantEori = None,
      importerEori = None,
      duty = None,
      vat = None,
      excise = None,
      credit = None,
      debit = None,
      balance = Some(cashDailyStatement.openingBalance)
    )

    def toReportLayout: Seq[CashTransactionCsvRow] =
      (closingBalance +: declarations) ++ withdrawals ++ transfersOut ++ topUps ++ transfersIn
  }
}
