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
import org.jsoup.nodes.{Document, Element}
import utils.SpecBase
import utils.Utils.notificationPanelComponent

class NotificationPanelSpec extends SpecBase {

  "NotificationPanel component" should {

    "display the notification panel with correct contents" when {

      "showNotification is true" in new Setup {
        override val showNotification: Boolean = true

        val notificationDoc: Document = Jsoup.parse(renderedView)

        val notificationPanel: Element = notificationDoc.getElementById("notification-panel")
        notificationPanel must not be None
        notificationPanel.hasClass("notifications-panel") mustBe true

        val paragraph: Element = notificationPanel.select("p.govuk-body.govuk-\\!-margin-bottom-1").first()
        paragraph must not be None
        paragraph.text() mustBe s"$preMessage $linkText $postMessage"

        val link: Element = paragraph.select("a.govuk-link").first()
        link must not be None
        link.attr("href") mustBe linkUrl
        link.text() mustBe linkText
      }
    }

    "not display the notification panel" when {

      "showNotification is false" in new Setup {
        override val showNotification: Boolean = false

        val notificationDoc: Document = Jsoup.parse(renderedView)

        val notificationPanel: Element = notificationDoc.getElementById("notification-panel")
        notificationPanel must not be true
      }
    }
  }

  trait Setup {

    val showNotification: Boolean
    val preMessage: String = "preMessage"
    val linkUrl: String = "linkUrl"
    val linkText: String = "linkText"
    val postMessage: String = "postMessage"

    lazy val renderedView: String = {
      notificationPanelComponent(
        showNotification = showNotification,
        preMessage = preMessage,
        linkUrl = linkUrl,
        linkText = linkText,
        postMessage = postMessage
      ).body
    }
  }
}
