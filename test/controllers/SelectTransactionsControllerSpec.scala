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

package controllers

import connectors.CustomsFinancialsApiConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.RequestedTransactionsCache
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class SelectTransactionsControllerSpec extends SpecBase {

  "onPageLoad" should {
    "return OK when cached data present" in new Setup {

      when(mockRequestedTransactionsCache.clear(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectTransactionsController.onPageLoad().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return OK when no cached data present" in new Setup {
      when(mockRequestedTransactionsCache.clear(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectTransactionsController.onPageLoad().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return OK when clearing cache" in new Setup {
      when(mockRequestedTransactionsCache.clear(any)).thenReturn(Future.successful(true))

      val store: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectTransactionsController.onSubmit().url)

      val clear: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectTransactionsController.onPageLoad().url)

      running(app) {
        val result = route(app, store).value
        val test = route(app, clear).value
        status(result) mustBe OK
        status(test) mustBe OK
      }
    }
  }

  "onSubmit" should {
    "redirect to requested transactions page when valid data has been submitted" in new Setup {
      when(mockRequestedTransactionsCache.set(any, any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> day, "start.month" -> "11", "start.year" -> "2020",
            "end.day" -> "10", "end.month" -> "11", "end.year" -> "2020")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SelectedTransactionsController.onPageLoad().url
      }
    }

    "return BAD_REQUEST when the start date is earlier than system start date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> day, "start.month" -> "9", "start.year" -> "2019",
            "end.day" -> "10", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is earlier than system start date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> day, "start.month" -> "10", "start.year" -> "2019",
            "end.day" -> "1", "end.month" -> "9", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is future date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> day, "start.month" -> "10", "start.year" -> "2021",
            "end.day" -> "1", "end.month" -> "9", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is future date" in new Setup {
      val date: String = LocalDate.now().plusYears(2).getYear.toString

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> day, "start.month" -> "10", "start.year" -> "2019",
            "end.day" -> "1", "end.month" -> "10", "end.year" -> date)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is after the end date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> day, "start.month" -> "11", "start.year" -> "2019",
            "end.day" -> "1", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the requested data exceeds 6 years in the past" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> "1", "start.month" -> "10", "start.year" -> "2000",
            "end.day" -> "1", "end.month" -> "10", "end.year" -> "2000")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when invalid data submitted" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> day, "start.invalid" -> "10", "start.year" -> "2019",
            "end.day" -> "1", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when start date and end date are empty" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.SelectTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody("start.day" -> day, "start.month" -> "", "start.year" -> "2019",
            "end.day" -> "1", "end.month" -> "", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  trait Setup {
    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockRequestedTransactionsCache: RequestedTransactionsCache = mock[RequestedTransactionsCache]

    val day: String = "1"

    val app: Application = applicationBuilder
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[RequestedTransactionsCache].toInstance(mockRequestedTransactionsCache)
      )
      .configure(
        "features.fixed-systemdate-for-tests" -> "true")
      .build()
  }
}
