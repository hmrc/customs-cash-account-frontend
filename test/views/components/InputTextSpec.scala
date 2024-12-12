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

import forms.SearchTransactionsFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.FormGroup
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utils.Utils.{DetailsHint, InputTextHint, LabelHint}
import views.html.components.{button, inputText}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukButton
import utils.SpecBase

class InputTextSpec extends SpecBase {

  "InputText" should {
    "display the correct view" in new Setup {
      val view: Document = Jsoup.parse(
        application.injector
          .instanceOf[inputText]
          .apply(
            form = validForm,
            id = "value",
            name = "value",
            label = testLabel,
            isPageHeading = false,
            hint = None
          )
          .body
      )

      view.getElementsByTag("label").html() mustBe messages("eoriNumber.heading")
      view.getElementById("value").`val`() mustBe "GB123456789012"

      intercept[RuntimeException] {
        view.getElementById("value-hint").html()
      }
    }

    "display the correct hint" in new Setup {
      val view: Document = Jsoup.parse(
        application.injector
          .instanceOf[inputText]
          .apply(
            form = validForm,
            id = "value",
            name = "value",
            label = testLabel,
            isPageHeading = false,
            hint =
              Option(InputTextHint(Option(DetailsHint(detailsSummaryText, detailsText)), Option(LabelHint(labelText))))
          )
          .body
      )

      val detailsHintElement: Element = view.getElementById("value-hint")

      detailsHintElement.getElementById("value-hint-text").html() mustBe labelText
    }

    "display the inline component if any component is passed as part of FormGroup" in new Setup {
      val button: HtmlFormat.Appendable = new button(new GovukButton()).apply(msg = buttonText)

      val view: Document = Jsoup.parse(
        application.injector
          .instanceOf[inputText]
          .apply(
            form = invalidForm,
            id = "value",
            name = "value",
            label = testLabel,
            isPageHeading = false,
            hint = None,
            formGroup = FormGroup(afterInput = Some(HtmlContent(button)))
          )
          .body
      )

      val inLineComponent: Elements = view.getElementsByClass("govuk-button")

      inLineComponent.get(0).text() mustBe buttonText
    }

    "display error if form has any error" in new Setup {
      val view: Document = Jsoup.parse(
        application.injector
          .instanceOf[inputText]
          .apply(
            form = invalidForm,
            id = "value",
            name = "value",
            label = testLabel,
            isPageHeading = false,
            hint = None
          )
          .body
      )

      view.getElementById("value-error").childNodes().size() must be > 0
      view.getElementsByClass("govuk-visually-hidden").html() mustBe "Error:"
    }
  }

  trait Setup {
    val validForm: Form[String]   = new SearchTransactionsFormProvider().apply().bind(Map("value" -> "GB123456789012"))
    val invalidForm: Form[String] = new SearchTransactionsFormProvider().apply().bind(Map("value" -> emptyString))

    val testLabel          = "eoriNumber.heading"
    val detailsSummaryText = "summaryText"
    val detailsText        = "text"
    val labelText          = "labelText"
    val buttonText         = "testButton"
  }
}
