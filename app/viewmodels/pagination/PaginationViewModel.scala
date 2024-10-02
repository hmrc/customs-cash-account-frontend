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

package viewmodels.pagination

import config.AppConfig
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}

trait PaginationViewModel {

  val results: MetaData
  val previous: Option[PaginationLink]
  val next: Option[PaginationLink]
  val items: Seq[PaginationItem]
  val pageNumber: Int

  def searchResult(searchParam: Option[String] = None)(implicit messages: Messages): String =
    (searchParam, results.count) match {
      case (Some(value), 1) => messages("cf.cash-account.number-of-movements.singular.with-search-param", "<b>1</b>", value)
      case (Some(value), x) => messages("cf.cash-account.number-of-movements.plural.with-search-param", s"<b>$x</b>", value)
      case (None, 1) => messages("cf.cash-account.number-of-movements.singular", "<b>1</b>")
      case (None, x) => messages("cf.cash-account.number-of-movements.plural", s"<b>$x</b>")
    }

  def paginatedSearchResult(searchParam: Option[String] = None)(implicit messages: Messages): String =
    searchParam match {
      case Some(value) =>
        messages("pagination.results.search", s"<b>${results.from}</b>", s"<b>${results.to}</b>", s"<b>${results.count}</b>", value)

      case None =>
        messages("pagination.results", s"<b>${results.from}</b>", s"<b>${results.to}</b>", s"<b>${results.count}</b>")
    }

  val pagination: Pagination = Pagination(Some(items), previous, next)
}

object PaginationViewModel {

  def apply[T <: PaginationViewModel](totalNumberOfItems: Int,
                                      currentPage: Int,
                                      numberOfItemsPerPage: Int,
                                      href: String,
                                      additionalParams: Seq[(String, String)])
                                     (f: (MetaData, Option[PaginationLink], Option[PaginationLink], Seq[PaginationItem]) => T)
                                     (implicit config: AppConfig): T = {

    val results: MetaData = MetaData(totalNumberOfItems, numberOfItemsPerPage, currentPage)
    val isTotalNumberOfItemsMoreThanTheLimit: Boolean =
      totalNumberOfItems <= config.numberOfRecordsToDisableNavigationButtonsInPagination

    val hrefWithParams: Int => String = page => additionalParams.foldLeft(s"$href?page=$page") {
      case (href, (key, value)) => href + s"&$key=$value"
    }

    val previous: Option[PaginationLink] = if (isTotalNumberOfItemsMoreThanTheLimit) {
      None
    } else {
      previousLinkForItemsGreaterThanAcceptedLimit(currentPage, hrefWithParams(currentPage - 1))
    }

    val next: Option[PaginationLink] = if (isTotalNumberOfItemsMoreThanTheLimit) {
      None
    } else {
      nextLinkForItemsGreaterThanAcceptedLimit(results.totalPages, currentPage, hrefWithParams(currentPage + 1))
    }

    val items = (1 to results.totalPages).foldLeft[Seq[PaginationItem]](Nil) {
      (acc, page) =>
        if (isTotalNumberOfItemsMoreThanTheLimit) {
          acc :+ PaginationItem(href = hrefWithParams(page), number = Some(page.toString), current = Some(page == currentPage))
        } else {
          if (page == 1 || (page >= currentPage - 1 && page <= currentPage + 1) || page == results.totalPages) {
            acc :+ PaginationItem(href = hrefWithParams(page), number = Some(page.toString), current = Some(page == currentPage))
          } else if (acc.lastOption.flatMap(_.ellipsis).contains(true)) {
            acc
          } else {
            acc :+ PaginationItem(ellipsis = Some(true))
          }
        }
    }

    f(results, previous, next, items)
  }

  private def previousLinkForItemsGreaterThanAcceptedLimit[T <: PaginationViewModel](currentPage: Int,
                                                                                     hrefValue: String) = {
    if (currentPage > 1) {
      Some(PaginationLink(hrefValue))
    } else {
      None
    }
  }

  private def nextLinkForItemsGreaterThanAcceptedLimit[T <: PaginationViewModel](totalPages: Int,
                                                                                 currentPage: Int,
                                                                                 hrefValue: String) = {
    if (currentPage < totalPages) {
      Some(PaginationLink(hrefValue))
    } else {
      None
    }
  }
}
