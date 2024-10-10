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

import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import models.response.PaymentsWithdrawalsAndTransfer
import utils.Utils.{h2Component, pComponent}

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
      val credit = if (txn.amount > 0) Some(f"${txn.amount}%.2f") else None
      val debit = if (txn.amount < 0) Some(f"${-txn.amount}%.2f") else None
      val transactionType = Some(txn.`type`)

      DailyStatementViewModel(
        date = txn.valueDate,
        //transactionType = transactionType,
        credit = credit,
        debit = debit,
        balance = None
      )
    }
    result.sorted
  }

}
