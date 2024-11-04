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

package views.components

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import views.html.components.button

class ButtonSpec extends SpecBase {

  "component" should {

    "display correct contents" when {

      "it contains msg, href and classes value" in new Setup {
        private val btnComponentAsText = buttonComponent.text()

        btnComponentAsText.contains(messages(msgKey)) mustBe true
        buttonComponent.html().contains(hrefValue) mustBe true

        buttonComponent.getElementsByClass(classesValue).size() mustBe 1
      }

      "it does not contain href and classes" in new Setup {
        private val buttonComponentWithNoHrefAsText = buttonComponentWithNoHref.text()

        buttonComponentWithNoHrefAsText.contains(messages(msgKey)) mustBe true
        buttonComponentWithNoHref.html().contains(hrefValue) mustBe false
        buttonComponentWithNoHref.html().contains(classesValue) mustBe false
      }
    }
  }

  trait Setup {
    val app: Application = buildApp
    val msgKey: String = "cf.verify.your.email.change.button"
    val hrefValue = "www.test.com"
    val classesValue = "govuk-!-margin-bottom-7"

    val buttonComponent: Document =
      Jsoup.parse(app.injector.instanceOf[button].apply(msgKey, Some(hrefValue), classesValue).body)

    val buttonComponentWithNoHref: Document =
      Jsoup.parse(app.injector.instanceOf[button].apply(msgKey).body)
  }
}
