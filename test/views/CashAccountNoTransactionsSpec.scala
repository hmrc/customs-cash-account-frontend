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
import views.html.cash_account_no_transactions

class CashAccountNoTransactionsSpec extends SpecBase {

  "view" should {

    "display correct guidance" in new Setup {
      val eori = "test_eori"
      val balances: CDSCashBalance = CDSCashBalance(None)

      val cashAccount: CashAccount = CashAccount(number = "12345678",
        owner = eori,
        status = AccountStatusOpen,
        balances = balances)

      val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

      val viewDoc: Document = view(model)

      shouldContainCorrectTitle(viewDoc)
      shouldContainCorrectBackLink(viewDoc)
      shouldContainAuthoriseAgentGuidance(viewDoc)
      shouldContainTopUpGuidance(viewDoc)
      shouldContainHowToUseCashAccountGuidance(viewDoc)
      shouldContainHelpAndSupportGuidance(viewDoc)
    }
  }

  private def shouldContainCorrectTitle(viewDoc: Document)(implicit msgs: Messages): Assertion = {
    viewDoc.title() mustBe s"${msgs("cf.cash-account.detail.title")} - ${msgs("service.name")} - GOV.UK"
  }

  private def shouldContainCorrectBackLink(viewDoc: Document)(implicit appConfig: AppConfig): Assertion = {
    viewDoc.html().contains(appConfig.customsFinancialsFrontendHomepage) mustBe true
  }

  private def shouldContainAuthoriseAgentGuidance(viewDoc: Document)(implicit msgs: Messages): Assertion = {
    viewDoc.getElementsByTag("p").text().contains(msgs("cf.cash-account.detail.no-transactions.p1")) mustBe true
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

    val linkElement = viewDoc.getElementById("cf.cash-account.help-and-support.link")

    linkElement.attribute("href").getValue mustBe config.cashAccountForCdsDeclarationsUrl
    linkElement.text() mustBe msgs("cf.cash-account.help-and-support.link.text")

    viewDoc.text().contains(msgs("cf.cash-account.help-and-support.link.text.pre")) mustBe true
    viewDoc.text().contains(msgs("cf.cash-account.help-and-support.link.text.post")) mustBe true
  }

  trait Setup {
    val app: Application = application.build()
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msgs: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()

    def view(accountModel: CashAccountViewModel): Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_no_transactions].apply(accountModel).body)
  }
}
