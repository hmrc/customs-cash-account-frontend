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

import java.time.LocalDate
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import utils.SpecBase
import viewmodels.ResultsPageSummary


class ResultsPageSummarySpec extends SpecBase {

  "LocalDateFormatter" should {
    "Date should format correctly" in new Setup {
      when(mockResultsPageSummary.testData(targetDate)(messages)).thenReturn("10 Feb 20202")
      running(app) {
        val date = LocalDate.of(2022, 2, 10)
        val result = connector.formatDate(date)(messages)
        result mustBe "10 Feb 2022"
      }
    }

    "return the day of the month with leading 0 if less than 10" in new Setup {
      when(mockResultsPageSummary.testData(targetDate)(messages)).thenReturn("09")

      running(app){
        val date = LocalDate.of(2022, 2,9)
        val result = connector.dateAsDay(date)(messages)
        result mustBe "09"
      }
    }

    "return the day of the month without leading 0 if equal to 10" in new Setup {
      when(mockResultsPageSummary.testData(targetDate)(messages)).thenReturn("10")
      running(app){
        val date = LocalDate.of(2022, 2,10)
        val result = connector.dateAsDay(date)(messages)
        result mustBe "10"
      }
    }

    "return the day of the month without leading 0 if greater than 10" in new Setup {
      when(mockResultsPageSummary.testData(targetDate)(messages)).thenReturn("11")
      running(app){
        val date = LocalDate.of(2022, 2,11)
        val result = date.getDayOfMonth
        result mustBe 11
      }
    }

    "testData" in new Setup {
      when(mockResultsPageSummary.testData(targetDate)(messages)).thenReturn("11")
      running(app){
        val date = LocalDate.of(2022, 2,11)
        val result = connector.testData(date)(messages)
        result mustBe "11"
      }
    }
  }

  trait Setup {
    val targetDate = LocalDate.of(2022, 2,11)
    val mockResultsPageSummary: ResultsPageSummary = mock[ResultsPageSummary]

    val app: Application = application
      .overrides(
        bind[ResultsPageSummary].toInstance(mockResultsPageSummary)
      )
      .configure()
      .build()

    implicit val messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
    val connector = app.injector.instanceOf[ResultsPageSummary]
  }
}
