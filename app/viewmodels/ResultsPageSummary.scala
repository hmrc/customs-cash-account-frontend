/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{CashTransactionDates, RequestedDateRange}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions

import java.time.LocalDate

class ResultsPageSummary(from: LocalDate, to: LocalDate)(implicit messages: Messages) extends SummaryListRowHelper {

  def rows:SummaryListRow = {
    cashTransactionsResultRow(CashTransactionDates(from, to))
  }

  def cashTransactionsResultRow(dates: CashTransactionDates): SummaryListRow = {
      summaryListRow(
        value = HtmlFormat.escape(
          messages("date.range",
            dateAsMonthAndYear(dates.start),
            dateAsMonthAndYear(dates.end))
        ).toString,
        secondValue = None,
        actions = Actions(items = Seq(ActionItem(
          href = controllers.routes.DownloadCsvController.downloadRequestedCsv(None, RequestedDateRange(dates.start,dates.end)).url,
          content = span(messages("cf.cash-account.detail.csv")),
          visuallyHiddenText = Some(messages("cf.cash-account.detail.csv-definition"))
        ))))
  }

  def dateAsMonthAndYear(date: LocalDate)(implicit messages: Messages): String = s"${dateAsMonth(date)} ${date.getYear}"

  def dateAsMonth(date: LocalDate)(implicit messages: Messages): String = messages(s"month.${date.getMonthValue}")
}
