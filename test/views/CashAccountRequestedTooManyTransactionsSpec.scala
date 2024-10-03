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
import org.scalatest.Assertion
import viewmodels.RequestedTooManyTransactionsViewModel
import views.html.cash_account_requested_too_many_transactions

import java.time.LocalDate

class CashAccountRequestedTooManyTransactionsSpec extends ViewTestHelper {

  "requested too many transactions view" should {

    "display correct contents with heading and statement message" in new Setup {

      implicit val view: Document = viewDoc

      val statementMsg: String = view.getElementById("requested-too-many-transactions-message-statement").text()

      titleShouldBeCorrect(view, titleMsgKey = "cf.cash-account.detail.title")
      shouldContainBackLinkUrl(view, selectedTxnUrl)
      shouldContainCorrectHeading(view)
      shouldContainCorrectFromDateInStatement(statementMsg, fromDate)
      shouldContainCorrectToDateInStatement(statementMsg, toDate)
      shouldContainCorrectTryAgainLink(view)
    }
  }

  private def shouldContainCorrectHeading(view: Document): Assertion = {
    view.getElementById("requested-too-many-transactions-message-heading").text() mustBe
      messages("cf.cash-account.transactions.requested.tooMany.transactions")
  }

  private def shouldContainCorrectFromDateInStatement(statementMsg: String,
                                                      fromDate: LocalDate): Assertion = {
    statementMsg.contains(s"${messages(s"month.${fromDate.getMonthValue}")} ${fromDate.getYear}") mustBe true
  }

  private def shouldContainCorrectToDateInStatement(statementMsg: String,
                                                    toDate: LocalDate): Assertion = {
    statementMsg.contains(s"${messages(s"month.${toDate.getMonthValue}")} ${toDate.getYear}") mustBe true
  }

  private def shouldContainCorrectTryAgainLink(implicit view: Document): Assertion = {
    view.getElementsByClass("govuk-body govuk-!-margin-bottom-9").text() mustBe
      messages("cf.cash-account.transactions.requested.tryAgain")
  }

  trait Setup {

    val fromDate: LocalDate = LocalDate.parse("2020-07-18")
    val toDate: LocalDate = LocalDate.parse("2020-07-20")
    val selectedTxnUrl: String = controllers.routes.SelectedTransactionsController.onPageLoad().url

    val model: RequestedTooManyTransactionsViewModel = RequestedTooManyTransactionsViewModel(
      fromDate, toDate, selectedTxnUrl, selectedTxnUrl)

    val viewDoc: Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_requested_too_many_transactions].apply(model).body)
  }

}
