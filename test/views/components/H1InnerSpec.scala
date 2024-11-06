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
import utils.SpecBase
import views.html.components.h1Inner

class H1InnerSpec extends SpecBase {

  "h1Inner component" should {

    "display correct contents" when {

      "it contains msg, innerMsg, id and classes value" in new Setup {

        private val h1InnerComponentAsText = h1InnerComponent.text()

        h1InnerComponentAsText mustBe messages(msgKey, messages(innerMsgKey))
        h1InnerComponent.select("h1").attr("class") mustBe classesValue
        h1InnerComponent.select("h1").attr("id") mustBe idValue.get
      }

      "it does not contain id" in new Setup {

        private val h1InnerComponentWithNoIdAsText = h1InnerComponentWithNoId.text()

        h1InnerComponentWithNoIdAsText mustBe messages(msgKey, messages(innerMsgKey))
        h1InnerComponentWithNoId.select("h1").attr("class") mustBe classesValue
        h1InnerComponentWithNoId.select("h1").hasAttr("id") mustBe false
      }

      "it uses default class" in new Setup {

        private val h1InnerComponentWithDefaultClassAsText = h1InnerComponentWithDefaultClass.text()

        h1InnerComponentWithDefaultClassAsText mustBe messages(msgKey, messages(innerMsgKey))
        h1InnerComponentWithDefaultClass.select("h1").attr("class") mustBe "govuk-heading-xl"
      }

      "it handles missing inner message" in new Setup {

        private val h1InnerComponentWithEmptyInnerMsgAsText = h1InnerComponentWithEmptyInnerMsg.text()

        h1InnerComponentWithEmptyInnerMsgAsText mustBe messages(msgKey, emptyString)
        h1InnerComponentWithEmptyInnerMsg.select("h1").attr("class") mustBe classesValue
        h1InnerComponentWithEmptyInnerMsg.select("h1").hasAttr("id") mustBe false
      }

      "it handles empty class string" in new Setup {

        private val h1InnerComponentWithEmptyClassAsText = h1InnerComponentWithEmptyClass.text()

        h1InnerComponentWithEmptyClassAsText mustBe messages(msgKey, messages(innerMsgKey))
        h1InnerComponentWithEmptyClass.select("h1").attr("class") mustBe emptyString
        h1InnerComponentWithEmptyClass.select("h1").attr("id") mustBe idValue.get
      }
    }
  }

  trait Setup {

    val msgKey: String = "cf.message"
    val innerMsgKey: String = "cf.message.inner"
    val idValue: Option[String] = Some("test-id")
    val classesValue: String = "govuk-heading-xl"

    val h1InnerComponent: Document =
      Jsoup.parse(application.injector.instanceOf[h1Inner].apply(msgKey, innerMsgKey, idValue, classesValue).body)

    val h1InnerComponentWithNoId: Document =
      Jsoup.parse(application.injector.instanceOf[h1Inner].apply(msgKey, innerMsgKey, None, classesValue).body)

    val h1InnerComponentWithDefaultClass: Document =
      Jsoup.parse(application.injector.instanceOf[h1Inner].apply(msgKey, innerMsgKey, idValue).body)

    val h1InnerComponentWithEmptyInnerMsg: Document =
      Jsoup.parse(application.injector.instanceOf[h1Inner].apply(msgKey, emptyString, None, classesValue).body)

    val h1InnerComponentWithEmptyClass: Document =
      Jsoup.parse(application.injector.instanceOf[h1Inner].apply(msgKey, innerMsgKey, idValue, emptyString).body)
  }
}
