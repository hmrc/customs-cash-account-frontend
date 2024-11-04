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

import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import utils.Utils.{period, singleSpace}
import views.html.components.newTabLink

class NewTabLinkSpec extends SpecBase {

  "component" should {

    "display correct view" when {

      "all the parameters have some value" in new Setup {
        val component: Document =
          newTabLinkComponent(linkMessage, href, Some(preLinkMessage), Some(postLinkMessage), classes)

        elementByParagraph(component).text() mustBe
          s"$preLinkMessage$singleSpace$linkMessage$singleSpace$postLinkMessage$period"

        elementByClasses(component, classes).get(0).text() mustBe
          s"$preLinkMessage$singleSpace$linkMessage$singleSpace$postLinkMessage$period"

        shouldContainTheMessage(component, preLinkMessage)
        shouldContainTheMessage(component, postLinkMessage)
      }

      "there is no preLinkMessage" in new Setup {
        val component: Document =
          newTabLinkComponent(
            linkMessage = linkMessage,
            href = href,
            postLinkMessage = Some(postLinkMessage))

        elementByParagraph(component).text() mustBe s"$linkMessage$singleSpace$postLinkMessage$period"

        elementByClasses(component, defaultClasses).get(0).text() mustBe
          s"$linkMessage$singleSpace$postLinkMessage$period"

        shouldNotContainTheMessage(component, preLinkMessage)
        shouldContainTheMessage(component, postLinkMessage)
      }

      "there is no postLinkMessage" in new Setup {
        val component: Document =
          newTabLinkComponent(
            linkMessage = linkMessage,
            href = href,
            preLinkMessage = Some(preLinkMessage))

        elementByParagraph(component).text() mustBe s"$preLinkMessage$singleSpace$linkMessage$singleSpace$period"

        elementByClasses(component, defaultClasses).get(0).text() mustBe
          s"$preLinkMessage$singleSpace$linkMessage$singleSpace$period"

        shouldContainTheMessage(component, preLinkMessage)
        shouldNotContainTheMessage(component, postLinkMessage)
      }
    }
  }

  private def elementByParagraph(component: Document): Elements = {
    component.getElementsByTag("p")
  }

  private def elementByClasses(component: Document, classes: String): Elements = {
    component.getElementsByClass(classes)
  }

  private def shouldContainTheMessage(component: Document, msg: String): Assertion = {
    component.text().contains(msg) mustBe true
  }

  private def shouldNotContainTheMessage(component: Document, msg: String): Assertion = {
    component.text().contains(msg) mustBe false
  }

  trait Setup {
    val linkMessage: String = "go to test page"
    val href = "www.test.com"
    val preLinkMessage = "test_pre_link_message"
    val postLinkMessage = "test_post_link_message"
    val classes = "govuk-!-margin-bottom-7"
    val defaultClasses: String = "govuk-body"

    def newTabLinkComponent(linkMessage: String,
                            href: String,
                            preLinkMessage: Option[String] = None,
                            postLinkMessage: Option[String] = None,
                            classes: String = defaultClasses): Document =
      Jsoup.parse(buildApp.injector.instanceOf[newTabLink].
        apply(linkMessage, href, preLinkMessage, postLinkMessage, classes).body)

  }
}
