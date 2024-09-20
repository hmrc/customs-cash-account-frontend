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
import models.FileFormat.Csv
import models.FileRole.CashStatement
import models.metadata.{CashStatementFileMetadata, Metadata, MetadataItem}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import utils.SpecBase

import scala.concurrent.ExecutionContext
import play.api.Application

import scala.concurrent.Future

class SdesConnectorSpec extends SpecBase {

  "SdesConnector" should {
    "return transformed cash statements" in new Setup {

      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[Seq[FileInformation]]], any[ExecutionContext]))
        .thenReturn(Future.successful(cashStatementFilesWithUnknownFileTypesSdesResponse))

      when(mockHttp.get(eqTo(url"$sdesCashStatementUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(sdesConnector.getCashStatements(someEori)(hc, messages))
        result must be(cashStatementFiles)
      }
    }
  }

  trait Setup {

    val mockHttp: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val app: Application = application.overrides(
      bind[HttpClientV2].toInstance(mockHttp),
      bind[RequestBuilder].toInstance(requestBuilder)
    ).build()

    val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val sdesConnector: SdesConnector = app.injector.instanceOf[SdesConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()

    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

    val someDan = "1234"
    val someEori = "eori1"

    val size = 111L
    val size5 = 113L

    val yearStart = 2018
    val monthStart = 3
    val dayStart = 2
    val yearEnd = 2018
    val monthEnd = 3
    val dayEnd = 2

    val sdesCashStatementUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/CashStatement"

    val cashStatementFilesSdesResponse: Seq[FileInformation] = List(
      FileInformation("name_04", "download_url_06", size,
        Metadata(List(
          MetadataItem("PeriodStartYear", "2018"),
          MetadataItem("PeriodStartMonth", "3"),
          MetadataItem("PeriodStartDay", "2"),
          MetadataItem("PeriodEndYear", "2018"),
          MetadataItem("PeriodEndMonth", "3"),
          MetadataItem("PeriodEndDay", "2"),
          MetadataItem("FileType", "Csv"),
          MetadataItem("FileRole", "CashStatement")))))

    val cashStatementFilesWithUnknownFileTypesSdesResponse: Seq[FileInformation] = List(
      FileInformation("name_04", "download_url_06", size,
        Metadata(List(
          MetadataItem("PeriodStartYear", "2018"),
          MetadataItem("PeriodStartMonth", "3"),
          MetadataItem("PeriodStartDay", "1"),
          MetadataItem("PeriodEndYear", "2018"),
          MetadataItem("PeriodEndMonth", "3"),
          MetadataItem("PeriodEndDay", "1"),
          MetadataItem("FileType", "Cho"),
          MetadataItem("FileRole", "CashStatement")
        ))
      )
    ) ++ cashStatementFilesSdesResponse ++ List(
      FileInformation(
        "name_01", "download_url_01", size5,
        Metadata(List(
          MetadataItem("PeriodStartYear", "2018"),
          MetadataItem("PeriodStartMonth", "6"),
          MetadataItem("PeriodStartDay", "3"),
          MetadataItem("PeriodEndYear", "2018"),
          MetadataItem("PeriodEndMonth", "6"),
          MetadataItem("PeriodEndDay", "3"),
          MetadataItem("FileType", "Bar"),
          MetadataItem("FileRole", "CashStatement")))))

    val cashStatementFiles: Seq[CashStatementFile] = List(
      CashStatementFile(
        "name_04", "download_url_06", size,
        CashStatementFileMetadata(
          periodStartYear = yearStart,
          periodStartMonth = monthStart,
          periodStartDay = dayStart,
          periodEndYear = yearEnd,
          periodEndMonth = monthEnd,
          periodEndDay = dayEnd,
          fileFormat = FileFormat.Csv,
          fileRole = CashStatement,
          statementRequestId = None
        ), emptyString))
  }
}
