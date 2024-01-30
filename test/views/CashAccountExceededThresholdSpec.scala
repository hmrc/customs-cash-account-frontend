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

import models.domain.CAN
import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CashAccountViewModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import views.html.cash_account_exceeded_threshold

class CashAccountExceededThresholdSpec extends ViewTestHelper {

  "view" should {

    "display correct contents and guidance" in new Setup {
      implicit val view: Document = viewDoc

      titleShouldBeCorrect(view, titleMsgKey = "cf.cash-account.detail.title")

      shouldContainBackLinkUrl(view, appConfig.customsFinancialsFrontendHomepage)

      shouldContainCashAccountBalanceSection(model.account.number)

      shouldContainExceededThresholdMsg

      shouldContainMissingDocHeadingMsg

      shouldContainLink
    }
  }

  trait Setup {

    val eori = "test_eori"
    val accNumber = "1234567"
    val owner = "test_owner"

    val availBalance: BigDecimal = BigDecimal(100.0)
    val bal: CDSCashBalance = CDSCashBalance(Some(availBalance))

    val cashAcc: CashAccount = CashAccount(accNumber, owner, AccountStatusOpen, bal)
    val model: CashAccountViewModel = CashAccountViewModel(eori, cashAcc)

    val viewDoc: Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_exceeded_threshold].apply(model).body)
  }

  private def shouldContainCashAccountBalanceSection(accNumber: CAN)(implicit view: Document): Assertion = {
    view.getElementById("account-number").text() mustBe messages("cf.cash-account.detail.account", accNumber)

    view.getElementsByTag("h1").text() mustBe messages("cf.cash-account.detail.heading")

    view.getElementById("balance-available").text().contains(
      s"£100.00 ${messages("cf.cash-account.detail.available")}"
    ) mustBe true
  }

  private def shouldContainExceededThresholdMsg(implicit view: Document): Assertion = {
    view.text().contains(
      "There are too many transactions from the last 6 months to display consecutively.") mustBe true

    view.text().contains(
      "View previous transactions from a narrower date period using the search link below.") mustBe true
  }

  private def shouldContainMissingDocHeadingMsg(implicit view: Document): Assertion =
    view.getElementById("missing-documents-guidance-heading").text() mustBe
      messages("cf.cash-account.transactions.request.link.heading")

  private def shouldContainLink(implicit view: Document): Assertion = {
    val linkElement: String = view.getElementsByClass("govuk-!-margin-bottom-9").html()

    linkElement.contains(messages("cf.cash-account.transactions.request.link")) mustBe true

    linkElement.contains(messages("cf.cash-account.transactions.request.link.pre")) mustBe true

    linkElement.contains(controllers.routes.RequestTransactionsController.onPageLoad().url) mustBe true
  }
}
