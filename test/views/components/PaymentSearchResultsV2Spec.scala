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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.i18n.Messages
import utils.TestData.*
import viewmodels.PaymentSearchResultStatementsViewModel
import views.ViewTestHelper
import views.html.components.payment_search_results

class PaymentSearchResultsV2Spec extends ViewTestHelper {

  "view component" should {

    "display correct contents" when {

      "there are no transaction to display" in new Setup {
        val view: Document = componentView(PaymentSearchResultStatementsViewModel(Seq.empty, None))

        shouldDisplayNoTransactionsMessage(view)
        shouldNotDisplayTable(view)
      }

      "transactions are available to display" in new Setup {
        val view: Document = componentView(PaymentSearchResultStatementsViewModel(SEQ_OF_PAYMENT_DETAILS_01, None))

        shouldNotDisplayNoTransactionsMessage(view)
        shouldDisplayTableHeaders(view)
        shouldDisplayTableElements(view)
      }
    }
  }

  private def shouldDisplayNoTransactionsMessage(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.getElementById("no-transactions-to-display").text() mustBe
      msgs("cf.cash-account.transactions.no-transactions.message")
  }

  private def shouldNotDisplayNoTransactionsMessage(viewDocument: Document) = {
    Option(viewDocument.getElementById("no-transactions-to-display")) mustBe empty
  }

  private def shouldDisplayTableHeaders(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.html().contains(msgs("cf.cash-account.detail.date")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.transaction-type")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.credit")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.debit")) mustBe true
  }

  private def shouldDisplayTableElements(viewDocument: Document) = {

    val tableRows: Elements = viewDocument.getElementsByClass("govuk-table__row")

    val row1: String = tableRows.get(1).html()
    val row2: String = tableRows.get(2).html()
    val row3: String = tableRows.get(3).html()

    tableRows.size() mustBe 4

    row1 must include("17 August 2021")
    row2 must include("16 August 2021")
    row3 must include("15 August 2021")
  }

  private def shouldNotDisplayTable(viewDocument: Document) = {
    val tableRosElementsByClass = viewDocument.getElementsByClass("hmrc-responsive-table__heading")

    tableRosElementsByClass.size() mustBe 0
  }

  trait Setup {

    def componentView(model: PaymentSearchResultStatementsViewModel): Document =
      Jsoup.parse(app.injector.instanceOf[payment_search_results].apply(model).body)
  }
}
