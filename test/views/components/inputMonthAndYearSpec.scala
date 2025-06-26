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

import forms.SelectTransactionsFormProvider
import models.CashTransactionDates
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.Helpers.*
import play.twirl.api.HtmlFormat
import utils.SpecBase
import views.html.components.inputMonthAndYear

import java.time.Clock

class inputMonthAndYearSpec extends SpecBase {

  "InputMonthAndYear component" should {
    "render correctly with no errors" in new Setup {

      val formWithValues: Form[CashTransactionDates] = form.bind(
        Map(
          s"$startKey.month" -> "01",
          s"$startKey.year"  -> "2021"
        )
      )

      running(application) {
        val inputDateView = application.injector.instanceOf[inputMonthAndYear]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = startKey,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = false
        )(messages)

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("legend").text()              must include(headline)
        html.getElementById(s"$startKey.month").attr(value) must include("01")
        html.getElementById(s"$startKey.year").attr(value)  must include("2021")
        html.getElementsByTag("input").attr("class") mustNot include("govuk-input--error")
      }
    }

    "render correctly with month error" in new Setup {

      val formWithValues: Form[CashTransactionDates] = form.bind(
        Map(
          s"$startKey.month" -> "",
          s"$startKey.year"  -> "2021"
        )
      )

      running(application) {
        val inputDateView = application.injector.instanceOf[inputMonthAndYear]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = startKey,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = false
        )(messages)

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("legend").text()              must include(headline)
        html.getElementById(s"$startKey.month").attr(value) mustNot include("01")
        html.getElementById(s"$startKey.year").attr(value)  must include("2021")
        html.getElementsByTag("input").get(0).attr("class") must include("govuk-input--error")
      }
    }

    "render correctly with year error" in new Setup {

      val formWithValues: Form[CashTransactionDates] = form.bind(
        Map(
          s"$startKey.month" -> "01",
          s"$startKey.year"  -> ""
        )
      )

      running(application) {
        val inputDateView = application.injector.instanceOf[inputMonthAndYear]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = startKey,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = false
        )(messages)

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("legend").text()              must include(headline)
        html.getElementById(s"$startKey.month").attr(value) must include("01")
        html.getElementById(s"$startKey.year").attr(value) mustNot include("2021")
        html.getElementsByTag("input").get(1).attr("class") must include("govuk-input--error")
      }
    }

    "render correctly with both month and year errors" in new Setup {

      val formWithValues: Form[CashTransactionDates] = form.bind(
        Map(
          s"$startKey.month" -> "",
          s"$startKey.year"  -> ""
        )
      )

      running(application) {
        val inputDateView = application.injector.instanceOf[inputMonthAndYear]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = startKey,
          legendHiddenContent = None,
          legendClasses = "legend-class",
          hintText = None,
          legendAsPageHeading = false
        )(messages)

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("legend").text()       must include(headline)
        html.getElementById(s"$startKey.month").attr(value) mustNot include("01")
        html.getElementById(s"$startKey.year").attr(value) mustNot include("2021")
        html.getElementsByTag("input").attr("class") must include("govuk-input--error")
      }
    }

    trait Setup {

      implicit val clk: Clock              = Clock.systemUTC()
      val form: Form[CashTransactionDates] = new SelectTransactionsFormProvider().apply()
      val value                            = "value"
      val startKey: String                 = "start"
      val headline: String                 = "Date of birth"
    }
  }
}
