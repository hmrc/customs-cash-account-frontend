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

import java.time.LocalDate

import models._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import utils.SpecBase
import viewmodels.CashTransactionCsvRow.DailyStatementCsvRowsViewModel

class CashTransactionCsvRowSpec extends SpecBase {

  val app = application.build()
  implicit val messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  "generate an opening balance" in new Setup {
    override val dailyStatement = CashDailyStatement(LocalDate.of(2020, 3, 4), 123.33, 0.0, Nil, Nil)
    val openingExepectedRow = expectedRow.copy(transactionType = Some("Opening balance"))
    dailyStatement.toReportLayout.last must be(openingExepectedRow)
  }

  "generate a closing balance" in new Setup {
    override val dailyStatement = CashDailyStatement(LocalDate.of(2020, 3, 4), 0.0, 12345.67, Nil, Nil)
    val closingExepectedRow = expectedRow.copy(transactionType = Some("Closing balance"))
    dailyStatement.toReportLayout.head must be(closingExepectedRow)
  }

  "generate a declaration row" in {
    val taxGroups = Seq(
      TaxGroup(ImportVat, -1.23),
      TaxGroup(CustomsDuty, -2.34),
      TaxGroup(ExciseDuty, -3.45))

    val declarations = Seq(Declaration("someMRN", Some("someImporterEORI"),
      "someEORI", None, LocalDate.of(2020, 3, 3), -1234.56, taxGroups))

    val dailyStatement = CashDailyStatement(LocalDate.of(2020, 3, 4), 0.0, 0.0, declarations, Nil)

    val expectedRow = CashTransactionCsvRow(
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
      credit = None
    )

    dailyStatement.toReportLayout(1) must be(expectedRow)
  }

  "order declaration rows by ascending MRN" in {
    val declarations = Seq(
      Declaration("someMRN2", Some("someImporterEORI"), "someEORI",
        None,LocalDate.of(2020, 3, 3), 1234.56, Nil),
      Declaration("someMRN3", Some("someImporterEORI"), "someEORI",
        None, LocalDate.of(2020, 3, 3), 1234.56, Nil),
      Declaration("someMRN1", Some("someImporterEORI"), "someEORI",
        None, LocalDate.of(2020, 3, 3), 1234.56, Nil))

    val dailyStatement = CashDailyStatement(LocalDate.of(2020, 3, 4), 0.0, 0.0, declarations, Nil)
    val expectedMrns = Seq(Some("someMRN1"), Some("someMRN2"), Some("someMRN3"))

    val actualMrns = dailyStatement.toReportLayout.filter(
      _.transactionType.contains("Declaration")).map(_.movementReferenceNumber)

    actualMrns must be(expectedMrns)
  }

  "default vat/duty/excise to zero if not found in the declaration" in {
    val taxGroups = Nil
    val declarations = Seq(
      Declaration("someMRN", Some("someImporterEORI"), "someEORI", None,
        LocalDate.of(2020, 3, 3), 1234.56, taxGroups))

    val dailyStatement = CashDailyStatement(LocalDate.of(2020, 3, 4), 0.0, 0.0, declarations, Nil)

    val expectedRow = CashTransactionCsvRow(
      date = Some("2020-03-04"),
      movementReferenceNumber = Some("someMRN"),
      transactionType = Some("Declaration"),
      uniqueConsignmentReference = None,
      importerEori = Some("someImporterEORI"),
      declarantEori = Some("someEORI"),
      duty = Some(0.0),
      vat = Some(0.0),
      excise = Some(0.0),
      debit = Some(1234.56),
      credit = None
    )

    dailyStatement.toReportLayout(1) must be(expectedRow)
  }

  "generate a withdrawal row" in new Setup {
    val withdraw = withdrawal.copy(bankAccountNumber = Some("12345678"))
    val withdrawalStatement = dailyStatement.copy(otherTransactions = Seq(withdraw))

    val withdrawalExepectedRow = expectedRow.copy(transactionType = Some(
      "Withdrawal (to account ending 5678)"), debit = Some(23.45))

    withdrawalStatement.toReportLayout(1) must be(withdrawalExepectedRow)
  }

  "generate a withdrawal row when there is no bank account number" in new Setup {
    val withdrawalStatement = dailyStatement.copy(otherTransactions = Seq(withdrawal))
    val withdrawalExepectedRow = expectedRow.copy(transactionType = Some("Withdrawal"), debit = Some(23.45))
    withdrawalStatement.toReportLayout(1) must be(withdrawalExepectedRow)
  }

  "generate a transfer out row" in new Setup {
    val transferStatement = dailyStatement.copy(otherTransactions = Seq(transferOut))

    val transferExepectedRow = expectedRow.copy(transactionType = Some(
      "Transfer to another account"), debit = Some(23.45))

    transferStatement.toReportLayout(1) must be(transferExepectedRow)
  }

  "generate a top-up row" in new Setup {
    val topUpStatement = dailyStatement.copy(otherTransactions = Seq(topUp))
    val topUpExepectedRow = expectedRow.copy(transactionType = Some(
      "Top-up"), credit = Some(topUp.amount))

    topUpStatement.toReportLayout(1) must be(topUpExepectedRow)
  }

  "generate a transfer in row" in new Setup {
    val transferExepectedRow = expectedRow.copy(transactionType = Some(
      "Transfer from another account"), credit = Some(transferIn.amount))

    dailyStatement.toReportLayout(1) must be(transferExepectedRow)
  }

  "order the entries correctly within each day" in new Setup {
    val declarations = Seq(Declaration("someMRN", Some("someImporterEORI"), "someEORI",
        None, LocalDate.of(2020, 3, 3), 1234.56, Nil))

    val expectedTransactionTypes = Seq(
      Some("Closing balance"),
      Some("Declaration"),
      Some("Withdrawal"),
      Some("Transfer to another account"),
      Some("Top-up"),
      Some("Transfer from another account"),
      Some("Opening balance")
    )

    val orderedStatement = dailyStatement.copy(declarations = declarations,otherTransactions = otherTransactions)
    orderedStatement.toReportLayout.map(_.transactionType) must be(expectedTransactionTypes)
  }

  trait Setup {
    val withdrawal = Transaction(-23.45, Withdrawal, None)
    val transferOut = Transaction(-23.45, Transfer, None)
    val topUp = Transaction(23.45, Payment, None)
    val transferIn = Transaction(23.45, Transfer, None)
    val otherTransactions = Seq(withdrawal, transferOut, topUp, transferIn)
    val dailyStatement = CashDailyStatement(LocalDate.of(2020, 3, 4), 0.0, 0.0, Nil, Seq(transferIn))

    val expectedRow = CashTransactionCsvRow(
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
      debit = None
    )
  }
}
