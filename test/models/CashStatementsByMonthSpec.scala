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
import java.time.LocalDate
import utils.SpecBase

class CashStatementsByMonthSpec extends SpecBase {

  "CashStatementsByMonth" should {

    "format month correctly" in new Setup {
      cashStatementsByMonth.formattedMonth mustBe "May"
    }

    "format month and year correctly" in new Setup {
      cashStatementsByMonth.formattedMonthYear mustBe "1 May 2024"
    }

    "find the PDF file correctly" in new Setup {
      cashStatementsByMonth.pdf mustBe Some(pdfFile)
    }

    "find the CSV file correctly" in new Setup {
      cashStatementsByMonth.csv mustBe Some(csvFile)
    }

    "compare correctly based on date" in new Setup {
      val earlierDate: CashStatementsByMonth =
        CashStatementsByMonth(LocalDate.of(year, month - 1, day), files = files)(messages)

      cashStatementsByMonth.compare(earlierDate) must be > 0
      earlierDate.compare(cashStatementsByMonth) must be < 0
      cashStatementsByMonth.compare(cashStatementsByMonth) mustBe 0
    }


    "handle equality correctly" in new Setup {
      val anotherCashStatementsByMonth: CashStatementsByMonth = cashStatementsByMonth.copy()
      cashStatementsByMonth mustBe anotherCashStatementsByMonth
    }
  }

  trait Setup {

    val year = 2024
    val month = 5
    val day = 1

    val yearStart = 2024
    val monthStart = 5
    val dayStart = 1
    val yearEnd = 2025
    val monthEnd = 5
    val dayEnd = 31

    val size = 1024L
    val size2 = 2048L

    val date: LocalDate = LocalDate.of(year, month, day)

    val pdfMetadata: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = yearStart,
      periodStartMonth = monthStart,
      periodStartDay = dayStart,
      periodEndYear = yearEnd,
      periodEndMonth = monthEnd,
      periodEndDay = dayEnd,
      fileFormat = FileFormat.Pdf,
      fileRole = FileRole.CDSCashAccount,
      statementRequestId = Some("pdf-1234"))

    val csvMetadata: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = yearStart,
      periodStartMonth = monthStart,
      periodStartDay = dayStart,
      periodEndYear = yearEnd,
      periodEndMonth = monthEnd,
      periodEndDay = dayEnd,
      fileFormat = FileFormat.Csv,
      fileRole = FileRole.CDSCashAccount,
      statementRequestId = Some("csv-5678"))

    val pdfFile: CashStatementFile = CashStatementFile(
      filename = "statement_may_2024.pdf",
      downloadURL = "statement_may_2024.pdf",
      size = size,
      metadata = pdfMetadata)(messages)

    val csvFile: CashStatementFile = CashStatementFile(
      filename = "statement_may_2024.csv",
      downloadURL = "statement_may_2024.csv",
      size = size2,
      metadata = csvMetadata)(messages)

    val files: Seq[CashStatementFile] = Seq(pdfFile, csvFile)

    val cashStatementsByMonth: CashStatementsByMonth = CashStatementsByMonth(date, files)
  }
}
