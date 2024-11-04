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

import connectors.{CustomsDataStoreConnector, CustomsFinancialsApiConnector}
import models.CashTransactionDates
import models.email.UndeliverableEmail
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.auth.core.retrieve.Email
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class ConfirmationPageControllerSpec extends SpecBase {

  "calling page load returns valid response with email address" in new Setup {
    when(mockRequestedTransactionsCache.get(any)).thenReturn(Future.successful(Some(cashDates)))
    when(mockCustomsDataStoreConnector.getEmail(any)(any)).thenReturn(Future.successful(Right(Email(email))))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.ConfirmationPageController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK

      val content = contentAsString(result)
      content must include(emailParagraphId)
    }
  }

  "calling page load returns valid response without email address" in new Setup {
    when(mockRequestedTransactionsCache.get(any)).thenReturn(Future.successful(Some(cashDates)))
    when(mockCustomsDataStoreConnector.getEmail(any)(any)).thenReturn(Future.successful(Right(Email(emptyString))))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.ConfirmationPageController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK

      val content = contentAsString(result)
      content must not include emailParagraphId
    }
  }

  "calling page load returns valid response without email address for UndeliverableEmail" in new Setup {
    when(mockRequestedTransactionsCache.get(any)).thenReturn(Future.successful(Some(cashDates)))
    when(mockCustomsDataStoreConnector.getEmail(any)(any)).thenReturn(Future.successful(Left(UndeliverableEmail)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.ConfirmationPageController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK

      val content = contentAsString(result)
      content must not include emailParagraphId
    }
  }

  "calling page load returns error and redirects to cash account" in new Setup {
    when(mockRequestedTransactionsCache.get(any)).thenReturn(Future.successful(Some(emptyString)))
    when(mockCustomsDataStoreConnector.getEmail(any)(any)).thenReturn(Future.successful(Left(Email(emptyString))))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.ConfirmationPageController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.CashAccountController.showAccountUnavailable.url
    }
  }

  trait Setup {
    val emailParagraphId = "body-text-email"
    val email = "jackiechan@mail.com"

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockRequestedTransactionsCache: RequestedTransactionsCache = mock[RequestedTransactionsCache]
    val mockCustomsDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    val fromDate: LocalDate = LocalDate.parse("2023-03-01")
    val toDate: LocalDate = LocalDate.parse("2023-04-30")

    val cashDates: CashTransactionDates = CashTransactionDates(start = fromDate, end = toDate)

    val app: Application = applicationBuilder
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[RequestedTransactionsCache].toInstance(mockRequestedTransactionsCache),
        bind[CustomsDataStoreConnector].toInstance(mockCustomsDataStoreConnector))
      .configure("features.fixed-systemdate-for-tests" -> "true")
      .build()
  }
}
