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

package config

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import utils.TestData.{testHeading, testMessage, testTitle}
import views.html.{ErrorTemplate, not_found}
import play.api.test.CSRFTokenHelper.CSRFRequest

class ErrorHandlerSpec extends SpecBase {
  "standErrorTemplate" should {
    "render correct contents" in new Setup {
      val errorTemplateView: ErrorTemplate = application.injector.instanceOf[ErrorTemplate]

      errorHandler.standardErrorTemplate(testTitle, testHeading, testMessage).map { errorTemplate =>
        errorTemplate mustBe errorTemplateView(testTitle, testHeading, testMessage)

        val docView: Document = Jsoup.parse(errorTemplate.body)
        docView.getElementsByClass("govuk-heading-xl").text mustBe testHeading
        docView.getElementsByClass("govuk-body").text mustBe testMessage
      }
    }
  }

  "notFoundTemplate" should {
    "render correct contents" in new Setup {
      val notFoundView: not_found = application.injector.instanceOf[not_found]

      errorHandler.notFoundTemplate.map { notFoundTemplate =>
        notFoundTemplate.toString mustBe notFoundView.apply().body
      }
    }
  }

  trait Setup {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "test_path")

    def fakeRequest(method: String = emptyString, path: String = emptyString): FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(method, path).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    val errorHandler: ErrorHandler                     = application.injector.instanceOf[ErrorHandler]
  }
}
