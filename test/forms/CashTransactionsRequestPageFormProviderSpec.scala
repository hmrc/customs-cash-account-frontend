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
import play.api.data.{Form, FormError}
import utils.SpecBase

import java.time.{Clock, LocalDate, ZoneOffset}

class CashTransactionsRequestPageFormProviderSpec extends SpecBase {

  "apply" must {

    "populate CashTransactionDates form correctly (or with correct error) for input start date" when {

      "start date is valid" in new SetUp {
        form.bind(completeValidDates).get mustBe CashTransactionDates(start = validDate, end = validDate)
      }

      "the day of start date is blank " in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", emptyString, month10AsString, year2021AsString)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.day", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the month of start date is blank " in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, emptyString, year2021AsString)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.month", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the year of start date is blank " in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, emptyString)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.year", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the day value is invalid for the start date" in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", month40AsString, month10AsString, year2021AsString)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.day", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the month value is invalid for the start date" in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month14AsString, year2021AsString)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.month", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the year value is invalid for the start date" in new SetUp {
        val invalidYearValue = "-"

        val startDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, invalidYearValue)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.year", "cf.form.error.start.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "start date is in future" in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, futureYear.toString)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start", "cf.form.error.start-future-date")

        checkForError(form, formData, expectedErrors)
      }

      "start date has invalid length of the year" in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, "20211")

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start", "cf.form.error.year.length")

        checkForError(form, formData, expectedErrors)
      }

      "start date is not within ETMP statement date period" in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, (etmpStatementYear - 1).toString)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] =
          error("start", "cf.form.error.startDate.date-earlier-than-system-start-date")

        checkForError(form, formData, expectedErrors)
      }

      /**
       * Below has been ignored as of now cause below condition is not occurring because
       * ETMP Statement check is overriding this . Need to be checked with Business Team
       */

      "start date is not of a valid tax year" ignore new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, taxYearDateOlderThan6Years.toString)

        val validEndDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start", "cf.form.error.start.date-too-far-in-past")

        checkForError(form, formData, expectedErrors)
      }
    }

    "populate CashTransactionDates form correctly (or with correct error) for input end date" when {

      "end date is valid" in new SetUp {
        form.bind(completeValidDates).get mustBe CashTransactionDates(start = validDate, end = validDate)
      }

      "the day of end date is blank " in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", emptyString, month10AsString, year2021AsString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.day", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the month of end date is blank " in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, emptyString, year2021AsString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.month", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the year of end date is blank " in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, emptyString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.year", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the day value is invalid for the end date" in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", month32AsString, month10AsString, year2021AsString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.day", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the month value is invalid for the end date" in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month14AsString, year2021AsString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.month", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "the year value is invalid for the end date" in new SetUp {
        val invalidYearValue = "-"

        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month14AsString, invalidYearValue)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.year", "cf.form.error.end.date-number-invalid")

        checkForError(form, formData, expectedErrors)
      }

      "end date is in future" in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, futureYear.toString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end", "cf.form.error.end-future-date")

        checkForError(form, formData, expectedErrors)
      }

      "end date has invalid length of the year" in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] = populateFormValueMap("end", day10AsString, month10AsString, "20112")
        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end", "cf.form.error.year.length")

        checkForError(form, formData, expectedErrors)
      }

      "end date is not within ETMP statement date period" in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, (etmpStatementYear - 1).toString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end", "cf.form.error.endDate.date-earlier-than-system-start-date")

        checkForError(form, formData, expectedErrors)
      }

      /**
       * Below has been ignored as of now cause below condition is not occurring because
       * ETMP Statement check is overriding this . Need to be checked with Business Team
       */

      "end date is not of a valid tax year" ignore new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap("start", day10AsString, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap("end", day10AsString, month10AsString, taxYearDateOlderThan6Years.toString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end", "cf.form.error.end.date-too-far-in-past")

        checkForError(form, formData, expectedErrors)
      }
    }
  }

  trait SetUp {
    implicit val clock: Clock = Clock.system(ZoneOffset.UTC)

    val form: Form[CashTransactionDates] = new CashTransactionsRequestPageFormProvider().apply()

    val year = 2021
    val month = 10
    val day = 10

    val validDate: LocalDate = LocalDate.of(year, month, day)
    val futureYear: Int = LocalDate.now().getYear + 1
    val etmpStatementYear = 2019
    val taxYearDateOlderThan6Years: Int = LocalDate.now().getYear - 7

    val year2021AsString = "2021"
    val month10AsString = "10"
    val month32AsString = "32"
    val month40AsString = "40"
    val month14AsString = "14"
    val day10AsString = "10"

    lazy val completeValidDates: Map[String, String] =
      populateFormValueMap("start", day10AsString, month10AsString, year2021AsString) ++
        populateFormValueMap("end", day10AsString, month10AsString, year2021AsString)

    def populateFormValueMap(key: String,
                             day: String,
                             month: String,
                             year: String): Map[String, String] =
      Map(s"$key.day" -> day, s"$key.month" -> month, s"$key.year" -> year)
  }
}
