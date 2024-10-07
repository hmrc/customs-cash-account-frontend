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
      case (Some(value), 1) =>
        messages("pagination.number-of-movements.singular.with-search-param", "<strong>1</strong>", value)

      case (Some(value), x) =>
        messages("pagination.number-of-movements.plural.with-search-param", s"<strong>$x</strong>", value)

      case (None, 1) => messages("pagination.number-of-movements.singular", "<strong>1</strong>")
      case (None, x) => messages("pagination.number-of-movements.plural", s"<strong>$x</strong>")
    }

  def paginatedSearchResult(searchParam: Option[String] = None)(implicit messages: Messages): String =
    searchParam match {
      case Some(value) =>
        messages("pagination.results.search",
          s"<strong>${results.from}</strong>",
          s"<strong>${results.to}</strong>",
          s"<strong>${results.count}</strong>",
          value)

      case None =>
        messages("pagination.results",
          s"<strong>${results.from}</strong>",
          s"<strong>${results.to}</strong>",
          s"<strong>${results.count}</strong>")
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
    val isNumberOfItemsInsideLimit = totalNumberOfItems <= config.numberOfRecordsToDisableNavigationButtonsInPagination

    val hrefWithParams: Int => String = page => additionalParams.foldLeft(s"$href?page=$page") {
      case (href, (key, value)) => href + s"&$key=$value"
    }

    val previous: Option[PaginationLink] = if (isNumberOfItemsInsideLimit) {
      None
    } else {
      previousLinkForItemsGreaterThanAcceptedLimit(currentPage, hrefWithParams(currentPage - 1))
    }

    val next: Option[PaginationLink] = if (isNumberOfItemsInsideLimit) {
      None
    } else {
      nextLinkForItemsGreaterThanAcceptedLimit(results.totalPages, currentPage, hrefWithParams(currentPage + 1))
    }

    def populateItems(acc: Seq[PaginationItem], pageNo: Int): Seq[PaginationItem] = {
      (acc, pageNo) match {
        case (acc, page) if page == 1 || (page >= currentPage - 1 && page <= currentPage + 1) || page == results.totalPages =>
          acc :+ PaginationItem(
            href = hrefWithParams(page), number = Some(page.toString), current = Some(page == currentPage))

        case (acc, _) if (acc.lastOption.flatMap(_.ellipsis).contains(true)) => acc

        case _ => acc :+ PaginationItem(ellipsis = Some(true))
      }
    }

    val items = (1 to results.totalPages).foldLeft[Seq[PaginationItem]](Nil) {
      (acc, page) =>
        if (isNumberOfItemsInsideLimit) {
          acc :+ PaginationItem(
            href = hrefWithParams(page), number = Some(page.toString), current = Some(page == currentPage))
        } else {
          populateItems(acc, page)
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
