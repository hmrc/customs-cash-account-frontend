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
import views.html.jamie_details_page

class JamieDetailsPageControllerSpec extends ViewTestHelper {
  "view should" in new Setup {
    titleShouldBeCorrect(view, "cf.cash-account.detail.title")
    shouldContainBackLinkUrl(view, expectedBackLinkUrl)
    shouldContainCorrectTitle(view)
    shouldContainCorrectNameField(view)
    shouldContainCorrectAgeField(view)
    shouldDisplayHelpAndSupportHeader(view)
    shouldDisplayHelpAndSupportTextAndLink(view)
    shouldContainCorrectNiNumberField(view)
  }

  private def shouldContainCorrectTitle(viewDocument: Document) = {
    viewDocument.getElementById("details-header").text mustBe "Person Details"
  }
  
  private def shouldContainCorrectNameField(viewDocument: Document) = {
    viewDocument.getElementById("details-name").text mustBe "Name: Jamie"
  }

  private def shouldContainCorrectNiNumberField(viewDocument: Document) = {
    viewDocument.getElementById("details-ni-number").text mustBe "NI Number: DF45667a"
  }
  
  private def shouldContainCorrectAgeField(viewDocument: Document) = {
    viewDocument.getElementById("details-age").text mustBe "Age: 41"
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
    val name = "Jamie"
    val age = 41
    val niNumber = "DF45667a"
    val expectedBackLinkUrl: String =
      controllers.routes.JamiePageController.onPageLoad(None, None).url
    val view: Document = Jsoup.parse(app.injector.instanceOf[jamie_details_page].apply(name, age, Some(niNumber)).body)
  }
}
