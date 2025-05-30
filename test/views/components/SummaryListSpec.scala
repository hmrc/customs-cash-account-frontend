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

package views.components

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import utils.SpecBase
import viewmodels.ResultsPageSummary
import views.html.components.summaryList

import java.time.LocalDate

class SummaryListSpec extends SpecBase {

  "component" should {

    "display correct contents" when {

      "change is false" in new Setup {
        summaryListComponent.getElementById(downloadId).text() mustBe download
        summaryListComponent.getElementsByClass(classes).size() mustBe 1
        Option(summaryListComponent.getElementById(changeId)) mustBe None
      }

      "change is true without fullstop" in new Setup {
        summaryListComponentWithChange.getElementById(changeId).text() mustBe change
        summaryListComponentWithChange.getElementsByClass(classes).size() mustBe 1
        Option(summaryListComponentWithChange.getElementById(downloadId)) mustBe None
      }
    }
  }

  trait Setup {
    val day10th = 10
    val day11th = 11
    val month   = 3
    val year    = 2022

    val download: String   = "Download CSV"
    val change: String     = "Change"
    val classes: String    = "govuk-summary-list__actions"
    val downloadId: String = "downloadChange"
    val changeId: String   = "changeDates"

    val fromDate: LocalDate = LocalDate.of(year, month, day10th)
    val toDate: LocalDate   = LocalDate.of(year, month, day11th)

    val summary: ResultsPageSummary = new ResultsPageSummary(fromDate, toDate)

    val summaryListComponent: Document =
      Jsoup.parse(application.injector.instanceOf[summaryList].apply(summary = summary).body)

    val summaryListComponentWithChange: Document =
      Jsoup.parse(application.injector.instanceOf[summaryList].apply(summary = summary, isChange = true).body)
  }
}
