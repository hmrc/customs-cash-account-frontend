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

package connectors

import models._
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, UpstreamErrorResponse}
import utils.SpecBase

import scala.concurrent.Future

class CustomsDataStoreConnectorSpec extends SpecBase {
    "getEmail" should {
        "return an email address when the request is successful and undeliverable is not present in the response" in new Setup {
            val emailResponse: EmailResponse = EmailResponse(Some("some@email.com"), None, None)

            when[Future[EmailResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
                .thenReturn(Future.successful(emailResponse))

            running(app) {
                val result = await(connector.getEmail("someEori"))
                result mustBe Right(Email("some@email.com"))
            }
        }
    }

    trait Setup {
        val mockHttpClient: HttpClient = mock[HttpClient]
        implicit val hc: HeaderCarrier = HeaderCarrier()

        val app: Application = application.overrides(
            inject.bind[HttpClient].toInstance(mockHttpClient)
        ).build()

        val connector: CustomsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]

    }
}




