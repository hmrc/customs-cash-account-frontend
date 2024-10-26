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
import forms.JamieFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.Messages
import views.html.jamie_form_page


class JamieInputPageSpec extends ViewTestHelper {
  "view" should {
    "display correct content" in new Setup {
      titleShouldBeCorrect(view, "cf.cash-account.detail.title")
      shouldContainBackLinkUrl(view, appConfig.customsFinancialsFrontendHomepage)
      shouldContainCorrectTitle(view)
      shouldContainCorrectFormHeader(view)
      shouldContainCorrectFormInputValues(view)
      shouldContainNameHint(view)
      shouldContainAgeHint(view)
      shouldDisplayHelpAndSupportHeader(view)
      shouldDisplayHelpAndSupportTextAndLink(view)
    }
  }

  private def shouldContainCorrectTitle(viewDocument: Document) = {
    viewDocument.getElementById("main-header").text mustBe "Jamie's Super-Duper Form"
  }

  private def shouldContainCorrectFormHeader(viewDocument: Document) = {
    viewDocument.getElementById("form-header").text mustBe "Name and Age Form"
  }

  private def shouldContainCorrectFormInputValues(viewDocument: Document) = {
    val inputTextElementOne: Element = viewDocument.getElementById("valueOne")
    inputTextElementOne.getElementsByAttribute("name").text() mustBe emptyString

    val inputTextElementTwo: Element = viewDocument.getElementById("valueTwo")
    inputTextElementTwo.getElementsByAttribute("name").text() mustBe emptyString
  }

  private def shouldContainNameHint(viewDocument: Document) = {
    viewDocument.getElementById("valueOne-hint-text").text mustBe "Please enter your Name"
  }

  private def shouldContainAgeHint(viewDocument: Document) = {
    viewDocument.getElementById("valueTwo-hint-text").text mustBe "Please enter your Age"
  }

  private def shouldContainSearchButton(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.getElementsByClass("inline-button").text() mustBe msgs("site.search")
  }

  private def shouldDisplayHelpAndSupportHeader(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.getElementById("help-and-support-header")
      .text mustBe msgs("cf.cash-account.transactions.request.support.heading")
  }
  private def shouldDisplayHelpAndSupportTextAndLink(viewDocument: Document)
                                                    (implicit msgs: Messages, config: AppConfig) = {
    val viewAsHtml = viewDocument.html()

    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.pre.v2")) mustBe true
    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text")) mustBe true
    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.post")) mustBe true
    viewAsHtml.contains(config.cashAccountForCdsDeclarationsUrl) mustBe true
  }

  trait Setup {
    val form = new JamieFormProvider()()

    val view: Document = Jsoup.parse(app.injector.instanceOf[jamie_form_page].apply(form).body)
  }
}
