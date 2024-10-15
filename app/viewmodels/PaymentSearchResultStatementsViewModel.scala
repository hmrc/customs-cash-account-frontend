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

import config.AppConfig
import helpers.Formatters.{dateAsDayMonthAndYear, formatCurrencyAmount, yyyyMMddDateFormatter}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import utils.Utils.{h2Component, pComponent, prependNegativeSignWithAmount}
import models.response
import models.response.PaymentsWithdrawalsAndTransfer

import java.time.LocalDate

case class PaymentSearchResultStatementsViewModel(dailyStatements: Seq[DailyStatementViewModel],
                                                  hasTransactions: Boolean,
                                                  transForLastSixMonthsHeading: HtmlFormat.Appendable,
                                                  noTransFromLastSixMonthsText: Option[HtmlFormat.Appendable] = None)

object PaymentSearchResultStatementsViewModel {

  def apply(transactions: Seq[PaymentsWithdrawalsAndTransfer],
            pageNo: Option[Int])
           (implicit msgs: Messages, config: AppConfig): PaymentSearchResultStatementsViewModel = {

    val dailyStatements: Seq[PaymentsWithdrawalsAndTransfer] = transactions.sortBy(_.valueDate).reverse
    val hasTransactions = dailyStatements.nonEmpty

    PaymentSearchResultStatementsViewModel(
      dailyStatementsBasedOnPageNoForPagination(
        populateDailyStatementViewModelList(dailyStatements), pageNo, config.numberOfRecordsPerPage),
      hasTransactions,
      transForLastSixMonthsHeading,
      if (hasTransactions) None else Some(noTransFromLastSixMonthsText))
  }

  private def dailyStatementsBasedOnPageNoForPagination(statements: Seq[DailyStatementViewModel],
                                                        pageNo: Option[Int],
                                                        maxItemPerPage: Int): Seq[DailyStatementViewModel] = {
    pageNo match {
      case None => statements
      case Some(pageNoValue) =>
        if (pageNoValue == 1) {
          statements.slice(0, maxItemPerPage)
        } else {
          statements.slice((pageNoValue - 1) * maxItemPerPage, pageNoValue * maxItemPerPage)
        }
    }
  }

  private def transForLastSixMonthsHeading(implicit msgs: Messages): HtmlFormat.Appendable = {
    h2Component(
      msgKey = "cf.cash-account.transactions.transactions-for-last-six-months.heading",
      id = Some("transactions-for-last-six-months-heading"))
  }

  private def noTransFromLastSixMonthsText(implicit msgs: Messages): HtmlFormat.Appendable = {
    pComponent(
      messageKey = "cf.cash-account.transactions.no-transactions-for-last-six-months",
      id = Some("no-transactions-for-last-six-months-text"))
  }

  private def populateDailyStatementViewModelList(dailyStatements: Seq[PaymentsWithdrawalsAndTransfer])
                                                 (implicit msgs: Messages): Seq[DailyStatementViewModel] = {
    val result: Seq[DailyStatementViewModel] = dailyStatements.map { txn =>
      DailyStatementViewModel(
        date = LocalDate.parse(txn.valueDate, yyyyMMddDateFormatter),
        transactionType = Some(PaymentType(textString = Some(populateTransactionTypeText(txn)))),
        credit = populateCreditAmount(txn),
        debit = populateDebitAmount(txn),
        balance = None
      )
    }
    result.sorted
  }

  private def populateTransactionTypeText(valueObject: PaymentsWithdrawalsAndTransfer)(implicit msgs: Messages) = {
    valueObject.`type` match {
      case response.PaymentType.Payment => msgs("cf.cash-account.detail.top-up.v2")
      case response.PaymentType.Withdrawal => msgs("cf.cash-account.detail.withdrawal")
      case response.PaymentType.Transfer => msgs("cf.cash-account.detail.transfer.v2")
    }
  }

  private def populateCreditAmount(valueObject: PaymentsWithdrawalsAndTransfer): Option[String] = {
    valueObject.`type` match {
      case response.PaymentType.Payment => Some(formatCurrencyAmount(valueObject.amount))
      case response.PaymentType.Withdrawal => None
      case response.PaymentType.Transfer =>
        if (valueObject.amount < 0) {
          None
        } else {
          Some(formatCurrencyAmount(valueObject.amount))
        }
    }
  }

  private def populateDebitAmount(valueObject: PaymentsWithdrawalsAndTransfer): Option[String] = {
    valueObject.`type` match {
      case response.PaymentType.Payment => None
      case response.PaymentType.Withdrawal => Some(prependNegativeSignWithAmount(formatCurrencyAmount(valueObject.amount)))
      case response.PaymentType.Transfer =>
        if (valueObject.amount < 0) {
          Some(prependNegativeSignWithAmount(formatCurrencyAmount(valueObject.amount)))
        } else {
          None
        }
    }
  }

}
