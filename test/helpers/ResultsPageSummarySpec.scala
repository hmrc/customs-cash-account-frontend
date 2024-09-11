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

package helpers

import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import utils.SpecBase
import viewmodels.ResultsPageSummary

import java.time.LocalDate

class ResultsPageSummarySpec extends SpecBase {

  "LocalDateFormatter" should {

    "Date should format correctly with day" in new Setup {
      val result: String = resultPageSummary.formatDate(toDate, true)

      result mustBe "10 March 2022"
    }

    "Date should format correctly without day" in new Setup {
      val result: String = resultPageSummary.formatDate(toDate, false)

      result mustBe "March 2022"
    }

    "return the month when dateAsMonth is called" in new Setup {
      val result: String = resultPageSummary.dateAsMonth(toDate)

      result mustBe "March"
    }

    "return the day of the month with leading 0 if day of the month is less than 10" in new Setup {
      val dateWithDay9th: LocalDate = LocalDate.of(year, month, day9th)
      val result: String = resultPageSummary.dateAsDay(dateWithDay9th)

      result mustBe "09"
    }

    "return the day of the month without leading 0 if equal to 10" in new Setup {
      val dateWithDay10th: LocalDate = LocalDate.of(year, month, day10th)
      val result: String = resultPageSummary.dateAsDay(dateWithDay10th)

      result mustBe "10"
    }

    "return the day of the month without leading 0 if greater than 10" in new Setup {
      val dateWithDay11th: LocalDate = LocalDate.of(year, month, day11th)
      val result: String = resultPageSummary.dateAsDay(dateWithDay11th)

      result mustBe "11"
    }
  }

  trait Setup {
    val day9th = 9
    val day8th = 8
    val day10th = 10
    val day11th = 11

    val month = 3
    val year = 2022

    val fromDate: LocalDate = LocalDate.of(year, month, day8th)
    val toDate: LocalDate = LocalDate.of(year, month, day10th)

    val app: Application = application.build()

    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
    val resultPageSummary: ResultsPageSummary = new ResultsPageSummary(fromDate, toDate)
  }
}
