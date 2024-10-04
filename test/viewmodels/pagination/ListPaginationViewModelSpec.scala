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
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem}
import utils.TestData.{
  HREF, PAGE_4, PAGE_10, PAGE_460, PAGE_5, PAGE_451, PAGE_600, PAGE_880, PAGE_230, PAGE_80, PAGE_450,
  PAGE_300, PAGE_100, PAGE_200, PAGE_800, PAGE_1000, PAGE_650, PAGE_98, PAGE_99, PAGE_30, PAGE_720
}

class ListPaginationViewModelSpec extends SpecBase {

  "apply.next" should {

    "return some value when total items are more than 450 and" +
      " current page is less than the total number of pages" in new Setup {
      ListPaginationViewModel(PAGE_460, 2, 2, HREF).next.isDefined mustBe true
    }

    "return None when current page is not less than the total number of pages" in new Setup {
      ListPaginationViewModel(PAGE_10, PAGE_5, 2, HREF).next.isDefined mustBe false
    }

    "return None when no of records is equal to or less than 450" in new Setup {
      ListPaginationViewModel(PAGE_10, PAGE_5, 2, HREF).next.isDefined mustBe false
      ListPaginationViewModel(PAGE_450, PAGE_5, 2, HREF).next.isDefined mustBe false
    }

    "return some value when no of records is greater than 450" in new Setup {
      ListPaginationViewModel(PAGE_451, PAGE_5, 2, HREF).next.isDefined mustBe true
      ListPaginationViewModel(PAGE_600, PAGE_230, 2, HREF).next.isDefined mustBe true
      ListPaginationViewModel(PAGE_880, PAGE_80, 2, HREF).next.isDefined mustBe true
    }
  }

  "apply.previous" should {

    "return some value when total items are more than 450 and current page is greater than 1" in new Setup {
      ListPaginationViewModel(PAGE_720, PAGE_100, 2, HREF).previous.isDefined mustBe true
    }

    "return None when total items are more than 450 and current page is not greater than 1" in new Setup {
      ListPaginationViewModel(PAGE_650, 1, 2, HREF).previous.isDefined mustBe false
    }

    "return None when no of records is equal to or less than 450" in new Setup {
      ListPaginationViewModel(PAGE_10, 1, 2, HREF).previous.isDefined mustBe false
      ListPaginationViewModel(PAGE_450, PAGE_100, 2, HREF).previous.isDefined mustBe false
      ListPaginationViewModel(PAGE_300, PAGE_200, 2, HREF).previous.isDefined mustBe false
    }

    "return some value when no of records is greater than 450" in new Setup {
      ListPaginationViewModel(PAGE_451, PAGE_100, 2, HREF).previous.isDefined mustBe true
      ListPaginationViewModel(PAGE_800, PAGE_5, 2, HREF).previous.isDefined mustBe true
    }
  }

  "apply.items" should {
    "return [1] 2 … 100 when on page 1 of 100" in new Setup {
      val result: Seq[PaginationItem] = ListPaginationViewModel(PAGE_1000, 1, PAGE_10, "href").items

      result mustBe Seq(
        PaginationItem(s"href?page=1", Some("1"), current = Some(true)),
        PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
        PaginationItem("", ellipsis = Some(true)),
        PaginationItem(s"href?page=100", Some("100"), current = Some(false))
      )
    }

    "return 1 [2] 3 … 100 when on page 2 of 100" in new Setup {
      val result: Seq[PaginationItem] = ListPaginationViewModel(PAGE_1000, 2, PAGE_10, "href").items

      result mustBe Seq(
        PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
        PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
        PaginationItem(s"href?page=3", Some("3"), current = Some(false)),
        PaginationItem("", ellipsis = Some(true)),
        PaginationItem(s"href?page=100", Some("100"), current = Some(false))
      )
    }

    "return 1 2 [3] 4 … 100 when on page 3 of 100" in new Setup {
      val result: Seq[PaginationItem] = ListPaginationViewModel(PAGE_1000, 3, PAGE_10, "href").items

      result mustBe Seq(
        PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
        PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
        PaginationItem(s"href?page=3", Some("3"), current = Some(true)),
        PaginationItem(s"href?page=4", Some("4"), current = Some(false)),
        PaginationItem("", ellipsis = Some(true)),
        PaginationItem(s"href?page=100", Some("100"), current = Some(false))
      )
    }

    "return 1 … 3 [4] 5 … 100 when on page 4 of 100" in new Setup {
      val result: Seq[PaginationItem] = ListPaginationViewModel(PAGE_1000, PAGE_4, PAGE_10, "href").items

      result mustBe Seq(
        PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
        PaginationItem("", ellipsis = Some(true)),
        PaginationItem(s"href?page=3", Some("3"), current = Some(false)),
        PaginationItem(s"href?page=4", Some("4"), current = Some(true)),
        PaginationItem(s"href?page=5", Some("5"), current = Some(false)),
        PaginationItem("", ellipsis = Some(true)),
        PaginationItem(s"href?page=100", Some("100"), current = Some(false))
      )
    }

    "return 1 … 97 [98] 99 100 when on page 98 of 100" in new Setup {
      val result: Seq[PaginationItem] = ListPaginationViewModel(PAGE_1000, PAGE_98, PAGE_10, "href").items

      result mustBe Seq(
        PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
        PaginationItem("", ellipsis = Some(true)),
        PaginationItem(s"href?page=97", Some("97"), current = Some(false)),
        PaginationItem(s"href?page=98", Some("98"), current = Some(true)),
        PaginationItem(s"href?page=99", Some("99"), current = Some(false)),
        PaginationItem(s"href?page=100", Some("100"), current = Some(false))
      )
    }

    "return 1 … 98 [99] 100 when on page 99 of 100" in new Setup {
      val result: Seq[PaginationItem] = ListPaginationViewModel(PAGE_1000, PAGE_99, PAGE_10, "href").items

      result mustBe Seq(
        PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
        PaginationItem("", ellipsis = Some(true)),
        PaginationItem(s"href?page=98", Some("98"), current = Some(false)),
        PaginationItem(s"href?page=99", Some("99"), current = Some(true)),
        PaginationItem(s"href?page=100", Some("100"), current = Some(false))
      )
    }

    "return 1 … 99 [100] when on page 100 of 100" in new Setup {
      val result: Seq[PaginationItem] = ListPaginationViewModel(PAGE_1000, PAGE_100, PAGE_10, "href").items

      result mustBe Seq(
        PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
        PaginationItem("", ellipsis = Some(true)),
        PaginationItem(s"href?page=99", Some("99"), current = Some(false)),
        PaginationItem(s"href?page=100", Some("100"), current = Some(true))
      )
    }

    "return 1 [2] 3 when on page 2 of 3" in new Setup {
      val result: Seq[PaginationItem] = ListPaginationViewModel(PAGE_30, 2, PAGE_10, "href").items

      result mustBe Seq(
        PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
        PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
        PaginationItem(s"href?page=3", Some("3"), current = Some(false))
      )
    }
  }

  "searchResult" should {

    "return correct output" when {

      "searchParam has some value" in new Setup {
        ListPaginationViewModel(PAGE_30, 2, 1, "href").searchResult(Some("2")) mustBe
          msgs("pagination.number-of-movements.plural.with-search-param", "<b>30</b>", "2")
      }

      "searchParam has some value and results count is 1" in new Setup {
        ListPaginationViewModel(1, 2, 1, "href").searchResult(Some("2")) mustBe
          msgs("pagination.number-of-movements.singular.with-search-param", "<b>1</b>", "2")
      }

      "searchParam is None" in new Setup {
        ListPaginationViewModel(PAGE_30, 2, 1, "href").searchResult() mustBe
          msgs("pagination.number-of-movements.plural", "<b>30</b>")
      }

      "searchParam is None and results count is 1" in new Setup {
        ListPaginationViewModel(1, 2, 1, "href").searchResult() mustBe
          msgs("pagination.number-of-movements.singular", "<b>1</b>")
      }
    }
  }

  "paginatedSearchResult" should {

    "return correct output" when {

      "searchParam has some value" in new Setup {
        ListPaginationViewModel(PAGE_30, 2, 1, "href").paginatedSearchResult(Some("2")) mustBe
          msgs("pagination.results.search", "<b>2</b>", "<b>2</b>", "<b>30</b>", "2")
      }

      "searchParam is None" in new Setup {
        ListPaginationViewModel(PAGE_30, 2, 1, "href").paginatedSearchResult() mustBe
          msgs("pagination.results", "<b>2</b>", "<b>2</b>", "<b>30</b>")
      }
    }
  }

  "pagination" should {

    "return correct output" in new Setup {
      ListPaginationViewModel(2, 2, 1, "href").pagination mustBe
        Pagination(Some(
          Seq(
            PaginationItem("href?page=1", Some("1"), None, Some(false), None, Map()),
            PaginationItem("href?page=2", Some("2"), None, Some(true), None, Map()))
        ))
    }
  }

  trait Setup {
    val app: Application = application.build()
    implicit val config: AppConfig = appConfig(app)
    implicit val msgs: Messages = messages(app)
  }

}
