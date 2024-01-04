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

import config.AppConfig
import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CashAccountViewModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import views.html.cash_account_transactions_not_available

class CashAccountTransactionsNotAvailableSpec extends SpecBase {

  "view" should {

    "display transactionsTimeout section" when {

      "transactionsTimeout is true" in new Setup {
        val view: Document = viewDoc(model, transactionsTimeout = true)

        //page title
        view.title() mustBe
          s"${messages(app)("cf.cash-account.detail.title")} - ${messages(app)("service.name")} - GOV.UK"
        //backLink url
        view.html().contains(config.customsFinancialsFrontendHomepage) mustBe true

        //cash_account_balance section
        view.getElementById("account-number").text() mustBe
          msgs("cf.cash-account.detail.account", model.account.number)

        view.getElementsByTag("h1").text() mustBe msgs("cf.cash-account.detail.heading")

        view.getElementById("balance-available").text().contains(
          s"£100.00 ${msgs("cf.cash-account.detail.available")}"
        ) mustBe true

        // transaction timeout section
        val noTransactionAvailableSection: String = view.getElementById("no-transactions-available").text()

        noTransactionAvailableSection.contains(
          msgs("cf.cash-account.detail.transactions-not-available.first")) mustBe true
        noTransactionAvailableSection.contains(
          msgs("cf.cash-account.detail.transactions-not-available.second")) mustBe true

        //h2 element
        view.getElementById("missing-documents-guidance-heading").text() mustBe
          msgs("cf.cash-account.transactions.request.link.heading")

        //link
        val linkElement: String = view.getElementsByClass("govuk-!-margin-bottom-9").html()

        linkElement.contains(msgs("cf.cash-account.transactions.request.link")) mustBe true
        linkElement.contains(msgs("cf.cash-account.transactions.request.link.pre")) mustBe true
        linkElement.contains(controllers.routes.RequestTransactionsController.onPageLoad().url) mustBe true

        // Check for nonavailability of 'unable to show payment section'
        view.text().contains(msgs("cf.cash-account.detail.transactions-not-available")) mustBe false
      }
    }

    "display unable to show payment section" when {

      "transactionsTimeout is false" in new Setup {
        val view: Document = viewDoc(model, transactionsTimeout = false)

        //page title
        view.title() mustBe
          s"${messages(app)("cf.cash-account.detail.title")} - ${messages(app)("service.name")} - GOV.UK"
        //backLink url
        view.html().contains(config.customsFinancialsFrontendHomepage) mustBe true

        //cash_account_balance section
        view.getElementById("account-number").text() mustBe
          msgs("cf.cash-account.detail.account", model.account.number)

        view.getElementsByTag("h1").text() mustBe msgs("cf.cash-account.detail.heading")

        view.getElementById("balance-available").text().contains(
          s"£100.00 ${msgs("cf.cash-account.detail.available")}") mustBe true

        //unable to show payment section
        view.text().contains(msgs("cf.cash-account.detail.transactions-not-available")) mustBe true

        //check unavailability of transaction timeout section
        view.getElementById("no-transactions-available").text().contains(
          msgs("cf.cash-account.detail.transactions-not-available.first")) mustBe false

        view.getElementById("no-transactions-available").text().contains(
          msgs("cf.cash-account.detail.transactions-not-available.second")) mustBe false
      }
    }
  }


  trait Setup {
    val app: Application = application.build()

    implicit val msgs: Messages = messages(app)
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "test_path")

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
}
