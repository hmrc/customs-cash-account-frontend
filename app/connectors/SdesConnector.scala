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
import models.*
import models.FileFormat.{SdesFileFormats, filterFileFormats}
import models.FileRole.CDSCashAccount
import play.api.i18n.Messages
import play.api.libs.json.Reads
import services.{AuditingService, MetricsReporterService, SdesGatekeeperService}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdesConnector @Inject()(httpClientV2: HttpClientV2,
                              appConfig: AppConfig,
                              metricsReporterService: MetricsReporterService,
                              sdesGatekeeperService: SdesGatekeeperService,
                              auditingService: AuditingService
                             )(implicit executionContext: ExecutionContext) {

  import sdesGatekeeperService._

  def getCashStatements(eori: String)(implicit hc: HeaderCarrier, messages: Messages): Future[Seq[CashStatementFile]] = {

    val transform = convertTo[CashStatementFile] andThen filterFileFormats(SdesFileFormats)
    auditingService.auditCashStatements(eori)

    getSdesFiles[FileInformation, CashStatementFile](
      url = appConfig.sdesApiEndPoint,
      key = eori,
      metricsName = "sdes.get.cash-statements",
      transform = transform
    )
  }

  private def addXHeaders(hc: HeaderCarrier, key: String): HeaderCarrier =
    hc.copy(extraHeaders = hc.extraHeaders ++ Seq("x-client-id" -> appConfig.xClientIdHeader, "X-SDES-Key" -> key))

  private def getSdesFiles[A, B <: SdesFile](url: String,
                                             key: String,
                                             metricsName: String,
                                             transform: Seq[A] => Seq[B]
                                            )(implicit reads: Reads[Seq[A]], hc: HeaderCarrier): Future[Seq[B]] = {
    metricsReporterService.withResponseTimeLogging(metricsName) {
      httpClientV2
        .get(url"$url")
        .setHeader(addXHeaders(hc, key).extraHeaders: _*)
        .execute[Seq[A]]
        .map(transform)
    }
  }
}
