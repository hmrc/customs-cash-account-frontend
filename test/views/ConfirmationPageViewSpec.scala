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
import views.html.confirmation_page
import utils.SpecBase

class ConfirmationPageViewSpec extends SpecBase with ViewTestHelper {

  "ConfirmationPageView" should {

    "display correct information" when {
      "title is visible" in new Setup {
        titleShouldBeCorrect(view, "cf.cash-account.transactions.confirmation.statements")
      }

      "backlink should take you back to request transactions" in new Setup {
        shouldContainBackLinkUrl(view, controllers.routes.SelectedTransactionsController.onPageLoad().url)
      }

      "header is correct" in new Setup {
        view.getElementsByTag("h1").text() mustBe messages(
          "cf.cash-account.transactions.confirmation.statements")
      }

      "email panel is correct" in new Setup {
        view.getElementById("email-confirmation-panel-date").text() mustBe dates
      }

      "email confirmation is correct" in new Setup {
        view.getElementById("email-confirmation-subheader").text() mustBe messages(
          "cf.cash-account.transactions.confirmation.next")
      }

      "body text is correct" in new Setup {
        view.getElementById("body-text2").text() mustBe s"${messages(
          "cf.cash-account.transactions.confirmation.email")} ${messages(
            "cf.cash-account.transactions.confirmation.download")}"
      }

      "link text is correct" in new Setup {
        view.getElementById("link-text").text() mustBe messages(
          "cf.cash-account.transactions.confirmation.back")
      }
    }
  }

  trait Setup {
    val startDate = "March 2022"
    val endDate = "April 2022"
    val dates = s"$startDate ${messages("month.to")} $endDate"

    val view: Document = Jsoup.parse(app.injector.instanceOf[confirmation_page].apply(dates).body)
  }
}
