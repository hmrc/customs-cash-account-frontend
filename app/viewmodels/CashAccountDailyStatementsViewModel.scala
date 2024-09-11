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

import models.{CashDailyStatement, CashTransactionType, CashTransactions, Declaration, Transaction, Payment, Withdrawal, Transfer}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import helpers.Formatters.{dateAsDayMonthAndYear, formatCurrencyAmount}
import utils.Utils.{LinkComponentValues, linkComponent, emptyString}

import java.time.LocalDate


case class PaymentType(mrnLink: Option[HtmlFormat.Appendable] = None,
                       textString: Option[String] = None)

case class DailyStatementViewModel(date: String,
                                   transactionType: PaymentType,
                                   credit: Option[String] = None,
                                   debt: Option[String] = None,
                                   balance: Option[String]) extends Ordered[DailyStatementViewModel] {
  override def compare(that: DailyStatementViewModel): Int = LocalDate.parse(that.date).compareTo(LocalDate.parse(date))
}

case class CashAccountDailyStatementsViewModel(dailyStatements: Seq[DailyStatementViewModel],
                                               hasTransactions: Boolean)

object CashAccountDailyStatementsViewModel {
  def apply(transactions: CashTransactions)(implicit msgs: Messages): CashAccountDailyStatementsViewModel = {

    val pendingTransactions = transactions.pendingTransactions.sortBy(_.date).reverse
    val dailyStatements: Seq[CashDailyStatement] = transactions.cashDailyStatements.sortBy(_.date).reverse
    val hasTransactions = dailyStatements.nonEmpty

    val dailyStatementsForViewModel: Seq[DailyStatementViewModel] = populateDailyStatementViewModelList(dailyStatements)

    CashAccountDailyStatementsViewModel(dailyStatementsForViewModel, hasTransactions)
  }

  private def populateDailyStatementViewModelList(dailyStatements: Seq[CashDailyStatement])
                                                 (implicit msgs: Messages): Seq[DailyStatementViewModel] = {

    val result: Seq[Seq[DailyStatementViewModel]] = dailyStatements.map {
      dStat =>
        val date = dStat.date

        val testDeclarationViewModel: Seq[DailyStatementViewModel] =
          populateViewModelFromDeclarations(date, dStat.declarations)

        val testTransferAndWithdrawModel: Seq[DailyStatementViewModel] =
          populateViewModelFromPaymentAndWithdrawals(date, dStat.otherTransactions)

        testDeclarationViewModel ++ testTransferAndWithdrawModel
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
              linkMessageKey = "cf-cash-account.tbd",
              controllers.routes.DeclarationDetailController.displayDetails(
                declaration.secureMovementReferenceNumber.getOrElse(emptyString), None).url)
          ))),
          debt = Some(formatCurrencyAmount(declaration.amount)),
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
          debt = populateDebitAmount(paymentAndWithdrawal),
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
