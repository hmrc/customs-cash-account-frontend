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

package behaviours

import config.AppConfig
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase

trait StandardPageBehaviour {

  self: SpecBase =>

  val view: Document
  val titleMsgKey: String
  val backLink: Option[String] = None
  val componentIdsToVerify: List[String] = List.empty
  val helpAndSupportMsgKeys: Option[List[String]] = None
  val helpAndSupportLink: Option[String] = None
  val otherComponentGuidanceList: List[ComponentDetailsForAssertion] = List.empty

  implicit lazy val app: Application = application.build()
  implicit val msgs: Messages = messages(app)
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()
  implicit val appConfig: AppConfig = appConfig(app)

  def standardPage(): Unit =

      "display correct title" in {
        view.title() mustBe s"${msgs(titleMsgKey)} - ${msgs("service.name")} - GOV.UK"
      }

      "display correct back link" in {
        backLink.map(url => view.html().contains(url) mustBe true)
      }

      "display correct help and support guidance" in {
        if(helpAndSupportMsgKeys.isDefined) {
          helpAndSupportMsgKeys.map(msgsKey => view.html().contains(msgs(msgsKey)) mustBe true)
        } else {
          val viewAsHtml = view.html()

          viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text")) mustBe true
          viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.post")) mustBe true
          viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.pre")) mustBe true
          viewAsHtml.contains(
            helpAndSupportLink.getOrElse("https://www.gov.uk/guidance/use-a-cash-account-for-cds-declarations")
          ) mustBe true
        }
      }

      componentIdsToVerify.foreach {
        id =>
          s"display component with id $id" in {
            view.select(s"#$id").size() must be > 0
          }
      }

      otherComponentGuidanceList.foreach {
        component =>
          component.testDescription in {
            if(component.id.isDefined) {
              view.getElementById(component.id.getOrElse(emptyString)).text() mustBe component.expectedValue
            } else {
              view.html().contains(component.expectedValue)
            }
          }
      }
}

case class ComponentDetailsForAssertion(testDescription: String,
                                        id: Option[String] = None,
                                        expectedValue: String)

