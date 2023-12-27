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
import play.twirl.api.Html
import utils.SpecBase
import views.html.components.p1

class P1Spec extends SpecBase {

  "component" should {
    "display correct contents" when {
      "it contains all the parameters' value" in new Setup {
        p1Component.getElementById(id).text() mustBe
          s"${content.body}$space${link.get.body}$space${tabLink.get.body}"

        p1Component.getElementsByClass(classes).size() mustBe 1
      }

      "it contains only content" in new Setup {
        p1ComponentWithContentOnly.text() mustBe content.body
        p1ComponentWithContentOnly.getElementsByClass("govuk-body").size mustBe 1
      }

      "it contains content, id and classes only" in new Setup {
        p1ComponentWithContentIdAndClasses.getElementById(id).text() mustBe content.body
        p1ComponentWithContentIdAndClasses.getElementsByClass(classes).text() mustBe content.body
      }
    }
  }

  trait Setup {
    val space = " "
    val content: Html = Html("test_content")
    val id: String = "test_id"
    val classes: String = "govuk-!-margin-bottom-7"
    val link: Option[Html] = Some(Html("test_Link"))
    val tabLink: Option[Html] = Some(Html("tab_link"))

    val app: Application = application.build()

    implicit val msgs: Messages = messages(app)

    val p1Component: Document =
      Jsoup.parse(app.injector.instanceOf[p1].apply(content, Some(id), Some(classes), link, tabLink).body)

    val p1ComponentWithContentOnly: Document = Jsoup.parse(app.injector.instanceOf[p1].apply(content).body)

    val p1ComponentWithContentIdAndClasses: Document =
      Jsoup.parse(app.injector.instanceOf[p1].apply(content, Some(id), Some(classes)).body)
  }
}
