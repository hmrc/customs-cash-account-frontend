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

package forms

import forms.FormTestHelper._
import models.CashTransactionDates
import utils.SpecBase

import java.time.{Clock, LocalDate, ZoneOffset}

class CashTransactionsRequestPageFormProviderSpec extends SpecBase {

  "apply" must {

    "populate CashTransactionDates form correctly (or with correct error) for input start date" when {

      "start date is valid" in new SetUp {
        form.bind(completeValidDates).get mustBe CashTransactionDates(start = validDate, end = validDate)
      }

      "the day of start date is blank " in new SetUp {
        val startDate = populateFormValueMap("start", "","10","2021")
        val validEndDate = populateFormValueMap("end", "10","10","2021")
        val formData = startDate ++ validEndDate

        val expectedErrors = error("start.day", "cf.form.error.start.date-number-invalid")

         checkForError(form, formData, expectedErrors)
      }

      "the month of start date is blank " in new SetUp {
        val startDate = populateFormValueMap("start", "10", "", "2021")
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")
        val formData = startDate ++ validEndDate

        val expectedErrors = error("start.month", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the year of start date is blank " in new SetUp {
        val startDate = populateFormValueMap("start", "10", "10", "")
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")
        val formData = startDate ++ validEndDate

        val expectedErrors = error("start.year", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the day value is invalid for the start date" in new SetUp {
        val startDate = populateFormValueMap("start", "40", "10", "2021")
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")
        val formData = startDate ++ validEndDate

        val expectedErrors = error("start.day", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the month value is invalid for the start date" in new SetUp {
        val startDate = populateFormValueMap("start", "10", "14", "2021")
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")
        val formData = startDate ++ validEndDate

        val expectedErrors = error("start.month", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the year value is invalid for the start date" in new SetUp {
        val startDate = populateFormValueMap("start", "10", "10", "-")
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")
        val formData = startDate ++ validEndDate

        val expectedErrors = error("start.year", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "start date is in future" in new SetUp {
        val startDate = populateFormValueMap("start", "10", "10", futureYear.toString)
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")
        val formData = startDate ++ validEndDate

        val expectedErrors = error("start", "cf.form.error.start-future-date")

        checkForError(form, formData, expectedErrors)
      }

      "start date has invalid length of the year" in new SetUp {
        val startDate = populateFormValueMap("start", "10", "10", "20211")
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors = error("start", "cf.form.error.year.length")

        checkForError(form, formData, expectedErrors)
      }

      "start date is not within ETMP statement date period" in new SetUp {
        val startDate = populateFormValueMap("start", "10", "10", (etmpStatementYear - 1).toString)
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors = error("start", "cf.form.error.startDate.date-earlier-than-system-start-date")

        checkForError(form, formData, expectedErrors)
      }

      /**
       * Below has been ignored as of now cause below condition is not occurring because
       * ETMP Statement check is overriding this . Need to be checked with Business Team
       */

      "start date is not of a valid tax year" ignore new SetUp {
        val startDate = populateFormValueMap("start", "10", "10", taxYearDateOlderThan6Years.toString)
        val validEndDate = populateFormValueMap("end", "10", "10", "2021")

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors = error("start", "cf.form.error.start.date-too-far-in-past")

        checkForError(form, formData, expectedErrors)
      }
    }

    "populate CashTransactionDates form correctly (or with correct error) for input end date" when {

      "end date is valid" in new SetUp {
        form.bind(completeValidDates).get mustBe CashTransactionDates(start = validDate, end = validDate)
      }

      "the day of end date is blank " in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "", "10", "2021")
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end.day", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the month of end date is blank " in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "10", "", "2021")
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end.month", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the year of end date is blank " in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "10", "10", "")
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end.year", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the day value is invalid for the end date" in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "32", "10", "2021")
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end.day", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the month value is invalid for the end date" in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "10", "14", "2021")
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end.month", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the year value is invalid for the end date" in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "10", "14", "-")
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end.year", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "end date is in future" in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "10", "10", futureYear.toString)
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end", "cf.form.error.end-future-date")

        checkForError(form, formData, expectedErrors)
      }

      "end date has invalid length of the year" in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "10", "10", "20112")
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end", "cf.form.error.year.length")

        checkForError(form, formData, expectedErrors)
      }

      "end date is not within ETMP statement date period" in new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "10", "10", (etmpStatementYear - 1).toString)
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end", "cf.form.error.endDate.date-earlier-than-system-start-date")

        checkForError(form, formData, expectedErrors)
      }

      /**
       * Below has been ignored as of now cause below condition is not occurring because
       * ETMP Statement check is overriding this . Need to be checked with Business Team
       */

      "end date is not of a valid tax year" ignore new SetUp {
        val validStartDate = populateFormValueMap("start", "10", "10", "2021")
        val endDate = populateFormValueMap("end", "10", "10", taxYearDateOlderThan6Years.toString)
        val formData = validStartDate ++ endDate

        val expectedErrors = error("end", "cf.form.error.end.date-too-far-in-past")

        checkForError(form, formData, expectedErrors)
      }
    }
  }

  trait SetUp {
    implicit val clock = Clock.system(ZoneOffset.UTC)
    val form = new CashTransactionsRequestPageFormProvider().apply()

    val validDate: LocalDate = LocalDate.of(2021, 10,10)
    val futureYear = LocalDate.now().getYear + 1
    val etmpStatementYear = 2019 // This needs to be updated should the Year value in Constraints.etmpStatementsDate is changed
    val taxYearDateOlderThan6Years = LocalDate.now().getYear  - 7

    lazy val completeValidDates: Map[String, String] =
      populateFormValueMap("start", "10", "10", "2021") ++
        populateFormValueMap("end", "10", "10", "2021")

    def populateFormValueMap(key: String,
                             day: String,
                             month: String,
                             year: String): Map[String, String] =
      Map(s"$key.day" -> day, s"$key.month" -> month, s"$key.year" -> year)
  }
}


