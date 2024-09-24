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
import views.html.cash_account_transactions_not_available

class CashAccountTransactionsNotAvailableSpec extends ViewTestHelper {

  "view" should {

    "display transactionsTimeout section" when {

      "transactionsTimeout is true" in new Setup {
        implicit val view: Document = viewDoc(model, transactionsTimeout = true)

        titleShouldBeCorrect(view, "cf.cash-account.detail.title")

        shouldContainBackLinkUrl(view, appConfig.customsFinancialsFrontendHomepage)

        shouldContainCashAccountBalanceSection(model.account.number)

        shouldContainNoTransactionAvailableSection

        shouldContainH2Element

        shouldContainLinkElement

        checkUnavailabilityOfPaymentSection

        shouldContainHelpAndSupportSection
      }
    }

    "display unable to show payment section" when {

      "transactionsTimeout is false" in new Setup {
        implicit val view: Document = viewDoc(model, transactionsTimeout = false)

        titleShouldBeCorrect(view, "cf.cash-account.detail.title")

        shouldContainBackLinkUrl(view, appConfig.customsFinancialsFrontendHomepage)

        shouldContainCashAccountBalanceSection(model.account.number)

        shouldContainPaymentSection

        checkUnavailabilityOfTransactionTimeOutSection

        shouldContainHelpAndSupportSection
      }
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

    def viewDoc(accountModel: CashAccountViewModel,
                transactionsTimeout: Boolean): Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_transactions_not_available].apply(
        accountModel,
        transactionsTimeout
      ).body)
  }

  private def shouldContainCashAccountBalanceSection(accNumber: CAN)(implicit view: Document): Assertion = {
    view.getElementById("account-number").text() mustBe messages("cf.cash-account.detail.account", accNumber)

    view.getElementsByTag("h1").text() mustBe messages("cf.cash-account.detail.heading")

    view.getElementById("balance-available").text().contains(
      s"£100.00 ${messages("cf.cash-account.detail.available")}"
    ) mustBe true
  }

  private def shouldContainNoTransactionAvailableSection(implicit view: Document): Assertion = {
    val noTransactionAvailableSection: String = view.getElementById("no-transactions-available").text()

    noTransactionAvailableSection.contains(
      messages("cf.cash-account.detail.transactions-not-available.first")) mustBe true

    noTransactionAvailableSection.contains(
      messages("cf.cash-account.detail.transactions-not-available.second")) mustBe true
  }

  private def shouldContainH2Element(implicit view: Document): Assertion =
    view.getElementById("missing-documents-guidance-heading").text() mustBe
      messages("cf.cash-account.transactions.request.link.heading")

  private def shouldContainLinkElement(implicit view: Document): Assertion = {
    val linkElement: String = view.getElementsByClass("govuk-!-margin-bottom-9").html()

    linkElement.contains(messages("cf.cash-account.transactions.request.link.previous")) mustBe true

    linkElement.contains(messages("cf.cash-account.transactions.request.link.pre")) mustBe true

    linkElement.contains(controllers.routes.RequestTransactionsController.onPageLoad().url) mustBe true
  }

  private def checkUnavailabilityOfPaymentSection(implicit view: Document): Assertion =
    view.text().contains(messages("cf.cash-account.detail.transactions-not-available")) mustBe false

  private def checkUnavailabilityOfTransactionTimeOutSection(implicit view: Document): Assertion = {
    view.getElementById("no-transactions-available").text().contains(
      messages("cf.cash-account.detail.transactions-not-available.first")) mustBe false

    view.getElementById("no-transactions-available").text().contains(
      messages("cf.cash-account.detail.transactions-not-available.second")) mustBe false
  }

  private def shouldContainPaymentSection(implicit view: Document): Assertion =
    view.text().contains(messages("cf.cash-account.detail.transactions-not-available")) mustBe false

  private def shouldContainHelpAndSupportSection(implicit view: Document): Assertion = {
    val helpAndSupportLink = view.getElementsByClass("govuk-!-margin-bottom-9").html()

    view.getElementById("help-and-support-heading").text() mustBe
      messages("cf.cash-account.transactions.request.support.heading")

    helpAndSupportLink.contains(messages("cf.cash-account.help-and-support.link.text")) mustBe true

    helpAndSupportLink.contains(messages("cf.cash-account.help-and-support.link.text.pre")) mustBe true

    helpAndSupportLink.contains(appConfig.cashAccountForCdsDeclarationsUrl) mustBe true
  }
}
