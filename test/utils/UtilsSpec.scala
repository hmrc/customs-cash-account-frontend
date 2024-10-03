/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import config.AppConfig
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTable
import utils.Utils.*
import views.html.components.{h1, h1Inner, h2, h2Inner, link, newTabLink, p}

class UtilsSpec extends SpecBase {
  "Comma" should {
    "return correct value" in {
      comma mustBe ","
    }
  }

  "Empty string" should {
    "return correct value" in {
      emptyString mustBe ""
    }
  }

  "Period" should {
    "return correct value" in {
      period mustBe "."
    }
  }

  "hyphen" should {
    "return correct value" in {
      hyphen mustBe "-"
    }
  }

  "singleSpace" should {
    "return correct value" in {
      singleSpace mustBe " "
    }
  }

  "negativeSign" should {
    "return correct value" in {
      negativeSign mustBe "-"
    }
  }

  "poundSymbol" should {
    "return correct value" in {
      poundSymbol mustBe "£"
    }
  }
  
  "emptyH1Component" should {
    "return the empty h1 component" in {
      emptyH1Component mustBe new h1()
    }
  }

  "emptyH2Component" should {
    "return the empty h2 component" in {
      emptyH2Component mustBe new h2()
    }
  }

  "h2Component" should {
    "create the component correctly with provided input" in new Setup {
      h2Component(msgKey = testMsg, id = Some(testId)) mustBe
        new h2().apply(msg = testMsg, id = Some(testId))

      h2Component(msgKey = testMsg, id = Some(testId), classes = testClass) mustBe
        new h2().apply(msg = testMsg, id = Some(testId), classes = testClass)
    }
  }

  "emptyLinkComponent" should {
    "return the empty link component" in {
      emptyLinkComponent mustBe new link()
    }
  }

  "linkComponent" should {
    "create the component correctly with provided input" in new Setup {
      val result: HtmlFormat.Appendable = linkComponent(LinkComponentValues(pId = Some(testId),
        linkMessageKey = testMsgKey,
        location = testLocation,
        linkClass = testClass,
        preLinkMessageKey = Some(testMsgKey)))

      result mustBe new link().apply(linkMessageKey = testMsgKey,
        location = testLocation,
        linkClass = testClass,
        preLinkMessage = Some(testMsgKey),
        pId = Some(testId)
      )
    }
  }

  "emptyPComponent" should {
    "return the empty p component" in {
      emptyPComponent mustBe new p()
    }
  }

  "pComponent" should {
    "create the component correctly with provided input" in new Setup {
      val result: HtmlFormat.Appendable = pComponent(
        messageKey = testMsgKey,
        id = Some(testId),
        classes = testClass,
        bold = true)

      result mustBe new p().apply(
        message = testMsgKey,
        id = Some(testId),
        classes = testClass,
        bold = true)
    }
  }

  "hmrcNewTabLinkComponent" should {
    "create the component correctly with provided input" in new Setup {
      val result: HtmlFormat.Appendable = hmrcNewTabLinkComponent(linkMessage,
        href,
        Some(preLinkMessage),
        Some(postLinkMessage),
        classes)

      result mustBe new newTabLink(emptyHmrcNewTabLink).apply(linkMessage,
        href,
        Some(preLinkMessage),
        Some(postLinkMessage),
        classes = classes)
    }
  }

  "emptyGovUkTableComponent" should {
    "return the empty GovukTable" in {
      emptyGovUkTableComponent mustBe new GovukTable()
    }
  }

  "emptyH2InnerComponent" should {
    "return the empty h2Inner component" in {
      emptyH2InnerComponent mustBe new h2Inner()
    }
  }

  "emptyH1InnerComponent" should {
    "return the empty h1Inner component" in {
      emptyH1InnerComponent mustBe new h1Inner()
    }
  }

  "prependNegativeSignWithAmount" should {
    "prepend - sign with amount" in {
      val testAmount = "400.00"

      prependNegativeSignWithAmount(testAmount) mustBe "-400.00"
    }
  }

  trait Setup {
    val app: Application = application.build()
    implicit val msgs: Messages = messages(app)
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]

    val testMsgKey = "test_key"
    val testMsg = "test_msg"
    val testId = "test_id"
    val testClass = "test_class"
    val testLocation = "test_location"
    val testHref = "http://www.test.com"
    val testLang = "en"

    val linkMessage: String = "go to test page"
    val href = "www.test.com"
    val preLinkMessage = "test_pre_link_message"
    val postLinkMessage = "test_post_link_message"
    val classes = "govuk-!-margin-bottom-7"
  }
}
