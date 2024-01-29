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

import helpers.FormHelper.updateFormErrorKeyForStartAndEndDate
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers
import play.api.{Application, inject}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.html.components.GovukErrorSummary
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.{ErrorLink, ErrorSummary}
import utils.SpecBase
import views.html.components.errorSummary

class ErrorSummarySpec extends SpecBase {

  "ErrorSummary component" must {

    "show correct error with unchanged key when isErrorKeyUpdateEnabled is false" in new SetUp {
      val errorSum: ErrorSummary = ErrorSummary(
        errorList = Seq(ErrorLink(Some("#start"), content = Text(msgs("cf.form.error.start.date-number-invalid")))),
        title = Text(msgs("error.summary.title"))
      )

      val govSummaryHtmlFormat: HtmlFormat.Appendable = new GovukErrorSummary().apply(errorSum)

      when(mockGovSummary.apply(any[ErrorSummary])).thenReturn(govSummaryHtmlFormat)

      val app: Application = application.overrides(
        inject.bind[GovukErrorSummary].toInstance(mockGovSummary)
      ).build()

      val view: errorSummary = app.injector.instanceOf[errorSummary]
      val formErrors: Seq[FormError] = Seq(FormError("start", "cf.form.error.start.date-number-invalid"))

      val result: HtmlFormat.Appendable = view(formErrors, None)

      result.toString().contains(
        "<a href=\"#start\">cf.form.error.start.date-number-invalid</a>") shouldBe true
      result.toString().contains("error.summary.title") shouldBe true
    }

    "show correct error with updated key when key has value start, isErrorKeyUpdateEnabled is true and " +
      "updateFormErrorKeyForTheMessage function is provided" in new SetUp {

      val errorSum: ErrorSummary = ErrorSummary(
        errorList = Seq(ErrorLink(Some("#start.day"), content = Text(msgs("cf.form.error.start-future-date")))),
        title = Text(msgs("error.summary.title"))
      )

      val govSummaryHtmlFormat: HtmlFormat.Appendable = new GovukErrorSummary().apply(errorSum)

      when(mockGovSummary.apply(any[ErrorSummary])).thenReturn(govSummaryHtmlFormat)

      val app: Application = application.overrides(
        inject.bind[GovukErrorSummary].toInstance(mockGovSummary)
      ).build()

      val view: errorSummary = app.injector.instanceOf[errorSummary]
      val formErrors: Seq[FormError] = Seq(FormError("start", "cf.form.error.start-future-date"))

      val result: HtmlFormat.Appendable = view(
        formErrors,
        None,
        isErrorKeyUpdateEnabled = true,
        updateFormErrorKeyForTheMessage = Some(updateFormErrorKeyForStartAndEndDate()))

      result.toString().contains(
        "<a href=\"#start.day\">cf.form.error.start-future-date</a>") shouldBe true
      result.toString().contains("error.summary.title") shouldBe true
    }

    "show correct error with updated key when key has value start,error msg key is cf.form.error.year.length " +
      " isErrorKeyUpdateEnabled is true and updateFormErrorKeyForTheMessage function is provided" in new SetUp {

      val errorSum: ErrorSummary = ErrorSummary(
        errorList = Seq(ErrorLink(Some("#start.year"), content = Text(msgs("cf.form.error.year.length")))),
        title = Text(msgs("error.summary.title"))
      )

      val govSummaryHtmlFormat: HtmlFormat.Appendable = new GovukErrorSummary().apply(errorSum)

      when(mockGovSummary.apply(any[ErrorSummary])).thenReturn(govSummaryHtmlFormat)

      val app: Application = application.overrides(
        inject.bind[GovukErrorSummary].toInstance(mockGovSummary)
      ).build()

      val view: errorSummary = app.injector.instanceOf[errorSummary]
      val formErrors: Seq[FormError] = Seq(FormError("start", "cf.form.error.year.length"))

      val result: HtmlFormat.Appendable = view(
        formErrors,
        None,
        isErrorKeyUpdateEnabled = true,
        updateFormErrorKeyForTheMessage = Some(updateFormErrorKeyForStartAndEndDate()))

      result.toString().contains(
        "<a href=\"#start.year\">cf.form.error.year.length</a>") shouldBe true
      result.toString().contains("error.summary.title") shouldBe true
    }

    "show correct error with updated key when key has value of end, isErrorKeyUpdateEnabled is true and " +
      "updateFormErrorKeyForTheMessage function is provided" in new SetUp {

      val errorSum: ErrorSummary = ErrorSummary(
        errorList = Seq(ErrorLink(Some("#end.day"), content = Text(msgs("cf.form.error.end-future-date")))),
        title = Text(msgs("error.summary.title"))
      )

      val govSummaryHtmlFormat: HtmlFormat.Appendable = new GovukErrorSummary().apply(errorSum)

      when(mockGovSummary.apply(any[ErrorSummary])).thenReturn(govSummaryHtmlFormat)

      val app: Application = application.overrides(
        inject.bind[GovukErrorSummary].toInstance(mockGovSummary)
      ).build()

      val view: errorSummary = app.injector.instanceOf[errorSummary]
      val formErrors: Seq[FormError] = Seq(FormError("end", "cf.form.error.end-future-date"))

      val result: HtmlFormat.Appendable = view(
        formErrors,
        None,
        isErrorKeyUpdateEnabled = true,
        updateFormErrorKeyForTheMessage = Some(updateFormErrorKeyForStartAndEndDate()))

      result.toString().contains(
        "<a href=\"#end.day\">cf.form.error.end-future-date</a>") shouldBe true
      result.toString().contains("error.summary.title") shouldBe true
    }

    "show correct error with updated key when key has value end,error msg key is cf.form.error.year.length " +
      " isErrorKeyUpdateEnabled is true and updateFormErrorKeyForTheMessage function is provided" in new SetUp {

      val errorSum: ErrorSummary = ErrorSummary(
        errorList = Seq(ErrorLink(Some("#end.year"), content = Text(msgs("cf.form.error.year.length")))),
        title = Text(msgs("error.summary.title"))
      )

      val govSummaryHtmlFormat: HtmlFormat.Appendable = new GovukErrorSummary().apply(errorSum)

      when(mockGovSummary.apply(any[ErrorSummary])).thenReturn(govSummaryHtmlFormat)

      val app: Application = application.overrides(
        inject.bind[GovukErrorSummary].toInstance(mockGovSummary)
      ).build()

      val view: errorSummary = app.injector.instanceOf[errorSummary]
      val formErrors: Seq[FormError] = Seq(FormError("end", "cf.form.error.year.length"))

      val result: HtmlFormat.Appendable = view(
        formErrors,
        None,
        isErrorKeyUpdateEnabled = true,
        updateFormErrorKeyForTheMessage = Some(updateFormErrorKeyForStartAndEndDate()))

      result.toString().contains(
        "<a href=\"#end.year\">cf.form.error.year.length</a>") shouldBe true
      result.toString().contains("error.summary.title") shouldBe true
    }
  }
}

trait SetUp {
  implicit val msgs: Messages = Helpers.stubMessages()
  val mockGovSummary: GovukErrorSummary = mock[GovukErrorSummary]
}
