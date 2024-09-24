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

import config.AppConfig
import connectors._
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.RequestedTransactionsCache
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class ConfirmationPageControllerSpec extends SpecBase {

  "call page load returns valid response" in new Setup {
    when(mockRequestedTransactionsCache.get(any)).thenReturn(Future.successful(Some(cashDates)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.ConfirmationPageController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK
    }
  }

  "calling pageload returns error and redirects to cash account" in new Setup {
    when(mockRequestedTransactionsCache.get(any)).thenReturn(Future.successful(Some("")))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.ConfirmationPageController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.CashAccountController.showAccountUnavailable.url
    }
  }

  trait Setup {

    val cashAccountNumber = "1234567"
    val eori = "exampleEori"
    val someCan = "1234567"
    val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockRequestedTransactionsCache: RequestedTransactionsCache = mock[RequestedTransactionsCache]

    val fromDate: LocalDate = LocalDate.parse("2023-03-01")
    val toDate: LocalDate = LocalDate.parse("2023-04-30")

    val cashDates = CashTransactionDates(start = fromDate, end = toDate)

    val app: Application = application
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[RequestedTransactionsCache].toInstance(mockRequestedTransactionsCache)
      )
      .configure("features.fixed-systemdate-for-tests" -> "true")
      .build()

    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
