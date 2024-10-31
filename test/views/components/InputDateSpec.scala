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

import forms.CashTransactionsRequestPageFormProvider
import play.api.Application
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.SpecBase
import views.html.components.inputDate

import java.time.Clock

class InputDateSpec extends SpecBase {

  "InputDate component" should {

    "render correctly with no errors" in new Setup {

      val formWithValues = form.bind(
        Map(
          s"$id.day" -> "01",
          s"$id.month" -> "01",
          s"$id.year" -> "2021"
        )
      )

      running(app) {
        val inputDateView = app.injector.instanceOf[inputDate]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = false
        )(messages(app))

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("legend").text() must include(headline)
        html.getElementById(s"$id.day").attr(id) must include("01")
        html.getElementById(s"$id.month").attr(id) must include("01")
        html.getElementById(s"$id.year").attr(id) must include("2021")
        html.getElementsByTag("input").attr("class") mustNot include("govuk-input--error")
      }
    }

    "render h1 tag correctly" in new Setup {

      val formWithValues = form.bind(
        Map(
          s"$id.day" -> "01",
          s"$id.month" -> "01",
          s"$id.year" -> "2021"
        )
      )

      running(app) {
        val inputDateView = app.injector.instanceOf[inputDate]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = true
        )(messages(app))

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("h1").text() must include(headline)
        html.getElementById(s"$id.day").attr(id) must include("01")
        html.getElementById(s"$id.month").attr(id) must include("01")
        html.getElementById(s"$id.year").attr(id) must include("2021")
        html.getElementsByTag("input").attr("class") mustNot include("govuk-input--error")
      }
    }

    "render correctly with month error" in new Setup {

      val formWithValues = form.bind(
        Map(
          s"$id.day" -> "01",
          s"$id.month" -> "",
          s"$id.year" -> "2021"
        )
      )

      running(app) {
        val inputDateView = app.injector.instanceOf[inputDate]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = false
        )(messages(app))

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("legend").text() must include(headline)
        html.getElementById(s"$id.day").attr(id) must include("01")
        html.getElementById(s"$id.month").attr(id) mustNot include("01")
        html.getElementById(s"$id.year").attr(id) must include("2021")
        html.getElementsByTag("input").get(1).attr("class") must include("govuk-input--error")
      }
    }

    "render correctly with year error" in new Setup {

      val formWithValues = form.bind(
        Map(
          s"$id.day" -> "01",
          s"$id.month" -> "01",
          s"$id.year" -> ""
        )
      )

      running(app) {
        val inputDateView = app.injector.instanceOf[inputDate]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = false
        )(messages(app))

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("legend").text() must include(headline)
        html.getElementById(s"$id.day").attr(id) must include("01")
        html.getElementById(s"$id.month").attr(id) must include("01")
        html.getElementById(s"$id.year").attr(id) mustNot include("2021")
        html.getElementsByTag("input").get(2).attr("class") must include("govuk-input--error")
      }
    }

    "render correctly with both day, month and year errors" in new Setup {

      val formWithValues = form.bind(
        Map(
          s"$id.day" -> "",
          s"$id.month" -> "",
          s"$id.year" -> ""
        )
      )

      running(app) {
        val inputDateView = app.injector.instanceOf[inputDate]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = false
        )(messages(app))

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("legend").text() must include(headline)
        html.getElementById(s"$id.day").attr(id) mustNot include("01")
        html.getElementById(s"$id.month").attr(id) mustNot include("01")
        html.getElementById(s"$id.year").attr(id) mustNot include("2021")
        html.getElementsByTag("input").attr("class") must include("govuk-input--error")
      }
    }

    trait Setup {
      implicit val clk: Clock = Clock.systemUTC()
      val form = new CashTransactionsRequestPageFormProvider().apply()
      val id = "value"
      val headline = "Date of birth"

      val app: Application = buildApp
    }
  }
}
