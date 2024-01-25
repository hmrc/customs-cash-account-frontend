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
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import utils.SpecBase
import viewmodels.ResultsPageSummary

import java.time.LocalDate

class ResultsPageSummarySpec extends SpecBase {

  "LocalDateFormatter" should {
    "Date should format correctly" in new Setup {
      when(mockResultsPageSummary.formatDate(targetDate)(messages)).thenReturn("10 Feb 2022")
      running(app) {
        val result = connector.formatDate(targetDate)(messages)
        result mustBe "10 Feb 2022"
      }
    }

    "return the month when dateAsMonth is called" in new Setup {
      when(mockResultsPageSummary.dateAsMonth(targetDate)(messages)).thenReturn("March")
      running(app) {
        val result = connector.dateAsMonth(targetDate)(messages)
        result mustBe "March"
      }
    }

    "return the day of the month with leading 0 if less than 10" in new Setup {
      when(mockResultsPageSummary.dateAsDay(targetDate)).thenReturn("09")
      running(app) {
        val result = connector.dateAsDay(targetDate)
        result mustBe "09"
      }
    }

    "return the day of the month with leading 0 if getDayOfMonth is less than 10" in new Setup {
      override val day = 9
      val nineDate: LocalDate = LocalDate.of(year, month, day)
//      val nineDate = LocalDate.of(2022, 3,9)
      when(mockResultsPageSummary.dateAsDay(nineDate)).thenReturn("09")

      running(app) {
        val result = connector.dateAsDay(nineDate)
        result mustBe "09"
      }
    }

    "return the day of the month without leading 0 if equal to 10" in new Setup {
      when(mockResultsPageSummary.dateAsDay(targetDate)).thenReturn("10")
      running(app) {
        val result = connector.dateAsDay(targetDate)
        result mustBe "10"
      }
    }

    "return the day of the month without leading 0 if greater than 10" in new Setup {
      when(mockResultsPageSummary.dateAsDay(targetDate)).thenReturn("11")
      running(app) {
        val result = connector.dateAsDay(targetDate)
        result mustBe "11"
      }
    }
  }

  trait Setup {
    val day = 11
    val month = 3
    val year = 2022
    val targetDate: LocalDate = LocalDate.of(year, month, day)
    val mockResultsPageSummary: ResultsPageSummary = mock[ResultsPageSummary]

    val app: Application = application
      .overrides(
        bind[ResultsPageSummary].toInstance(mockResultsPageSummary)
      )
      .configure()
      .build()

    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
    val connector: ResultsPageSummary = app.injector.instanceOf[ResultsPageSummary]
  }
}
