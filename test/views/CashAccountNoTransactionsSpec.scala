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
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import utils.Utils.{period, singleSpace}
import views.html.cash_account_no_transactions

class CashAccountNoTransactionsSpec extends SpecBase {

  "view" should {

    "display correct title" in new Setup {
      shouldContainCorrectTitle(viewDoc)
    }

    "display correct back link" in new Setup {
      shouldContainCorrectBackLink(viewDoc)
    }

    "display correct account number" in new Setup {
      shouldDisplayCorrectAccountNumber(viewDoc, accNumber)
    }

    "display correct cash account heading" in new Setup {
      shouldContainCorrectCashAccountHeading(viewDoc)
    }

    "display correct cash text no amount" in new Setup {
      shouldContainCorrectTextNoAmount(viewDoc)
    }

    "display correct authorised agent guidance" in new Setup {
      shouldContainAuthoriseAgentGuidance(viewDoc)
    }

    "display correct top up guidance" in new Setup {
      shouldContainTopUpGuidance(viewDoc)
    }

    "display correct how to use cash account guidance" in new Setup {
      shouldContainHowToUseCashAccountGuidance(viewDoc)
    }

    "display correct help and support guidance" in new Setup {
      shouldContainHelpAndSupportGuidance(viewDoc)
    }
  }

  private def shouldContainCorrectTitle(viewDoc: Document)(implicit msgs: Messages): Assertion = {
    viewDoc.title() mustBe s"${msgs("cf.cash-account.detail.title")} - ${msgs("service.name")} - GOV.UK"
  }

  private def shouldContainCorrectBackLink(viewDoc: Document)(implicit appConfig: AppConfig): Assertion = {
    viewDoc.html().contains(appConfig.customsFinancialsFrontendHomepage) mustBe true
  }

  private def shouldDisplayCorrectAccountNumber(viewDoc: Document,
                                                accNumber: String)(implicit msgs: Messages): Assertion = {
    viewDoc.getElementById("account-number").text() mustBe msgs("cf.cash-account.detail.account", accNumber)
  }

  private def shouldContainCorrectCashAccountHeading(viewDoc: Document)(implicit msgs: Messages): Assertion = {
    viewDoc.getElementsByTag("h1").text() mustBe msgs("cf.cash-account.detail.heading")
  }

  private def shouldContainCorrectTextNoAmount(viewDoc: Document)(implicit msgs: Messages): Assertion = {
    viewDoc.getElementById("balance-available")
      .text() mustBe s"£0$singleSpace${msgs("cf.cash-account.detail.available")}"
  }

  private def shouldContainAuthoriseAgentGuidance(viewDoc: Document)(implicit msgs: Messages): Assertion = {
    viewDoc.getElementsByTag("p").text()
      .contains(msgs("cf.cash-account.detail.no-transactions.p1")) mustBe true
  }

  private def shouldContainTopUpGuidance(viewDoc: Document)(implicit msgs: Messages): Assertion = {
    viewDoc.text().contains(msgs("cf.cash-account.top-up.guidance")) mustBe true
  }

  private def shouldContainHowToUseCashAccountGuidance(viewDoc: Document)(implicit msgs: Messages,
                                                                          config: AppConfig): Assertion = {
    val linkElement = viewDoc.getElementById("cf.cash-account.how-to-use.guidance.link")

    linkElement.attribute("href").getValue mustBe config.cashAccountForCdsDeclarationsUrl
    linkElement.text() mustBe msgs("cf.cash-account.how-to-use.guidance.link.text")

    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.text.pre")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.text.post")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.how-to-use.guidance.link.text")) mustBe true
  }

  private def shouldContainHelpAndSupportGuidance(viewDoc: Document)(implicit msgs: Messages,
                                                                     config: AppConfig): Assertion = {
    viewDoc.getElementById("help-and-support-heading").text() mustBe
      msgs("cf.cash-account.transactions.request.support.heading")

    val linkElement = viewDoc.getElementsByClass("govuk-body govuk-!-margin-bottom-9")

    linkElement.get(0).getElementsByTag("a").attr("href") mustBe
      config.cashAccountForCdsDeclarationsUrl

    linkElement.text() mustBe
      s"${
        msgs("cf.cash-account.help-and-support.link.text.pre")
      }$singleSpace${
        msgs("cf.cash-account.help-and-support.link.text")
      }$singleSpace${
        msgs("cf.cash-account.help-and-support.link.text.post")
      }$period"
  }

  trait Setup {
    val eori = "test_eori"
    val balances: CDSCashBalance = CDSCashBalance(None)
    val accNumber = "12345678"

    val cashAccount: CashAccount = CashAccount(
      number = accNumber, owner = eori, status = AccountStatusOpen, balances = balances)

    val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

    val app: Application = buildApp
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()

    def view(accountModel: CashAccountViewModel): Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_no_transactions].apply(accountModel).body)

    val viewDoc: Document = view(model)
  }
}
