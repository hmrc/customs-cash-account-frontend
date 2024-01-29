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
import models.domain.EORI
import models.email.{EmailResponse, EmailResponses, UndeliverableEmail, UnverifiedEmail}
import play.api.Logger
import play.api.http.Status.NOT_FOUND
import services.MetricsReporterService
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDataStoreConnector @Inject()(http: HttpClient,
                                          metricsReporter: MetricsReporterService)
                                         (implicit appConfig: AppConfig, ec: ExecutionContext) {

  private val log = Logger(this.getClass)

  def getEmail(eori: EORI)(implicit hc: HeaderCarrier): Future[Either[EmailResponses, Email]] = {
    val dataStoreEndpoint = s"${appConfig.customsDataStore}/eori/$eori/verified-email"
    val resourceNameForMetrics = "customs-data-store.get.email"

    metricsReporter.withResponseTimeLogging(resourceNameForMetrics) {
      http.GET[EmailResponse](dataStoreEndpoint).map {
        case EmailResponse(Some(address), _, None) => Right(Email(address))
        case EmailResponse(Some(email), _, Some(_)) => Left(UndeliverableEmail(email))
        case _ => Left(UnverifiedEmail)
      }.recover {
        case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
          Left(UnverifiedEmail)
        case exception =>
          log.error(s"verified-email API call is Failing and error is ::: ${exception.getMessage}")
          throw exception
      }
    }
  }

}
