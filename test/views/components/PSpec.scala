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
import utils.SpecBase
import views.html.components.p

class PSpec extends SpecBase {

  "component" should {

    "display correct contents" when {

      "only message key has been provided" in new Setup {
        pComponent.text() mustBe messages(msgKey)
        pComponent.getElementsByClass(defaultClass).text() mustBe messages(msgKey)
      }

      "id and classes have been provided along with msg key" in new Setup {
        pComponentWithIdAndClasses.getElementById(id).text() mustBe messages(msgKey)
        pComponentWithIdAndClasses.getElementsByClass(classes).text() mustBe messages(msgKey)
      }

      "id, classes have been provided along with msg key and bold attribute is true" in new Setup {
        pComponentBoldWithIdAndClasses.getElementById(id).text() mustBe messages(msgKey)
        pComponentBoldWithIdAndClasses.getElementsByClass(classes).text() mustBe messages(msgKey)
        pComponentBoldWithIdAndClasses.getElementsByClass(defaultClass).text() mustBe empty
        pComponentBoldWithIdAndClasses.getElementsByClass(
          "govuk-!-font-weight-bold").text() mustBe messages(msgKey)
      }
    }
  }

  trait Setup {
    val msgKey = "cf.verify.your.email.p1"
    val id = "test_id"
    val classes = "custom_class"
    val defaultClass = "govuk-body"

    val app: Application = application

    val pComponent: Document = Jsoup.parse(app.injector.instanceOf[p].apply(msgKey).body)
    val pComponentWithIdAndClasses: Document =
      Jsoup.parse(app.injector.instanceOf[p].apply(msgKey, classes = classes, id = Some(id)).body)

    val pComponentBoldWithIdAndClasses: Document =
      Jsoup.parse(app.injector.instanceOf[p].apply(msgKey, classes = classes, id = Some(id), bold = true).body)
  }
}
