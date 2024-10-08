/*
 * Copyright 2024 HM Revenue & Customs
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

import models.*
import models.FileRole.CashStatement
import models.metadata.{Metadata, MetadataItem}
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase

class SdesGatekeeperServiceSpec extends SpecBase {

  "SdesGatekeeperService" should {

    "convert FileInformation to CashStatementFile correctly" in new Setup {
      val sdesFileInformation: FileInformation = validCashStatementFileInformation

      val result: CashStatementFile = sdesGatekeeperService.convertToCashStatementFile(sdesFileInformation)(msg)

      result.filename mustBe sdesFileInformation.filename
      result.downloadURL mustBe sdesFileInformation.downloadURL
      result.metadata.periodStartYear mustBe periodStartYear
      result.metadata.periodStartMonth mustBe periodStartMonth
      result.metadata.periodStartDay mustBe periodStartDay
      result.metadata.periodEndYear mustBe periodEndYear
      result.metadata.periodEndMonth mustBe periodEndMonth
      result.metadata.periodEndDay mustBe periodEndDay
      result.metadata.fileFormat mustBe FileFormat(csv)
      result.metadata.fileRole mustBe CashStatement
      result.metadata.statementRequestId mustBe someRequestId
    }
  }

  trait Setup {

    val app: Application = application.build()
    implicit val msg: Messages = messages(app)

    val sdesGatekeeperService = new SdesGatekeeperService()
    val periodStartYear = 2017
    val periodStartMonth = 11
    val periodStartDay = 1
    val periodEndYear = 2017
    val periodEndMonth = 11
    val periodEndDay = 8
    val fileSize = 500L
    val someAccountNumber: Option[String] = Some("123456789")
    val someRequestId: Option[String] = Some("Ab1234")
    val csv = "csv"
    val fileName = "test-file.csv"
    val downloadURL = "test-file.csv"

    val validCashStatementFileInformation: FileInformation = FileInformation(
      filename = fileName,
      downloadURL = downloadURL,
      fileSize = fileSize,
      metadata = Metadata(Seq(
        MetadataItem("PeriodStartYear", periodStartYear.toString),
        MetadataItem("PeriodStartMonth", periodStartMonth.toString),
        MetadataItem("PeriodStartDay", periodStartDay.toString),
        MetadataItem("PeriodEndYear", periodEndYear.toString),
        MetadataItem("PeriodEndMonth", periodEndMonth.toString),
        MetadataItem("PeriodEndDay", periodEndDay.toString),
        MetadataItem("FileType", csv),
        MetadataItem("FileRole", "CashStatement"),
        MetadataItem("CashAccountNumber", someAccountNumber.getOrElse(emptyString)),
        MetadataItem("statementRequestID", someRequestId.getOrElse(emptyString))
      ))
    )
  }
}
