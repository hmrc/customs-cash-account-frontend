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

import models.CashTransactionDates
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.select_transactions
import forms.SelectTransactionsFormProvider

import java.time.Clock

class SelectTransactionsSpec extends ViewTestHelper {

  "SelectTransactions" should {

    "display correct information" when {
      "title is visible" in new Setup {
        titleShouldBeCorrect(view, "cf.cash-account.transactions.title")
      }

      "backlink should take you back to request transactions" in new Setup {
        shouldContainBackLinkUrl(view, controllers.routes.CashAccountV2Controller.showAccountDetails(None).url)
      }

      "start field must be Displayed" in new Setup {
        view.getElementById("start").text() mustBe dateText
      }

      "start hint is displayed" in new Setup {
        view
          .getElementsByClass("govuk-hint")
          .text()
          .contains("Start date must be after October 2019. For example, 3 2021.") mustBe true
      }

      "end hint is displayed" in new Setup {
        view.getElementsByClass("govuk-hint").text().contains("For example, 3 2021") mustBe true
      }

      "end field must be Displayed" in new Setup {
        view.getElementById("start").text() mustBe dateText
      }

      "header is correct" in new Setup {
        view.getElementsByTag("h1").text() mustBe messages("cf.cash-account.transactions.heading")
      }
    }
  }

  trait Setup {
    val dateText = "Month Year"

    implicit val clk: Clock              = Clock.systemUTC()
    val form: Form[CashTransactionDates] = new SelectTransactionsFormProvider().apply()
    val view: Document                   = Jsoup.parse(app.injector.instanceOf[select_transactions].apply(form).body)
  }
}
