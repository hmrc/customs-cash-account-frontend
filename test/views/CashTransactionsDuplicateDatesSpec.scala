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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.cash_transactions_duplicate_dates

import java.time.Clock

class CashTransactionsDuplicateDatesSpec extends ViewTestHelper {

  "view" should {

    "display correct information" when {

      "title is visible" in new Setup {
        titleShouldBeCorrect(view, "cf.cash-account.duplicate.header")
      }

      "backlink should take you back to request transactions" in new Setup {
        shouldContainBackLinkUrl(view, controllers.routes.SelectTransactionsController.onPageLoad().url)
      }

      "header is correct" in new Setup {
        view.getElementsByTag("h1").text() mustBe messages("cf.cash-account.duplicate.header")
      }

      "label is correct" in new Setup {
        val msg: String = messages("cf.cash-account.duplicate.message", "Jan 2021", "Feb 2021")
        view.getElementById("duplicate-date-label").text() mustBe msg
      }

      "link is correct" in new Setup {
        view.getElementById("duplicate-date-link").text() mustBe s"${messages("cf.cash-account.duplicate.link")}"
      }
    }
  }

  trait Setup {
    implicit val clk: Clock = Clock.systemUTC()

    val displayedMsg = "You requested transactions from Jan 2021 to Feb 2021"
    val startDate = "Jan 2021"
    val endDate = "Feb 2021"

    val view: Document = Jsoup.parse(
      app.injector.instanceOf[cash_transactions_duplicate_dates].apply(
        displayedMsg, startDate, endDate).body)
  }
}
