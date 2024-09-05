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
import views.html.selected_transactions_view
import viewmodels.ResultsPageSummary

import java.time.LocalDate

class SelectedTransactionsViewSpec extends ViewTestHelper {

  "SelectedTransactionsView" should {

    "display correct information" when {
      "title is visible" in new Setup {
        titleShouldBeCorrect(view, "cf.cash-account.transactions.request.review.heading")
      }

      "backlink should take you back to request transactions" in new Setup {
        shouldContainBackLinkUrl(view, controllers.routes.SelectTransactionsController.onPageLoad().url)
      }

      "account number must be displayed" in new Setup {
        view.getElementById("cash-account.number").text() mustBe displayAccountNumberFormat
      }

      "header is correct" in new Setup {
        view.getElementsByTag("h1").text() mustBe messages(
          "cf.cash-account.transactions.request.review.heading")
      }
    }
  }

  trait Setup {

    val day10th = 10
    val day11th = 11
    val month = 3
    val year = 2022

    val fromDate: LocalDate = LocalDate.of(year, month, day10th)
    val toDate: LocalDate = LocalDate.of(year, month, day11th)

    val summary: ResultsPageSummary = new ResultsPageSummary(fromDate, toDate)
    val returnLink: String = "some return link"
    val accountNumber: String = "some account number"
    val displayAccountNumberFormat: String = s"Account: $accountNumber"

    val view: Document =
      Jsoup.parse(app.injector.instanceOf[selected_transactions_view].apply(
        summary, returnLink, accountNumber).body)
  }
}
