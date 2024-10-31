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
import views.html.components.h2Inner

class H2InnerSpec extends SpecBase {

  "h2Inner component" should {

    "display correct contents" when {

      "it contains msg, innerMsg, id and classes value" in new Setup {

        private val h2InnerComponentAsText = h2InnerComponent.text()

        h2InnerComponentAsText mustBe messages(app)(msgKey, messages(app)(innerMsgKey))
        h2InnerComponent.select("h2").attr("class") mustBe classesValue
        h2InnerComponent.select("h2").attr("id") mustBe idValue.get
      }

      "it does not contain id" in new Setup {

        private val h2InnerComponentWithNoIdAsText = h2InnerComponentWithNoId.text()

        h2InnerComponentWithNoIdAsText mustBe messages(app)(msgKey, messages(app)(innerMsgKey))
        h2InnerComponentWithNoId.select("h2").attr("class") mustBe classesValue
        h2InnerComponentWithNoId.select("h2").hasAttr("id") mustBe false
      }

      "it uses default class" in new Setup {

        private val h2InnerComponentWithDefaultClassAsText = h2InnerComponentWithDefaultClass.text()

        h2InnerComponentWithDefaultClassAsText mustBe messages(app)(msgKey, messages(app)(innerMsgKey))
        h2InnerComponentWithDefaultClass.select("h2").attr("class") mustBe "govuk-heading-m"
      }

      "it handles missing inner message" in new Setup {

        private val h2InnerComponentWithEmptyInnerMsgAsText = h2InnerComponentWithEmptyInnerMsg.text()

        h2InnerComponentWithEmptyInnerMsgAsText mustBe messages(app)(msgKey, emptyString)
        h2InnerComponentWithEmptyInnerMsg.select("h2").attr("class") mustBe classesValue
        h2InnerComponentWithEmptyInnerMsg.select("h2").hasAttr("id") mustBe false
      }

      "it handles empty class string" in new Setup {

        private val h2InnerComponentWithEmptyClassAsText = h2InnerComponentWithEmptyClass.text()

        h2InnerComponentWithEmptyClassAsText mustBe messages(app)(msgKey, messages(app)(innerMsgKey))
        h2InnerComponentWithEmptyClass.select("h2").attr("class") mustBe emptyString
        h2InnerComponentWithEmptyClass.select("h2").attr("id") mustBe idValue.get
      }
    }
  }

  trait Setup {

    val app: Application = buildApp
    val msgKey: String = "cf.message"
    val innerMsgKey: String = "cf.message.inner"
    val idValue: Option[String] = Some("test-id")
    val classesValue: String = "govuk-heading-m"

    implicit val msg: Messages = messages(app)

    val h2InnerComponent: Document =
      Jsoup.parse(app.injector.instanceOf[h2Inner].apply(msgKey, innerMsgKey, idValue, classesValue).body)

    val h2InnerComponentWithNoId: Document =
      Jsoup.parse(app.injector.instanceOf[h2Inner].apply(msgKey, innerMsgKey, None, classesValue).body)

    val h2InnerComponentWithDefaultClass: Document =
      Jsoup.parse(app.injector.instanceOf[h2Inner].apply(msgKey, innerMsgKey, idValue).body)

    val h2InnerComponentWithEmptyInnerMsg: Document =
      Jsoup.parse(app.injector.instanceOf[h2Inner].apply(msgKey, emptyString, None, classesValue).body)

    val h2InnerComponentWithEmptyClass: Document =
      Jsoup.parse(app.injector.instanceOf[h2Inner].apply(msgKey, innerMsgKey, idValue, emptyString).body)
  }
}
