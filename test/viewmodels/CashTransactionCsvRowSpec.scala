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

import models._
import utils.SpecBase
import viewmodels.CashTransactionCsvRow.DailyStatementCsvRowsViewModel

import java.time.LocalDate

class CashTransactionCsvRowSpec extends SpecBase {

  "generate a closing balance" in new Setup {

    override val dailyStatement: CashDailyStatement =
      CashDailyStatement(LocalDate.of(year2020, month3, day4), amountZero, 12345.67, Nil, Nil)

    val closingExpectedRow: CashTransactionCsvRow =
      expectedRow.copy(transactionType = Some("Closing balance"), balance = Some(12345.67))

    dailyStatement.toReportLayout.head must be(closingExpectedRow)
  }

  "generate a declaration row" in new Setup {

    val taxGroups: Seq[TaxGroup] = Seq(
      TaxGroup(ImportVat, -1.23, taxTypes),
      TaxGroup(CustomsDuty, -2.34, taxTypes),
      TaxGroup(ExciseDuty, -3.45, taxTypes)
    )

    val declarations: Seq[Declaration] =
      Seq(
        Declaration(
          "someMRN",
          Some("someImporterEORI"),
          "someEORI",
          None,
          dateWithDay4,
          -1234.56,
          taxGroups,
          Some(sMRN)
        )
      )

    override val dailyStatement: CashDailyStatement =
      CashDailyStatement(dateWithDay4, amountZero, amountZero, declarations, Nil)

    override val expectedRow: CashTransactionCsvRow = CashTransactionCsvRow(
      date = Some("2020-03-04"),
      movementReferenceNumber = Some("someMRN"),
      uniqueConsignmentReference = None,
      transactionType = Some("Declaration"),
      importerEori = Some("someImporterEORI"),
      declarantEori = Some("someEORI"),
      duty = Some(2.34),
      vat = Some(1.23),
      excise = Some(3.45),
      debit = Some(1234.56),
      credit = None,
      balance = None
    )

    dailyStatement.toReportLayout(1) must be(expectedRow)
  }

  "order declaration rows by ascending MRN" in new Setup {

    val declarations: Seq[Declaration] = Seq(
      Declaration("someMRN2", Some("someImporterEORI"), "someEORI", None, dateWithDay3, 1234.56, Nil, Some(sMRN)),
      Declaration("someMRN3", Some("someImporterEORI"), "someEORI", None, dateWithDay3, 1234.56, Nil, Some(sMRN)),
      Declaration("someMRN1", Some("someImporterEORI"), "someEORI", None, dateWithDay3, 1234.56, Nil, Some(sMRN))
    )

    override val dailyStatement: CashDailyStatement =
      CashDailyStatement(dateWithDay4, amountZero, amountZero, declarations, Nil)

    val expectedMrns: Seq[Some[String]] = Seq(Some("someMRN1"), Some("someMRN2"), Some("someMRN3"))

    val actualMrns: Seq[Option[String]] =
      dailyStatement.toReportLayout.filter(_.transactionType.contains("Declaration")).map(_.movementReferenceNumber)

    actualMrns must be(expectedMrns)
  }

  "default vat/duty/excise to zero if not found in the declaration" in new Setup {
    val taxGroups                      = Nil
    val declarations: Seq[Declaration] = Seq(
      Declaration("someMRN", Some("someImporterEORI"), "someEORI", None, dateWithDay3, 1234.56, taxGroups, Some(sMRN))
    )

    override val dailyStatement: CashDailyStatement =
      CashDailyStatement(dateWithDay4, amountZero, amountZero, declarations, Nil)

    override val expectedRow: CashTransactionCsvRow = CashTransactionCsvRow(
      date = Some("2020-03-04"),
      movementReferenceNumber = Some("someMRN"),
      transactionType = Some("Declaration"),
      uniqueConsignmentReference = None,
      importerEori = Some("someImporterEORI"),
      declarantEori = Some("someEORI"),
      duty = Some(amountZero),
      vat = Some(amountZero),
      excise = Some(amountZero),
      debit = Some(1234.56),
      credit = None,
      balance = None
    )

    dailyStatement.toReportLayout(1) must be(expectedRow)
  }

  "generate a withdrawal row" in new Setup {
    val withdraw: Transaction                   = withdrawal.copy(bankAccountNumber = Some("12345678"))
    val withdrawalStatement: CashDailyStatement = dailyStatement.copy(otherTransactions = Seq(withdraw))

    val withdrawalExpectedRow: CashTransactionCsvRow =
      expectedRow.copy(transactionType = Some("Withdrawal (to account ending 5678)"), debit = Some(23.45))

    withdrawalStatement.toReportLayout(1) must be(withdrawalExpectedRow)
  }

  "generate a withdrawal row when there is no bank account number" in new Setup {
    val withdrawalStatement: CashDailyStatement = dailyStatement.copy(otherTransactions = Seq(withdrawal))

    val withdrawalExpectedRow: CashTransactionCsvRow =
      expectedRow.copy(transactionType = Some("Withdrawal"), debit = Some(23.45))
    withdrawalStatement.toReportLayout(1) must be(withdrawalExpectedRow)
  }

  "generate a transfer out row" in new Setup {
    val transferStatement: CashDailyStatement = dailyStatement.copy(otherTransactions = Seq(transferOut))

    val transferExpectedRow: CashTransactionCsvRow =
      expectedRow.copy(transactionType = Some("Transfer to another account"), debit = Some(23.45))

    transferStatement.toReportLayout(1) must be(transferExpectedRow)
  }

  "generate a top-up row" in new Setup {
    val topUpStatement: CashDailyStatement = dailyStatement.copy(otherTransactions = Seq(topUp))

    val topUpExpectedRow: CashTransactionCsvRow =
      expectedRow.copy(transactionType = Some("Top-up"), credit = Some(topUp.amount))

    topUpStatement.toReportLayout(1) must be(topUpExpectedRow)
  }

  "generate a transfer in row" in new Setup {
    val transferExpectedRow: CashTransactionCsvRow =
      expectedRow.copy(transactionType = Some("Transfer from another account"), credit = Some(transferIn.amount))

    dailyStatement.toReportLayout(1) must be(transferExpectedRow)
  }

  "order the entries correctly within each day" in new Setup {

    val declarations: Seq[Declaration] = Seq(
      Declaration(
        "someMRN",
        Some("someImporterEORI"),
        "someEORI",
        None,
        LocalDate.of(year2020, month3, day3),
        1234.56,
        Nil,
        Some(sMRN)
      )
    )

    val expectedTransactionTypes: Seq[Some[String]] = Seq(
      Some("Closing balance"),
      Some("Declaration"),
      Some("Withdrawal"),
      Some("Transfer to another account"),
      Some("Top-up"),
      Some("Transfer from another account")
    )

    val orderedStatement: CashDailyStatement =
      dailyStatement.copy(declarations = declarations, otherTransactions = otherTransactions)

    orderedStatement.toReportLayout.map(_.transactionType) must be(expectedTransactionTypes)
  }

  trait Setup {

    val withdrawal: Transaction             = Transaction(-23.45, Withdrawal, None)
    val transferOut: Transaction            = Transaction(-23.45, Transfer, None)
    val topUp: Transaction                  = Transaction(23.45, Payment, None)
    val transferIn: Transaction             = Transaction(23.45, Transfer, None)
    val otherTransactions: Seq[Transaction] = Seq(withdrawal, transferOut, topUp, transferIn)

    val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"

    val amountZero = 0.0

    val year2020 = 2020
    val month3   = 3
    val day4     = 4
    val day3     = 3

    val fiveHundred: BigDecimal = BigDecimal(500.00)

    val dateWithDay3: LocalDate          = LocalDate.of(year2020, month3, day3)
    val dateWithDay4: LocalDate          = LocalDate.of(year2020, month3, day4)
    val dateForDailyStatement: LocalDate = LocalDate.of(year2020, month3, day4)

    val taxTypes: Seq[TaxType] = Seq(
      TaxType(
        reasonForSecurity = Some("Reason"),
        taxTypeID = "50",
        amount = fiveHundred
      )
    )

    val dailyStatement: CashDailyStatement =
      CashDailyStatement(dateForDailyStatement, amountZero, amountZero, Nil, Seq(transferIn))

    val expectedRow: CashTransactionCsvRow = CashTransactionCsvRow(
      date = Some("2020-03-04"),
      transactionType = None,
      movementReferenceNumber = None,
      uniqueConsignmentReference = None,
      importerEori = None,
      declarantEori = None,
      duty = None,
      vat = None,
      excise = None,
      credit = None,
      debit = None,
      balance = None
    )
  }
}
