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

package helpers

import config.AppConfig

import java.time.LocalDate
import play.api.i18n.Messages
import services.DateTimeService

import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CashAccountUtils @Inject()(dateTimeService: DateTimeService, appConfig: AppConfig){

  def filenameWithDateTime()(implicit messages: Messages): String = {
    val formattedTime = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(dateTimeService.localDateTime())
    messages("cf.cash-account.csv.filename", formattedTime)
  }

  def filenameRequestCashTransactions(from:LocalDate, to:LocalDate)(implicit messages: Messages): String = {
    messages("cf.cash-account.requested.csv.filename", dateAsMonthAndYear(from) , dateAsMonthAndYear(to))
  }

  def makeHumanReadable(columnName: String)(implicit messages: Messages): String = {
    val messagePrefix = "cf.cash-account.csv"
    val messageKey = s"$messagePrefix.$columnName"
    messages(messageKey)
  }

  def dateAsMonthAndYear(date: LocalDate)(implicit messages: Messages): String = DateTimeFormatter.ofPattern("yyyyMM").format(date)

  def transactionDateRange(): (LocalDate, LocalDate) = {
    val to = dateTimeService.localDateTime().toLocalDate
    val from = to.minusMonths(appConfig.numberOfMonthsOfCashTransactionsToShow)
    (from, to)
  }
}
