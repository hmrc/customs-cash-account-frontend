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

import config.AppConfig
import models.email.*
import play.api.Application
import play.api.inject.bind
import services.MetricsReporterService
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HttpReads
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import scala.concurrent.ExecutionContext

class DataStoreConnectorSpec extends SpecBase {

  "getEmail" should {

    "return correct email address when verified email is found" in new Setup {
      private val emailResFromAPI = EmailResponse(Some(emailId), None, None)

      when(mockMetricsReporter.withResponseTimeLogging[EmailResponse](any)(any)(any)).thenReturn(
        Future.successful(emailResFromAPI))

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailResFromAPI))
      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      connector.getEmail(eori).map {
        res => res mustBe Right(Email(emailId))
      }
    }

    "return Left(UndeliverableEmail) when undeliverable email is returned from data store" in new Setup {
      val emailResFromAPI: EmailResponse = EmailResponse(Some(emailId), None, Some(undelInfoOb))

      when(mockMetricsReporter.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenReturn(Future.successful(emailResFromAPI))

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailResFromAPI))
      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      connector.getEmail(eori).map {
        res => res mustBe Left(UndeliverableEmail(emailId))
      }
    }

    "return Left(UnverifiedEmail) when unverified email is returned from data store" in new Setup {
      val emailResFromAPI: EmailResponse = EmailResponse(None, None, Some(undelInfoOb))

      when(mockMetricsReporter.withResponseTimeLogging[EmailResponse](any)(any)(any)).thenReturn(
        Future.successful(emailResFromAPI))

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailResFromAPI))
      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      connector.getEmail(eori).map {
        res => res mustBe Left(UnverifiedEmail)
      }
    }

    "return Left(UnverifiedEmail) if any error occurs while calling the data store API " in new Setup {
      private val emailResFromAPI = EmailResponse(Some(emailId), None, None)

      when(mockMetricsReporter.withResponseTimeLogging[EmailResponse](any)(any)(any)).thenReturn(
        Future.successful(emailResFromAPI))

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Error occurred")))
      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      connector.getEmail(eori).map {
        res => res mustBe Left(UnverifiedEmail)
      }
    }
  }

  trait Setup {
    val eori = "EORINOTIMESTAMP"
    val emailId = "test@test.com"
    val value = 12

    val undelInfoEventOb: UndeliverableInformationEvent = UndeliverableInformationEvent("example-id",
      "someEvent",
      "email@email.com",
      "2021-05-14T10:59:45.811+01:00",
      Some(value),
      Some("Inbox full"),
      "HMRC-CUS-ORG~EORINumber~GB744638982004")

    val undelInfoOb: UndeliverableInformation = UndeliverableInformation("someSubject",
      "example-id",
      "example-group-id",
      "2021-05-14T10:59:45.811+01:00",
      undelInfoEventOb)

    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    val mockMetricsReporter: MetricsReporterService = mock[MetricsReporterService]

    val app: Application = application.overrides(
      bind[HttpClientV2].toInstance(mockHttpClient),
      bind[RequestBuilder].toInstance(requestBuilder),
      bind[MetricsReporterService].toInstance(mockMetricsReporter)
    ).build()

    implicit val mockConfig: AppConfig = app.injector.instanceOf[AppConfig]

    val connector: CustomsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]
  }
}
