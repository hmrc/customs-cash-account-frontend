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

package views.components

import models.CashDailyStatement
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.Assertion
import viewmodels.{Paginated, PaginatedDailyStatement, PaginatedTransactions}
import views.ViewTestHelper
import views.html.components.pager

import java.time.LocalDate

class PagerSpec extends ViewTestHelper {

  "view" should {

    "display correct contents" when {

      "data does not fit in one page" in new Setup {

        val model: Paginated = new Paginated {
          override val allItems: Seq[PaginatedTransactions] =
            Seq(paginatedTransactions, paginatedTransactions, paginatedTransactions)

          override val itemsPerPage: Int = 2
          override val requestedPage: Int = 1
          override val urlForPage: Int => String =
            e => controllers.routes.CashAccountController.showAccountDetails(Some(e)).url
        }

        implicit val view: Document = viewDoc(model)

        shouldContainPaginationNavigationMsg
        shouldContainLinkToNextPageSection
      }
    }

    "data fits in one page" in new Setup {

      val model: Paginated = new Paginated {
        override val allItems: Seq[PaginatedTransactions] = Seq(paginatedTransactions)
        override val itemsPerPage: Int = 1
        override val requestedPage: Int = 1
        override val urlForPage: Int => String =
          e => controllers.routes.CashAccountController.showAccountDetails(Some(e)).url
      }

      implicit val view: Document = viewDoc(model)

      shouldNotContainNavigationLinks
    }
  }

  trait Setup {
    val date: LocalDate = LocalDate.parse("2020-08-05")
    val amount = 100.0

    val cashDailyStatement: CashDailyStatement =
      CashDailyStatement(date, BigDecimal(amount), BigDecimal(amount), Nil, Nil)

    val paginatedTransactions: PaginatedDailyStatement = PaginatedDailyStatement(cashDailyStatement)

    def viewDoc(model: Paginated): Document = Jsoup.parse(app.injector.instanceOf[pager].apply(model).body)
  }

  private def shouldContainPaginationNavigationMsg(implicit view: Document): Assertion =
    view.getElementsByClass("govuk-visually-hidden").text().contains("Pagination navigation") mustBe true

  private def shouldContainLinkToNextPageSection(implicit view: Document): Assertion = {
    val elements: Elements = view.getElementsByClass("govuk-pagination__next")
    val linkElement = elements.get(0).html()

    linkElement.contains(messages("cf.pager.next")) mustBe true
    linkElement.contains(messages("cf.pager.summary.accessibility")) mustBe true

    Option(view.getElementsByClass(
      "govuk-pagination__icon govuk-pagination__icon--next")) must not be empty
  }

  private def shouldNotContainNavigationLinks(implicit view: Document): Assertion = {
    Option(view.getElementsByClass("govuk-!-padding-bottom-9")) must not be empty

    view.html().contains("govuk-pagination__next") mustBe false
  }

}
