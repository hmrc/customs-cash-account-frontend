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

package services

import config.AppConfig
import models.email.{UndeliverableEmail, UnverifiedEmail}
import play.api.Application
import play.api.inject.bind
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HttpClient
import utils.SpecBase

class DataStoreServiceSpec extends SpecBase {

  "getEmail" should {

    "return correct email address when verified email is found" in new Setup {
      service.getEmail(eori) mustBe Right(Email(emailId))
    }

    "return Left(UndeliverableEmail) when undeliverable email is returned from data store" in new Setup {
      service.getEmail(eori) mustBe Left(UndeliverableEmail(emailId))
    }

    "return Left(UnverifiedEmail) when unverified email is returned from data store" in new Setup {
      service.getEmail(eori) mustBe Left(UnverifiedEmail)
    }

    "return Left(UnverifiedEmail) if any error occurs while calling the data store API " in new Setup {
      service.getEmail(eori) mustBe Left(UnverifiedEmail)
    }
  }

  trait Setup {
    val eori = "EORINOTIMESTAMP"
    val emailId = "test@test.com"

    val mockHttpClient: HttpClient = mock[HttpClient]
    val mockMetricsReporter: MetricsReporterService = mock[MetricsReporterService]

    implicit val mockConfig: AppConfig = mock[AppConfig]

    val app: Application = application.overrides(
      bind[HttpClient].toInstance(mockHttpClient),
      bind[MetricsReporterService].toInstance(mockMetricsReporter),
      bind[AppConfig].toInstance(mockConfig)
    ).build()

    val service: DataStoreService = app.injector.instanceOf[DataStoreService]
  }
}
