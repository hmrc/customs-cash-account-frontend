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
import models.{CashDailyStatement, CashTransactions}

import java.time.LocalDate
import java.time.chrono.ChronoLocalDate

case class CashTransactionsViewModel(cashTransactions: CashTransactions, page: Option[Int])(
  implicit appConfig: AppConfig) extends Paginated {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(identity[ChronoLocalDate])

  val downloadUrl: String = controllers.routes.DownloadCsvController.downloadCsv(None).url

  val pendingTransactionsGroupedByDate: Seq[PaginatedPendingDailyStatement] = {
    val pendingGroupedByDate = cashTransactions.pendingTransactions.groupBy(_.date).toSeq
    val sortedGroupedByDate = pendingGroupedByDate.sortBy(_._1).reverse

    sortedGroupedByDate.map {
      case (date, declarations) => PaginatedPendingDailyStatement(date, declarations)
    }
  }

  override val allItems: Seq[PaginatedTransactions] =
    pendingTransactionsGroupedByDate ++ cashTransactions.cashDailyStatements.sorted.map(PaginatedDailyStatement)

  override val itemsPerPage: Int = appConfig.numberOfDaysToShow
  override val requestedPage: Int = page.getOrElse(1)
  override val urlForPage: Int => String = e => controllers.routes.CashAccountController.showAccountDetails(Some(e)).url
}

object CashTransactionsViewModel {

  implicit class CashDailyStatementViewModel(cashDailyStatement: CashDailyStatement) {
    private val numberOfBalanceRows = 2

    val size: Int = cashDailyStatement.declarations.size +
      cashDailyStatement.topUps.size +
      cashDailyStatement.withdrawals.size +
      cashDailyStatement.transfersIn.size +
      cashDailyStatement.transfersOut.size +
      numberOfBalanceRows
  }

}
