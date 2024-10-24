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

package models

import models.metadata.CashStatementFileMetadata
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import utils.SpecBase

import java.time.LocalDate

class SdesFileSpec extends SpecBase {

  "SdesFile" should {

    "correctly derive fileFormat from metadata" in new Setup {
      file.metadata.fileFormat mustBe FileFormat.Csv
      file.fileFormat mustBe FileFormat.Csv
    }

    "correctly compute monthAndYear from metadata" in new Setup {
      file.monthAndYear mustBe LocalDate.of(year, month, day)
    }
  }

  "CashStatementFile" should {

    "format size correctly" in new Setup {
      file.formattedSize mustBe "1KB"
    }

    "format month correctly" in new Setup {
      file.formattedMonth mustBe "May"
    }

    "compare correctly based on fileFormat" in new Setup {
      val otherMetadata: CashStatementFileMetadata = CashStatementFileMetadata(
        periodStartYear = yearStart,
        periodStartMonth = monthStart,
        periodStartDay = dayStart,
        periodEndYear = yearEnd,
        periodEndMonth = monthEnd,
        periodEndDay = dayEnd,
        fileFormat = FileFormat.Pdf,
        fileRole = FileRole.CDSCashAccount,
        statementRequestId = Some("xyz-1234"))

      val otherFile: CashStatementFile = CashStatementFile(
        filename = "other_file.pdf",
        downloadURL = "other_file.pdf",
        size = size2,
        metadata = otherMetadata)(messages)

      file.compare(otherFile) mustBe <(0)
      otherFile.compare(file) mustBe >(0)
    }

    "handle equality correctly" in new Setup {
      val anotherFile: CashStatementFile = file.copy()

      file mustBe anotherFile
    }
  }

  trait Setup {

    val app: Application = application.build()
    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

    val yearStart = 2024
    val monthStart = 5
    val dayStart = 5
    val yearEnd = 2025
    val monthEnd = 8
    val dayEnd = 8

    val size = 111L
    val size2 = 200L

    val year = 2024
    val month = 5
    val day = 1

    val metadata: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = yearStart,
      periodStartMonth = monthStart,
      periodStartDay = dayStart,
      periodEndYear = yearEnd,
      periodEndMonth = monthEnd,
      periodEndDay = dayEnd,
      fileFormat = FileFormat.Csv,
      fileRole = FileRole.CDSCashAccount,
      statementRequestId = Some("abc-defg-1234-abc"))

    val file: CashStatementFile = CashStatementFile(
      filename = "name_04",
      downloadURL = "name_04",
      size = size,
      metadata = metadata)(messages)
  }
}
