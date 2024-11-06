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

import views.html.components.link
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import utils.SpecBase

class LinkSpec extends SpecBase {

  "component" should {

    "display correct contents" when {

      "it contains all the mandatory parameters" in new Setup {
        linkComponent.getElementById(linkId).text() mustBe linkMessageKey
      }

      "it contains all the mandatory parameters along with pId" in new Setup {
        linkComponentWithPId.getElementById(pId).text() mustBe linkMessageKey
      }

      "it contains all the mandatory parameters along with pre and post link messages" in new Setup {
        linkComponentWithPreAndPostLinkMsg.getElementById(pId).text().contains(testPreLinkMsg)
        linkComponentWithPreAndPostLinkMsg.getElementById(pId).text().contains(testPostLinkMsg)
      }

      "it contains linkMessage string instead of linkMessage msg key" in new Setup {
        linkComponentWithLinkMessage.getElementById(linkId).text() mustBe linkMessageString
      }

    }
  }

  trait Setup {
    val space = " "
    val linkMessageKey = "test_msg"
    val linkMessageString = "test_msg_string"
    val location = "test@test.com"
    val linkId: String = "test_id"
    val pId = "test_pid"
    val testPreLinkMsg = "preLink_message"
    val testPostLinkMsg = "postLink_message"

    val linkComponent: Document =
      Jsoup.parse(application.injector.instanceOf[link].apply(linkMessageKey, location, Some(linkId)).body)

    val linkComponentWithPId: Document =
      Jsoup.parse(application.injector.instanceOf[link].apply(
        linkMessageKey = linkMessageKey, location = location, pId = Some(pId)).body)

    val linkComponentWithPreAndPostLinkMsg: Document = Jsoup.parse(application.injector.instanceOf[link].apply(
      linkMessageKey = linkMessageKey,
      location = location,
      pId = Some(pId),
      preLinkMessage = Some(testPreLinkMsg),
      postLinkMessage = Some(testPostLinkMsg)).body)

    val linkComponentWithLinkMessage: Document = Jsoup.parse(application.injector.instanceOf[link].apply(
      linkMessageKey = linkMessageKey,
      location = location,
      linkId = Some(linkId),
      pId = Some(pId),
      preLinkMessage = Some(testPreLinkMsg),
      postLinkMessage = Some(testPostLinkMsg),
      linkMessage = Some(linkMessageString)).body)
  }
}
