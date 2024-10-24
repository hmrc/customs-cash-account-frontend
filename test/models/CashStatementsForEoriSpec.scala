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

class CashStatementsForEoriSpec extends SpecBase {

  "CashStatementsForEori" should {

    "compare correctly when both have validFrom dates" in new Setup {
      val eoriHistory1: EoriHistory = createEoriHistory("someEori", year, month, day, year + 1, month, day)
      val eoriHistory2: EoriHistory = createEoriHistory("someEori", year + 1, month, day, year + 2, month, day)

      val cashStatementsForEori1: CashStatementsForEori = createCashStatementsForEori(eoriHistory1)
      val cashStatementsForEori2: CashStatementsForEori = createCashStatementsForEori(eoriHistory2)

      cashStatementsForEori1.compare(cashStatementsForEori2) mustBe 1
      cashStatementsForEori2.compare(cashStatementsForEori1) mustBe -1
      cashStatementsForEori1.compare(cashStatementsForEori1) mustBe 0
    }

    "consider instance with validFrom as greater than without validFrom" in new Setup {
      val eoriHistoryWithDate: EoriHistory = createEoriHistory("someEori", year, month, day, year + 1, month, day)
      val eoriHistoryWithoutDate: EoriHistory = createEoriHistoryWithoutDates("someEori")

      val cashStatementsWithDate: CashStatementsForEori = createCashStatementsForEori(eoriHistoryWithDate)
      val cashStatementsWithoutDate: CashStatementsForEori = createCashStatementsForEori(eoriHistoryWithoutDate)

      cashStatementsWithDate.compare(cashStatementsWithoutDate) mustBe 1
      cashStatementsWithoutDate.compare(cashStatementsWithDate) mustBe 1
    }

    "consider two instances without validFrom as equal" in new Setup {
      val eoriHistory1: EoriHistory = createEoriHistoryWithoutDates("someEori")
      val eoriHistory2: EoriHistory = createEoriHistoryWithoutDates("someEori")

      val cashStatements1: CashStatementsForEori = createCashStatementsForEori(eoriHistory1)
      val cashStatements2: CashStatementsForEori = createCashStatementsForEori(eoriHistory2)

      cashStatements1.compare(cashStatements2) mustBe 1
    }

    "handle equality correctly" in new Setup {
      val eoriHistory: EoriHistory = createEoriHistory("someEori", year, month, day, year + 1, month, day)

      val cashStatementsForEori1: CashStatementsForEori =
        CashStatementsForEori(eoriHistory, currentStatements, requestedStatements)

      val cashStatementsForEori2: CashStatementsForEori =
        CashStatementsForEori(eoriHistory, currentStatements, requestedStatements)

      cashStatementsForEori1 mustBe cashStatementsForEori2
    }
  }

  trait Setup {

    val app: Application = application.build()
    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

    val year = 2024
    val month = 5
    val day = 1
    val size = 1024L

    def createEoriHistory(eori: String,
                          fromYear: Int,
                          fromMonth: Int,
                          fromDay: Int,
                          untilYear: Int,
                          untilMonth: Int,
                          untilDay: Int): EoriHistory =
      EoriHistory(
        eori = eori,
        validFrom = Some(LocalDate.of(fromYear, fromMonth, fromDay)),
        validUntil = Some(LocalDate.of(untilYear, untilMonth, untilDay)))

    def createEoriHistoryWithoutDates(eori: String): EoriHistory =
      EoriHistory(eori = eori, validFrom = None, validUntil = None)

    def createCashStatementsForEori(eoriHistory: EoriHistory): CashStatementsForEori =
      CashStatementsForEori(eoriHistory, Seq.empty, Seq.empty)

    val pdfMetadata: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = year,
      periodStartMonth = month,
      periodStartDay = day,
      periodEndYear = year + 1,
      periodEndMonth = month,
      periodEndDay = day,
      fileFormat = FileFormat.Pdf,
      fileRole = FileRole.CDSCashAccount,
      statementRequestId = Some("pdf-1234"))

    val csvMetadata: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = year,
      periodStartMonth = month,
      periodStartDay = day,
      periodEndYear = year + 1,
      periodEndMonth = month,
      periodEndDay = day,
      fileFormat = FileFormat.Csv,
      fileRole = FileRole.CDSCashAccount,
      statementRequestId = Some("csv-5678"))

    val pdfFile: CashStatementFile = CashStatementFile(
      filename = "statement_may_2024.pdf",
      downloadURL = "statement_may_2024.pdf",
      size = size,
      metadata = pdfMetadata)

    val csvFile: CashStatementFile = CashStatementFile(
      filename = "statement_may_2024.csv",
      downloadURL = "statement_may_2024.csv",
      size = size + 1024,
      metadata = csvMetadata)

    val files: Seq[CashStatementFile] = Seq(pdfFile, csvFile)

    val cashStatementsByMonth: CashStatementsByMonth = CashStatementsByMonth(LocalDate.of(year, month, day), files)

    val currentStatements: Seq[CashStatementsByMonth] = Seq(cashStatementsByMonth)
    val requestedStatements: Seq[CashStatementsByMonth] = Seq(cashStatementsByMonth)
  }
}
