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
import models.email.{UndeliverableEmail, UnverifiedEmail}
import models.{AccountStatusOpen, CDSCashBalance, CashAccount}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.retrieve.Email
import utils.SpecBase

import scala.concurrent.Future

class CashAccountControllerSpec extends SpecBase {

  "show account details" must {
    "always redirect to the permanent Cash Account page" in new Setup {
      val app: Application = applicationBuilder
        .overrides(bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(None).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.CashAccountV2Controller.showAccountDetails(None).url
      }
    }

    "preserve the requested page when redirecting" in new Setup {
      val app: Application = applicationBuilder
        .overrides(bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(5)).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.CashAccountV2Controller.showAccountDetails(Some(5)).url
      }
    }

    "continue to redirect users with unverified email" in new Setup {
      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Left(UnverifiedEmail)))

      val app: Application = applicationBuilder
        .overrides(bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(None).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.EmailController.showUnverified().url
      }
    }

    "continue to redirect users with undeliverable email" in new Setup {
      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Left(UndeliverableEmail("cash@example.com"))))

      val app: Application = applicationBuilder
        .overrides(bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(None).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.EmailController.showUndeliverable().url
      }
    }
  }

  "showAccountUnavailable" must {
    "return OK" in new Setup {
      val app: Application = applicationBuilder
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountUnavailable.url)
        val result  = route(app, request).value

        status(result) mustEqual OK
      }
    }
  }

  "showUnableToDownloadCSV" must {

    "return OK" in new Setup {
      val app: Application = applicationBuilder
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.showUnableToDownloadCSV().url)
        val result  = route(app, request).value

        status(result) mustEqual OK
      }
    }
  }

  "tooManyTransactions" must {
    "return OK" in new Setup {

      val app: Application = applicationBuilder
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      when(mockCustomsFinancialsApiConnector.getCashAccount(any)(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.tooManyTransactions().url)
        val result  = route(app, request).value

        status(result) mustEqual OK
      }
    }

    "return NotFound" in new Setup {

      val app: Application = applicationBuilder
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      when(mockCustomsFinancialsApiConnector.getCashAccount(any)(any, any))
        .thenReturn(Future.successful(None))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.tooManyTransactions().url)
        val result  = route(app, request).value

        status(result) mustEqual NOT_FOUND
      }
    }
  }

  trait Setup {
    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    when(mockDataStoreConnector.getEmail(any)(any))
      .thenReturn(Future.successful(Right(Email("cash@example.com"))))

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]

    val cashAccount: CashAccount =
      CashAccount("1234567", "exampleEori", AccountStatusOpen, CDSCashBalance(Some(BigDecimal(123456.78))))
  }
}
