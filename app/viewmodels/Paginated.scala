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

import models.{CashDailyStatement, Declaration}

import java.time.LocalDate

sealed trait PaginatedTransactions

case class PaginatedDailyStatement(dailyStatement: CashDailyStatement) extends PaginatedTransactions

case class PaginatedPendingDailyStatement(date: LocalDate, declarations: Seq[Declaration]) extends PaginatedTransactions

trait Paginated {

  val allItems: Seq[PaginatedTransactions]
  val itemsPerPage: Int
  val requestedPage: Int
  val urlForPage: Int => String

  private val FirstPage = 1
  private val FixedWidth = 5
  private val lookAhead = FixedWidth / 2
  private lazy val totalNumberOfItems: Int = allItems.length

  private lazy val lastPage = totalNumberOfItems % itemsPerPage match {
    case 0 => totalNumberOfItems / itemsPerPage
    case _ => totalNumberOfItems / itemsPerPage + 1
  }

  lazy val currentPage: Int = requestedPage.max(FirstPage).min(lastPage)

  lazy val isFirstPage: Boolean = currentPage == FirstPage
  lazy val isLastPage: Boolean = currentPage == lastPage

  lazy val dataFitsOnOnePage: Boolean = totalNumberOfItems <= itemsPerPage

  private lazy val firstItemOnPage: Int = (currentPage - 1) * itemsPerPage
  private lazy val lastItemOnPage: Int = totalNumberOfItems.min(currentPage * itemsPerPage)

  lazy val visibleItems: Seq[PaginatedTransactions] = allItems.slice(firstItemOnPage, lastItemOnPage)

  lazy val pageRange: IndexedSeq[Int] = {
    val range = if (currentPage <= lookAhead) {
      FirstPage to FixedWidth
    } else if (currentPage + lookAhead >= lastPage) {
      lastPage + 1 - FixedWidth to lastPage
    } else {
      currentPage - lookAhead to currentPage + lookAhead
    }
    range.filter(n => n >= FirstPage && n <= lastPage)
  }

}
