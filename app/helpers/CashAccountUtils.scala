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

package helpers

import config.AppConfig
import helpers.Formatters.{ddMMyyyyDateFormatter, yyyyMMddHHmmssDateFormatter}
import play.api.i18n.Messages
import services.DateTimeService

import java.time.LocalDate
import javax.inject.Inject

class CashAccountUtils @Inject() (dateTimeService: DateTimeService, appConfig: AppConfig) {

  def filenameWithDateTime()(implicit messages: Messages): String = {
    val formattedTime = yyyyMMddHHmmssDateFormatter.format(dateTimeService.localDateTime())
    messages("cf.cash-account.csv.filename", formattedTime)
  }

  def filenameRequestCashTransactions(from: LocalDate, to: LocalDate)(implicit messages: Messages): String =
    messages("cf.cash-account.requested.csv.filename", dateFormat(from), dateFormat(to))

  def makeHumanReadable(columnName: String)(implicit messages: Messages): String = {
    val messagePrefix = "cf.cash-account.csv"
    val messageKey    = s"$messagePrefix.$columnName"
    messages(messageKey)
  }

  private def dateFormat(date: LocalDate): String = ddMMyyyyDateFormatter.format(date)

  def transactionDateRange(): (LocalDate, LocalDate) = {
    val to   = dateTimeService.localDateTime().toLocalDate
    val from = to.minusMonths(appConfig.numberOfMonthsOfCashTransactionsToShow)
    (from, to)
  }
}
