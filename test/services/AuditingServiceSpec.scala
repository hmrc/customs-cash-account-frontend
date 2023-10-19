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
import models.AuditModel
import org.mockito.captor.ArgCaptor
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector._
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.SpecBase
import java.time.LocalDateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditingServiceSpec extends SpecBase {

  "AuditingService" should {

    "create the correct data event for recording a successful audit event" in new Setup {
      val model = AuditModel("auditType", "transactionName", json.Json.toJson("the details"))
      await(auditingService.audit(model))

      val dataEventCaptor = ArgCaptor[ExtendedDataEvent]
      verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)
      val dataEvent = dataEventCaptor.value

      dataEvent.auditSource should be(expectedAuditSource)
      dataEvent.auditType should be("auditType")
      dataEvent.detail.toString() should include("the details")
      dataEvent.tags.toString() should include("transactionName")
    }

    "create the correct data event for recording a successful CSV download" in new Setup {
      await(auditingService.auditCsvDownload("eori1", "can1", now, today, tomorrow))

      val dataEventCaptor = ArgCaptor[ExtendedDataEvent]
      verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)
      val dataEvent = dataEventCaptor.value

      dataEvent.auditSource should be(expectedAuditSource)
      dataEvent.auditType should be("DownloadCashStatement")
      dataEvent.detail.toString() should include("eori1")
      dataEvent.detail.toString() should include("can1")
      dataEvent.detail.toString() should include(today.toString)
      dataEvent.tags("transactionName") should be("Download cash transactions")
    }

    "create the data event for recording an failed audit result" in new Setup {
      when(mockAuditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(AuditResult.Failure("An audit failure occurred")))

      val model = AuditModel("auditType", "transactionName", json.Json.toJson("the details"))
      await(auditingService.audit(model))

      val dataEventCaptor = ArgCaptor[ExtendedDataEvent]
      verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)
      val dataEvent = dataEventCaptor.value

      dataEvent.auditSource should be(expectedAuditSource)
      dataEvent.auditType should be("auditType")
      dataEvent.detail.toString() should include("the details")
      dataEvent.tags.toString() should include("transactionName")
    }

    "throw an exception when the send fails to connect" in new Setup {
      when(mockAuditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.failed(new Exception("Failed connection")))
      val model = AuditModel("auditType", "transactionName", json.Json.toJson("the details"))
      intercept[Exception] {
        await(auditingService.audit(model))
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val tomorrow = today.plusDays(1)

    val expectedAuditSource = "customs-cash-account-frontend"

    val mockConfig = mock[AppConfig]
    when(mockConfig.appName).thenReturn("customs-cash-account-frontend")

    val mockAuditConnector = mock[AuditConnector]
    when(mockAuditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

    val auditingService = new AuditingService(mockConfig, mockAuditConnector)
  }
}
