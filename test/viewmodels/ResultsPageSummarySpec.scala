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

package viewmodels

import org.scalatest.Assertion
import utils.SpecBase

import java.time.LocalDate

class ResultsPageSummarySpec extends SpecBase {

  "ResultsPageSummary with day flag as true" should {

    "return correct SummaryListRow object from rows" when {

      "fullStop is false" in new Setup {

        val summaryList: SummaryListRow = resultPageSummary01.rows(false)

        shouldProduceCorrectDatesRangeText(summaryList, "08 March 2022 to 10 April 2022")
        shouldContainCorrectLinkForAction(summaryList, "/download-requested-csv")
        shouldNotHaveClasses(summaryList)
        shouldNotHaveSecondValue(summaryList)
      }

      "fullStop is true" in new Setup {

        val summaryList: SummaryListRow = resultPageSummary01.rows()

        shouldProduceCorrectDatesRangeText(summaryList, "08 March 2022 to 10 April 2022.")
        shouldContainCorrectLinkForAction(summaryList, "/download-requested-csv")
        shouldNotHaveClasses(summaryList)
        shouldNotHaveSecondValue(summaryList)
      }
    }
  }

  "ResultsPageSummary with day flag as false" should {

    "return correct SummaryListRow object from rowsV2" when {

      "fullStop is false" in new Setup {

        val summaryList: SummaryListRow = resultPageSummary02.rowsV2(false)

        shouldProduceCorrectDatesRangeText(summaryList, "March 2022 to April 2022")
        shouldContainCorrectLinkForAction(summaryList, "request-cash-transactions/v2")
        shouldNotHaveClasses(summaryList)
        shouldNotHaveSecondValue(summaryList)
      }

      "fullStop is true" in new Setup {

        val summaryList: SummaryListRow = resultPageSummary02.rowsV2()

        shouldProduceCorrectDatesRangeText(summaryList, "March 2022 to April 2022.")
        shouldContainCorrectLinkForAction(summaryList, "request-cash-transactions/v2")
        shouldNotHaveClasses(summaryList)
        shouldNotHaveSecondValue(summaryList)
      }
    }
  }

  "LocalDateFormatter" should {

    "Date should format correctly with day" in new Setup {
      val result: String = resultPageSummary01.formatDate(toDate, true)

      result mustBe "10 April 2022"
    }

    "Date should format correctly without day" in new Setup {
      val result: String = resultPageSummary01.formatDate(toDate, false)

      result mustBe "April 2022"
    }

    "return the month when dateAsMonth is called" in new Setup {
      val result: String = resultPageSummary01.dateAsMonth(toDate)

      result mustBe "April"
    }

    "return the day of the month with leading 0 if day of the month is less than 10" in new Setup {
      val dateWithDay9th: LocalDate = LocalDate.of(year, month3rd, day9th)
      val result: String            = resultPageSummary01.dateAsDay(dateWithDay9th)

      result mustBe "09"
    }

    "return the day of the month without leading 0 if equal to 10" in new Setup {
      val dateWithDay10th: LocalDate = LocalDate.of(year, month3rd, day10th)
      val result: String             = resultPageSummary01.dateAsDay(dateWithDay10th)

      result mustBe "10"
    }

    "return the day of the month without leading 0 if greater than 10" in new Setup {
      val dateWithDay11th: LocalDate = LocalDate.of(year, month3rd, day11th)
      val result: String             = resultPageSummary01.dateAsDay(dateWithDay11th)

      result mustBe "11"
    }
  }

  private def shouldProduceCorrectDatesRangeText(summaryList: SummaryListRow, datesRange: String): Assertion =
    summaryList.value.content.asHtml.body mustBe datesRange

  private def shouldContainCorrectLinkForAction(summaryList: SummaryListRow, url: String): Assertion =
    summaryList.actions.head.items.head.href.contains(url) mustBe true

  private def shouldNotHaveClasses(summaryList: SummaryListRow): Assertion =
    summaryList.classes mustBe emptyString

  private def shouldNotHaveSecondValue(summaryList: SummaryListRow): Assertion =
    summaryList.secondValue mustBe None

  trait Setup {
    val day9th  = 9
    val day8th  = 8
    val day10th = 10
    val day11th = 11

    val month3rd = 3
    val month4th = 4
    val year     = 2022

    val fromDate: LocalDate = LocalDate.of(year, month3rd, day8th)
    val toDate: LocalDate   = LocalDate.of(year, month4th, day10th)

    val resultPageSummary01: ResultsPageSummary = new ResultsPageSummary(fromDate, toDate, true)
    val resultPageSummary02: ResultsPageSummary = new ResultsPageSummary(fromDate, toDate, false)
  }
}
