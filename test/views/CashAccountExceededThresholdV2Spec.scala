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

import forms.SearchTransactionsFormProvider
import models.domain.CAN
import models.{AccountStatusOpen, CDSCashBalance, CashAccount}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.i18n.Messages
import viewmodels.TooManyTransactionsViewModel
import views.html.cash_account_exceeded_threshold_v2

class CashAccountExceededThresholdV2Spec extends ViewTestHelper {

  "view" should {

    "display correct contents and guidance" in new Setup {
      implicit val view: Document = viewDoc

      titleShouldBeCorrect(view, titleMsgKey = "cf.cash-account.detail.title")

      shouldContainBackLinkUrl(view, appConfig.customsFinancialsFrontendHomepage)

      shouldContainCashAccountBalanceSection(accNumber)

      shouldContainCorrectSearchForTransactionsInputTextDetails(view)

      shouldContainSearchButton(view)

      shouldContainExceededThresholdMsg

      shouldContainMissingDocHeadingMsg

      shouldContainLink

      shouldContainNewTabLink

    }
  }

  trait Setup {

    val eori: String = "test_eori"
    val accNumber: String = "1234567"
    val owner: String = "test_owner"

    val availBalance: BigDecimal = BigDecimal(100.0)
    val bal: CDSCashBalance = CDSCashBalance(Some(availBalance))

    val cashAcc: CashAccount = CashAccount(accNumber, owner, AccountStatusOpen, bal)
    val model: TooManyTransactionsViewModel = TooManyTransactionsViewModel(eori, cashAcc)

    val form: Form[String] = new SearchTransactionsFormProvider().apply()

    val viewDoc: Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_exceeded_threshold_v2].apply(form, model).body)
  }

  private def shouldContainCashAccountBalanceSection(accNumber: CAN)(implicit view: Document): Assertion = {
    view.getElementById("account-number").text() mustBe messages("cf.cash-account.detail.account", accNumber)

    view.getElementsByTag("h1").text() mustBe messages("cf.cash-account.detail.heading")

    view.getElementById("balance-available").text().contains(
      s"£100.00 ${messages("cf.cash-account.detail.available")}"
    ) mustBe true
  }

  private def shouldContainCorrectSearchForTransactionsInputTextDetails(viewDocument: Document)
                                                                       (implicit msgs: Messages) = {
    val inputTextElement: Element = viewDocument.getElementById("search-transactions")
    inputTextElement.getElementsByAttribute("name").text() mustBe emptyString

    viewDocument.getElementById("search-transactions-hint-text").html() mustBe
      msgs("cf.cash-account.transactions.search-for-transactions.hint")
  }

  private def shouldContainSearchButton(viewDocument: Document)
                                       (implicit msgs: Messages) = {
    viewDocument.getElementsByClass("inline-button").text() mustBe msgs("site.search")
  }

  private def shouldContainExceededThresholdMsg(implicit view: Document): Assertion = {
    view.getElementById("exceeded-threshold").text() mustBe
      messages("cf.cash-account.transactions.too-many-transactions.hint01")
  }

  private def shouldContainMissingDocHeadingMsg(implicit view: Document): Assertion =
    view.getElementById("missing-documents-guidance-heading").text() mustBe
      messages("cf.cash-account.transactions.request.support.heading")

  private def shouldContainLink(implicit view: Document): Assertion = {
    val linkElement: String = view.getElementsByClass("govuk-!-margin-bottom-9").html()

    linkElement.contains(messages("cf.cash-account.transactions.too-many-transactions.hint03")) mustBe true

    linkElement.contains(messages("cf.cash-account.transactions.too-many-transactions.hint02")) mustBe true

    linkElement.contains(messages("cf.cash-account.transactions.too-many-transactions.hint04")) mustBe true

    linkElement.contains(controllers.routes.RequestTransactionsController.onPageLoad().url) mustBe true
  }

  private def shouldContainNewTabLink(implicit view: Document): Assertion = {
    val newTabLinkElement = view.getElementsByClass("govuk-!-margin-bottom-9").html()

    newTabLinkElement.contains(messages("cf.cash-account.transactions.request.support.link")) mustBe true

    newTabLinkElement.contains(appConfig.cashAccountForCdsDeclarationsUrl) mustBe true
  }
}
