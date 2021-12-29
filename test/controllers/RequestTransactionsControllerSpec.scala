/*
 * Copyright 2021 HM Revenue & Customs
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
import models._
import play.api.Application
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.RequestedTransactionsCache
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class RequestTransactionsControllerSpec extends SpecBase {

  "onPageLoad" should {
    "return OK when cached data present" in new Setup {
      when(mockRequestedTransactionsCache.get(any))
        .thenReturn(Future.successful(Some(CashTransactionDates(LocalDate.now(), LocalDate.now()))))


      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onPageLoad.url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return OK when no cached data present" in new Setup {
      when(mockRequestedTransactionsCache.get(any))
        .thenReturn(Future.successful(None))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onPageLoad.url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

  }

  "onSubmit" should {
    "return BAD_REQUEST when the start date is earlier than system start date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit.url)
          .withFormUrlEncodedBody("start.month" -> "9", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is earlier than system start date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit.url)
          .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "9", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is future date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit.url)
          .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2021", "end.month" -> "9", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is future date" in new Setup {
      val date: String = LocalDate.now().plusYears(2).getYear.toString

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit.url)
          .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> date)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is after the end date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit.url)
          .withFormUrlEncodedBody("start.month" -> "11", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the requested data exceeds 6 years in the past" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit.url)
          .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2000", "end.month" -> "10", "end.year" -> "2000")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when invalid data submitted" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit.url)
          .withFormUrlEncodedBody("start.invalid" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when start date and end date are empty" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit.url)
          .withFormUrlEncodedBody("start.month" -> "", "start.year" -> "2019", "end.month" -> "", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  trait Setup {
    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockRequestedTransactionsCache: RequestedTransactionsCache = mock[RequestedTransactionsCache]

    val app: Application = application
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[RequestedTransactionsCache].toInstance(mockRequestedTransactionsCache)
      )
      .configure(
        "features.fixed-systemdate-for-tests" -> "true")
      .build()
  }
}


