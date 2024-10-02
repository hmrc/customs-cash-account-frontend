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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.cash_account_not_available

class CashAccountNotAvailableSpec  extends ViewTestHelper {

  "view" should {
    "display correct contents" in new Setup {

      titleShouldBeCorrect(view, "cf.cash-account.detail.title")
      shouldContainBackLinkUrl(view, appConfig.customsFinancialsFrontendHomepage)
      shouldDisplayCashAccountHeader(view)
      shouldDisplayUnableToShowAccountMessage(view)
      shouldDisplayHelpAndSupportHeader(view)
      shouldDisplayHelpAndSupportTextAndLink(view)
    }
  }

  private def shouldDisplayCashAccountHeader(view: Document)(implicit msgs: Messages) = {
    view.getElementById("account-header").text mustBe msgs("cf.cash-account.detail.title")
  }

  private def shouldDisplayUnableToShowAccountMessage(view: Document)(implicit msgs: Messages) = {
    view.getElementById("account-not-available").text mustBe msgs("cf.cash-account.detail.account-not-available")
  }

  private def shouldDisplayHelpAndSupportHeader(view: Document)(implicit msgs: Messages) = {
    view.getElementById("help-and-support-header").
      text mustBe msgs("cf.cash-account.transactions.request.support.heading")
  }

  private def shouldDisplayHelpAndSupportTextAndLink(view: Document)(implicit msgs: Messages, config: AppConfig) = {
    val viewAsHtml = view.html()

    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.pre")) mustBe true
    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text")) mustBe true
    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.post")) mustBe true
    viewAsHtml.contains(config.cashAccountForCdsDeclarationsUrl) mustBe true
  }

  trait Setup {
    val view: Document = Jsoup.parse(app.injector.instanceOf[cash_account_not_available].apply().body)
    }
  }
