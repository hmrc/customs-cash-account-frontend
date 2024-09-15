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

import models.{CashTransactionDates, RequestedDateRange}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import utils.Utils.{emptyString, period}

import java.time.LocalDate

class ResultsPageSummary(from: LocalDate, to: LocalDate, day: Boolean = true)
                        (implicit messages: Messages) extends SummaryListRowHelper {

  def rows(fullStop: Boolean = true): SummaryListRow = {
    cashTransactionsResultRow(CashTransactionDates(from, to), fullStop, day)
  }

  private def cashTransactionsResultRow(dates: CashTransactionDates,
                                        fullStop: Boolean,
                                        day: Boolean): SummaryListRow = {
    summaryListRow(
      value = HtmlFormat.escape(
        if (fullStop) {
          rowResult(dates, day)
        } else {
          rowResultWithoutFullStop(dates, day)
        }).toString(),
      secondValue = None,
      actions = Actions(items = Seq(ActionItem(
        href = controllers.routes.DownloadCsvController.downloadRequestedCsv(
          None, RequestedDateRange(dates.start, dates.end)).url,
        content = span(messages("cf.cash-account.detail.csv")),
        visuallyHiddenText = Some(messages("cf.cash-account.detail.csv-definition"))
      )))

    )
  }

  private def rowResult(dates: CashTransactionDates, day: Boolean): String = {
    messages("date.range",
      formatDate(dates.start, day),
      formatDate(dates.end, day))
  }

  private def rowResultWithoutFullStop(dates: CashTransactionDates, day: Boolean): String = {
    messages("date.range",
      formatDate(dates.start, day),
      formatDate(dates.end, day)).replace(period, emptyString)
  }

  def formatDate(date: LocalDate, day: Boolean)(implicit messages: Messages): String =
    if (day) {
      s"${dateAsDay(date)} ${dateAsMonth(date)} ${date.getYear}"
    } else {
      s"${dateAsMonth(date)} ${date.getYear}"
    }


  def dateAsDay(date: LocalDate): String = {
    val dayTenth = 10

    if (date.getDayOfMonth >= dayTenth) {
      s"${date.getDayOfMonth}"
    }
    else {
      s"0${date.getDayOfMonth}"
    }
  }

  def dateAsMonth(date: LocalDate)(implicit messages: Messages): String = {
    messages(s"month.${date.getMonthValue}")
  }
}
