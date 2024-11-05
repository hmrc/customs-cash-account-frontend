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

import org.jsoup.nodes.Document
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase

trait GuidancePageBehaviour {

  self: SpecBase =>

  val view: Document
  val titleMsgKey: String
  val backLink: Option[String] = None
  val componentIdsToVerify: List[String] = List.empty
  val helpAndSupportMsgKeys: Option[List[String]] = None
  val helpAndSupportLink: Option[String] = None
  val otherComponentGuidanceList: List[ComponentDetailsForAssertion] = List.empty
  val linksToVerify: List[LinkDetails] = List.empty

  implicit lazy val app: Application = application
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()

  def guidancePage(): Unit =

    "display correct title" in {
      view.title() mustBe s"${messages(titleMsgKey)} - ${messages("service.name")} - GOV.UK"
    }

    "display correct back link" in {
      backLink.map(url => view.html() must include(url))
    }

    "display correct help and support guidance" in {
      if (helpAndSupportMsgKeys.isDefined) {
        helpAndSupportMsgKeys.map(msgsKey => view.html().contains(messages(msgsKey)) mustBe true)
      } else {
        val viewAsHtml = view.html()

        viewAsHtml must include(messages("cf.cash-account.help-and-support.link.text"))
        viewAsHtml must include(messages("cf.cash-account.help-and-support.link.text.post"))
        viewAsHtml must include(messages("cf.cash-account.help-and-support.link.text.pre"))
        viewAsHtml must include(
          helpAndSupportLink.getOrElse("https://www.gov.uk/guidance/use-a-cash-account-for-cds-declarations"))
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
          if (component.id.isDefined) {
            view.getElementById(component.id.getOrElse(emptyString)).text() mustBe component.expectedValue
          } else {
            view.html() must include(component.expectedValue)
          }
        }
    }

    linksToVerify.foreach {
      link =>
        s"display the link for ${link.urlText}" in {
          if (link.id.isDefined) {
            val linkElement = view.getElementById(link.id.getOrElse(emptyString))

            linkElement.html() must include(link.url)
            linkElement.html() must include(link.urlText)
          } else {
            view.html() must include(link.url)
            view.html() must include(link.urlText)
          }
        }
    }

    "display the deskpro link" in {
      val deskProLinkClass = "hmrc-report-technical-issue"

      view.select(s".$deskProLinkClass").text() mustBe "Is this page not working properly? (opens in new tab)"
    }
}

case class ComponentDetailsForAssertion(testDescription: String,
                                        id: Option[String] = None,
                                        expectedValue: String)

case class LinkDetails(url: String,
                       urlText: String,
                       id: Option[String] = None,
                       classes: Option[String] = None)
