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
import utils.SpecBase
import views.html.confirmation_page

class ConfirmationPageSpec extends SpecBase with ViewTestHelper {

  "ConfirmationPage" should {

    "display correct information" when {
      "title is visible" in new Setup {
        titleShouldBeCorrect(view, "cf.cash-account.transactions.confirmation.statements")
      }

      "backlink should not be in view" in new Setup {
        shouldNotContainBackLink(view, "govuk-back-link")
      }

      "header 1 is correct" in new Setup {
        view.getElementsByTag("h1").text() mustBe messages(
          "cf.cash-account.transactions.confirmation.statements")
      }

      "header 2 is correct" in new Setup {
        view.getElementsByTag("h2").text() mustBe
          "Help make GOV.UK better What happens next Support links"
      }

      "email confirmation is correct" in new Setup {
        view.getElementById("email-confirmation-subheader").text() mustBe messages(
          "cf.cash-account.transactions.confirmation.next")
      }

      "body text email is correct" in new Setup {
        view.getElementById("body-text-email").text() mustBe s"${messages(
          "cf.cash-account.transactions.confirmation.email")}"
      }

      "body text download is correct" in new Setup {
        view.getElementById("body-text-download").text() mustBe s"${messages(
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
