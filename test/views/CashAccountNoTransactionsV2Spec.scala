/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.i18n.Messages
import utils.Utils.singleSpace
import views.html.cash_account_no_transactions_v2

class CashAccountNoTransactionsV2Spec extends ViewTestHelper {

  "view" should {

    "display title" in new Setup {
      val viewDoc: Document = view(model)
      titleShouldBeCorrect(viewDoc, "cf.cash-account.detail.title")
    }

    "display back link" in new Setup {
      val viewDoc: Document = view(model)
      shouldContainBackLinkUrl(viewDoc, appConfig.customsFinancialsFrontendHomepage)
    }

    "display account details" in new Setup {
      val viewDoc: Document = view(model)
      shouldDisplayCorrectAccountDetails(viewDoc, accNumber)
    }

    "display pay import duty and tax guidance" in new Setup {
      val viewDoc: Document = view(model)
      shouldDisplayPayImportDutyAndTaxesGuidance(viewDoc)
    }

    "display authorise agent guidance" in new Setup {
      val viewDoc: Document = view(model)
      shouldDisplayAuthoriseAnAgentGuidance(viewDoc)
    }

    "display top up guidance" in new Setup {
      val viewDoc: Document = view(model)
      shouldDisplayTopUpGuidance(viewDoc)
    }

    "display find out how link" in new Setup {
      val viewDoc: Document = view(model)
      shouldDisplayFindOutHowLink(viewDoc)
    }

    "display help and support guidance header" in new Setup {
      val viewDoc: Document = view(model)
      shouldDisplayHelpAndSupportGuidanceHeader(viewDoc)
    }

    "display help and support guidance" in new Setup {
      val viewDoc: Document = view(model)
      shouldDisplayHelpAndSupportGuidance(viewDoc)
    }
  }

  private def shouldDisplayCorrectAccountDetails(viewDoc: Document, accNumber: String)(implicit msgs: Messages) = {
    viewDoc.getElementById("account-number").text mustBe msgs("cf.cash-account.detail.account", accNumber)
    viewDoc.getElementsByTag("h1").text mustBe msgs("cf.cash-account.detail.title")

    viewDoc.getElementById("balance-available").text() mustBe
      s"£0$singleSpace${msgs("cf.cash-account.detail.available")}"
  }

  private def shouldDisplayPayImportDutyAndTaxesGuidance(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-import-duties-guidance").text() mustBe
      msgs("cf.cash-account.detail.no-transactions.import-duties")
  }

  private def shouldDisplayAuthoriseAnAgentGuidance(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-authorise-an-agent").text() mustBe
      msgs("cf.cash-account.detail.no-transactions.authorise-an-agent")
  }

  private def shouldDisplayTopUpGuidance(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.text().contains(msgs("cf.cash-account.top-up.guidance.text.pre")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.top-up.guidance.text.link")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.top-up.guidance.text.post")) mustBe true
  }

  private def shouldDisplayFindOutHowLink(viewDoc: Document)(implicit msgs: Messages, config: AppConfig) = {
    val elementLink = viewDoc.getElementById("cash-account-top-up-guidance-link")
    elementLink.attribute("href").getValue mustBe config.cashAccountTopUpGuidanceUrl
    elementLink.text() mustBe msgs("cf.cash-account.top-up.guidance.text.link")

    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.text.pre")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.link.text")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.text.post")) mustBe true
  }

  private def shouldDisplayHelpAndSupportGuidanceHeader(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-help-and-support-guidance-header").text() mustBe
      msgs("cf.cash-account.transactions.request.support.heading")
  }

  private def shouldDisplayHelpAndSupportGuidance(viewDoc: Document)(implicit msgs: Messages, config: AppConfig) = {

    val viewAsHtml = viewDoc.html()

    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text")) mustBe true
    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.post")) mustBe true
    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.pre")) mustBe true
    viewAsHtml.contains(config.cashAccountForCdsDeclarationsUrl) mustBe true
  }

  trait Setup {
    val eori = "test_eori"
    val balances: CDSCashBalance = CDSCashBalance(None)
    val accNumber = "12345678"

    val cashAccount: CashAccount = CashAccount(number = accNumber,
      owner = eori,
      status = AccountStatusOpen,
      balances = balances)

    val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

    def view(account: CashAccountViewModel): Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_no_transactions_v2].apply(account).body)
  }
}
