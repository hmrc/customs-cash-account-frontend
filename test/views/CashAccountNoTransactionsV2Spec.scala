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
import play.api.Application
import play.api.i18n.Messages
import utils.Utils.{period, singleSpace}
import views.html.cash_account_no_transactions_v2

class CashAccountNoTransactionsV2Spec extends ViewTestHelper {

  "view" should {

    "display correct contents" in new Setup {
      val viewDoc: Document = view(model)

      titleShouldBeCorrect(viewDoc, "cf.cash-account.detail.title")
      shouldContainBackLinkUrl(viewDoc, appConfig.customsFinancialsFrontendHomepage)

      shouldDisplayCorrectAccountDetails(viewDoc, accNumber)
      shouldDisplayPayImportDutyAndTaxesGuidance(viewDoc)
      shouldDisplayTopUpGuidance(viewDoc)
      shouldDisplayFindOutHowLink(viewDoc)
      shouldDisplayHelpAndSupportGuidanceHeader(viewDoc)
      shouldDisplayHelpAndSupportGuidance(viewDoc)
    }

  }

  private def shouldDisplayCorrectAccountDetails(viewDoc: Document, accNumber: String)(implicit msgs: Messages) = {
    viewDoc.getElementById("account-number").text mustBe msgs("cf.cash-account.detail.account", accNumber)
    viewDoc.getElementsByTag("h1").text mustBe msgs("cf.cash-account.detail.title")
    viewDoc.getElementById("balance-available").text() mustBe s"£0$singleSpace${msgs("cf.cash-account.detail.available")}"
  }

  private def shouldDisplayPayImportDutyAndTaxesGuidance(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-import-duties-guidance").text() mustBe msgs("cf.cash-account.detail.no-transactions.p1")
  }

  private def shouldDisplayTopUpGuidance(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-authorise-agent-guidance").text() mustBe msgs("cf.cash-account.top-up.guidance")
  }

  private def shouldDisplayFindOutHowLink(viewDoc: Document)(implicit msgs: Messages, config: AppConfig) = {
    
    val elementLink = viewDoc.getElementById("cash-account-top-up-guidance-link")
    elementLink.attribute("href").getValue mustBe config.cashAccountForCdsDeclarationsUrl
    elementLink.text() mustBe msgs("cf.cash-account.how-to-use.guidance.link")
    
    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.text.pre")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.link")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.text.post")) mustBe true
  }

  private def shouldDisplayHelpAndSupportGuidanceHeader(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-help-and-support-guidance-header").text() mustBe msgs("cf.cash-account.transactions.request.support.heading")
  }

  private def shouldDisplayHelpAndSupportGuidance(viewDoc: Document)(implicit msgs: Messages, config: AppConfig) = {

    val elementLink = viewDoc.getElementsByClass("cash-account-help-and-support-guidance")
    elementLink.get(0).getElementsByTag("a").attr("href") mustBe config.cashAccountForCdsDeclarationsUrl

    elementLink.text() mustBe
      s"${
        msgs("cf.cash-account.help-and-support.link.text.pre")
      }$singleSpace${
        msgs("cf.cash-account.help-and-support.link.text")
      }$singleSpace${
        msgs("cf.cash-account.help-and-support.link.text.post")
      }$period"
  }



  trait Setup {
    val app: Application = application.build()
    //implicit val msgs: Messages = messages(app)
    //implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

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

