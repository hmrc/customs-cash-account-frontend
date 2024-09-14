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

import models.{CashDailyStatement, CashTransactionType, CashTransactions, Declaration, Payment, Transaction, Transfer, Withdrawal}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import helpers.Formatters.{dateAsDayMonthAndYear, formatCurrencyAmount}
import utils.Utils.{LinkComponentValues, emptyString, h2Component, linkComponent}

import java.time.LocalDate


case class PaymentType(mrnLink: Option[HtmlFormat.Appendable] = None,
                       textString: Option[String] = None)

case class DailyStatementViewModel(date: String,
                                   transactionType: PaymentType,
                                   credit: Option[String] = None,
                                   debit: Option[String] = None,
                                   balance: Option[String]) extends Ordered[DailyStatementViewModel] {
  override def compare(that: DailyStatementViewModel): Int = LocalDate.parse(that.date).compareTo(LocalDate.parse(date))
}

case class CashAccountDailyStatementsViewModel(dailyStatements: Seq[DailyStatementViewModel],
                                               hasTransactions: Boolean,
                                               transForLastSixMonthsHeading: HtmlFormat.Appendable)

object CashAccountDailyStatementsViewModel {
  def apply(transactions: CashTransactions)(implicit msgs: Messages): CashAccountDailyStatementsViewModel = {

    val pendingTransactions = transactions.pendingTransactions.sortBy(_.date).reverse
    val dailyStatements: Seq[CashDailyStatement] = transactions.cashDailyStatements.sortBy(_.date).reverse
    val hasTransactions = dailyStatements.nonEmpty

    val dailyStatementsForViewModel: Seq[DailyStatementViewModel] = populateDailyStatementViewModelList(dailyStatements)

    CashAccountDailyStatementsViewModel(dailyStatementsForViewModel, hasTransactions, transForLastSixMonthsHeading)
  }

  private def transForLastSixMonthsHeading(implicit msgs: Messages): HtmlFormat.Appendable = {
    h2Component(
      msgKey = "cf.cash-account.transactions.transactions-for-last-six-months.heading",
      id = Some("transactions-for-last-six-months-heading"))
  }

  private def populateDailyStatementViewModelList(dailyStatements: Seq[CashDailyStatement])
                                                 (implicit msgs: Messages): Seq[DailyStatementViewModel] = {

    val result: Seq[Seq[DailyStatementViewModel]] = dailyStatements.map {
      dStat =>
        val date = dStat.date

        val declarationDailyStatementViewModel: Seq[DailyStatementViewModel] =
          populateViewModelFromDeclarations(date, dStat.declarations)

        val declarationDailyStatementViewModelWithAccBalance = declarationDailyStatementViewModel.zipWithIndex.map {
          case (dStatViewModel, 0) => dStatViewModel.copy(balance = Some(formatCurrencyAmount(dStat.closingBalance)))
          case (dStatViewModel, _) => dStatViewModel
        }

        val transferAndWithdrawDailyStatementViewModel: Seq[DailyStatementViewModel] =
          populateViewModelFromPaymentAndWithdrawals(date, dStat.otherTransactions)

        transferAndWithdrawDailyStatementViewModel ++ declarationDailyStatementViewModelWithAccBalance.reverse
    }

    result.flatten.sortBy(_.date).reverse
  }

  private def populateViewModelFromDeclarations(date: LocalDate,
                                                declarations: Seq[Declaration])
                                               (implicit msgs: Messages): Seq[DailyStatementViewModel] = {
    declarations.map {
      declaration =>
        DailyStatementViewModel(
          date = dateAsDayMonthAndYear(date),
          transactionType = PaymentType(mrnLink = Some(linkComponent(
            LinkComponentValues(
              linkMessage = Some(declaration.movementReferenceNumber),
              location = controllers.routes.DeclarationDetailController.displayDetails(
                declaration.secureMovementReferenceNumber.getOrElse(emptyString), None).url)
          ))),
          debit = Some(formatCurrencyAmount(declaration.amount)),
          balance = None
        )
    }
  }

  private def populateViewModelFromPaymentAndWithdrawals(date: LocalDate,
                                                         paymentAndWithdrawals: Seq[Transaction])
                                                        (implicit msgs: Messages): Seq[DailyStatementViewModel] = {
    paymentAndWithdrawals.map {
      paymentAndWithdrawal =>
        DailyStatementViewModel(
          date = dateAsDayMonthAndYear(date),
          transactionType =
            PaymentType(textString = Some(populateTransactionTypeText(paymentAndWithdrawal))),
          credit = populateCreditAmount(paymentAndWithdrawal),
          debit = populateDebitAmount(paymentAndWithdrawal),
          balance = None
        )
    }
  }

  private def populateTransactionTypeText(transaction: Transaction)(implicit msgs: Messages) = {
    transaction.transactionType match {
      case Payment => msgs("cf.cash-account.detail.top-up.v2")
      case Withdrawal => msgs("cf.cash-account.detail.withdrawal")
      case Transfer =>
        if (transaction.amount < 0) {
          msgs("cf.cash-account.detail.transfer-out.v2", transaction.bankAccountNumber.getOrElse(emptyString))
        } else {
          msgs("cf.cash-account.detail.transfer-in.v2", transaction.bankAccountNumber.getOrElse(emptyString))
        }
    }
  }

  private def populateCreditAmount(transaction: Transaction): Option[String] = {
    transaction.transactionType match {
      case Payment => Some(formatCurrencyAmount(transaction.amount))
      case Withdrawal => None
      case Transfer =>
        if (transaction.amount < 0) {
          None
        } else {
          Some(formatCurrencyAmount(transaction.amount))
        }
    }
  }

  private def populateDebitAmount(transaction: Transaction): Option[String] = {
    transaction.transactionType match {
      case Payment => None
      case Withdrawal => Some(formatCurrencyAmount(transaction.amount))
      case Transfer =>
        if (transaction.amount < 0) {
          Some(formatCurrencyAmount(transaction.amount))
        } else {
          None
        }
    }
  }
}