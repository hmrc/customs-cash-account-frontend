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
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import utils.Utils.{DetailsHint, InputTextHint, LabelHint}
import views.html.components.inputText
import utils.SpecBase

class InputTextSpec extends SpecBase {

  "InputText" should {
    "display the correct view" in new Setup {
      val view: Document = Jsoup.parse(app.injector.instanceOf[inputText].apply(
        form = validForm,
        id = "value",
        name = "value",
        label = "eoriNumber.heading",
        isPageHeading = false,
        hint = None
      ).body)

      view.getElementsByTag("label").html() mustBe messages(app)("eoriNumber.heading")
      view.getElementById("value").`val`() mustBe "GB123456789012"

      intercept[RuntimeException] {
        view.getElementById("value-hint").html()
      }
    }

    "display the correct hint" in new Setup {
      val view: Document = Jsoup.parse(app.injector.instanceOf[inputText].apply(
        form = validForm,
        id = "value",
        name = "value",
        label = "eoriNumber.heading",
        isPageHeading = false,
        hint = Option(InputTextHint(
          Option(DetailsHint(detailsSummaryText, detailsText)), Option(LabelHint(labelText))))
      ).body)

      val detailsHintElement: Element = view.getElementById("value-hint-details")

      detailsHintElement.getElementById("value-hint-title").html() mustBe
        detailsSummaryText
      detailsHintElement.getElementsByClass("govuk-body").html() mustBe
        detailsText
    }

    "display error if form has any error" in new Setup {
      val view: Document = Jsoup.parse(app.injector.instanceOf[inputText].apply(
        form = invalidForm,
        id = "value",
        name = "value",
        label = "eoriNumber.heading",
        isPageHeading = false,
        hint = None
      ).body)

      view.getElementById("value-error").childNodes().size() must be > 0
      view.getElementsByClass("govuk-visually-hidden").html() mustBe "Error:"
    }
  }

  trait Setup {
    val app: Application = application.build()
    implicit val msg: Messages = messages(app)

    val validForm: Form[String] = new SearchTransactionsFormProvider().apply().bind(Map("value" -> "GB123456789012"))
    val invalidForm: Form[String] = new SearchTransactionsFormProvider().apply().bind(Map("value" -> "3456789012"))

    val detailsSummaryText = "summaryText"
    val detailsText = "text"
    val labelText = "labelText"
  }
}
