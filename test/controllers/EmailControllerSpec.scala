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

import connectors.CustomsDataStoreConnector
import models.email.{EmailUnverifiedResponse, EmailVerifiedResponse}
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.test.Helpers.{GET, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase

import scala.concurrent.Future

import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any

class EmailControllerSpec extends SpecBase {

  "showUnverified" must {

    "return unverified email response" in new Setup {

      when(mockConnector.retrieveUnverifiedEmail(any)).thenReturn(Future.successful(emailUnverifiedResponse))

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)

        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "display verify your email page when exception occurs while connector making the API call" in new Setup {

      when(mockConnector.retrieveUnverifiedEmail(any))
        .thenReturn(Future.successful(emailUnverifiedResponseWithNoEmailId))

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)

        val result = route(app, request).value
        status(result) mustBe OK
      }
    }
  }

  "showUndeliverable" must {

    "display undeliverableEmail page" in new Setup {

      when(mockConnector.verifiedEmail(any)).thenReturn(Future.successful(emailVerifiedResponse))

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUndeliverable().url)
        val result  = route(app, request).value

        status(result) mustBe OK
      }
    }

    "display undeliverableEmail page when exception occurs while connector is making the API call" in new Setup {

      when(mockConnector.verifiedEmail(any)).thenReturn(Future.successful(emailVerifiedResponseWithNoEmailId))

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUndeliverable().url)
        val result  = route(app, request).value

        status(result) mustBe OK
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier               = HeaderCarrier()
    val mockConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    val emailId                                                   = "test@test.com"
    val emailVerifiedResponse: EmailVerifiedResponse              = EmailVerifiedResponse(Some(emailId))
    val emailVerifiedResponseWithNoEmailId: EmailVerifiedResponse = EmailVerifiedResponse(None)

    val emailUnverifiedResponse: EmailUnverifiedResponse              = EmailUnverifiedResponse(Some(emailId))
    val emailUnverifiedResponseWithNoEmailId: EmailUnverifiedResponse = EmailUnverifiedResponse(None)

    val app: Application = applicationBuilder
      .overrides(
        bind[CustomsDataStoreConnector].toInstance(mockConnector)
      )
      .build()
  }
}
