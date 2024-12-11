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

package controllers.actions

import connectors.CustomsDataStoreConnector
import models.email.{UndeliverableEmail, UnverifiedEmail}
import models.request.IdentifierRequest
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.ServiceUnavailableException
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any

class EmailActionSpec extends SpecBase {

  "filter" should {

    "let requests with validated email through" in new Setup {
      running(app) {
        when(mockDataStoreConnector.getEmail(any)(any))
          .thenReturn(Future.successful(Right(Email("last.man@standing.co.uk"))))

        emailAction.filter(authenticatedRequest).map {
          _ mustBe None
        }
      }
    }

    "let request through, when getEmail throws service unavailable exception" in new Setup {
      running(app) {
        when(mockDataStoreConnector.getEmail(any)(any))
          .thenReturn(Future.failed(new ServiceUnavailableException(emptyString)))

        emailAction.filter(authenticatedRequest).map {
          _ mustBe None
        }
      }
    }

    "redirect users to email verified page when UnverifiedEmail response is returned" in new Setup {
      running(app) {
        when(mockDataStoreConnector.getEmail(any)(any)).thenReturn(Future.successful(Left(UnverifiedEmail)))

        val result = emailAction.filter(authenticatedRequest).map(_.get)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.EmailController.showUnverified().url)
      }
    }

    "redirect users to undelivered email page when undeliverable email response is returned" in new Setup {
      running(app) {
        when(mockDataStoreConnector.getEmail(any)(any))
          .thenReturn(Future.successful(Left(UndeliverableEmail("test@test.com"))))

        val response = emailAction.filter(authenticatedRequest).map(a => a.get)
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.routes.EmailController.showUndeliverable().url)
      }
    }
  }

  trait Setup {
    val eori                                              = "GB12345678"
    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    val app: Application = applicationBuilder
      .overrides(
        inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
      )
      .build()

    val emailAction: EmailAction = app.injector.instanceOf[EmailAction]

    val authenticatedRequest: IdentifierRequest[AnyContentAsEmpty.type] =
      IdentifierRequest(FakeRequest("GET", "/"), eori)
  }
}
