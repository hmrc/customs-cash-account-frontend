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

import forms.FormTestHelper.*
import models.CashTransactionDates
import play.api.data.{Form, FormError}
import utils.SpecBase

import java.time.{Clock, LocalDate, ZoneOffset}

class SelectTransactionsFormProviderSpec extends SpecBase {

  "apply" must {

    "populate form correctly (or with correct error) for input start date" when {

      "the month value of start date is blank" in new SetUp {
        val startDate: Map[String, String]    = populateFormValueMap(startKey, emptyString, year2021AsString)
        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)
        val formData: Map[String, String]     = startDate ++ validEndDate
        val expectedErrors: Seq[FormError]    = error("start.month", emptyMonthStartDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year value of start date is blank" in new SetUp {
        val startDate: Map[String, String]    = populateFormValueMap(startKey, month10AsString, emptyString)
        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)
        val formData: Map[String, String]     = startDate ++ validEndDate
        val expectedErrors: Seq[FormError]    = error("start.year", emptyYearStartDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the month and year values of start date are empty" in new SetUp {
        val startDate: Map[String, String]    = populateFormValueMap(startKey, emptyString, emptyString)
        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)
        val formData: Map[String, String]     = startDate ++ validEndDate
        val expectedErrors: Seq[FormError]    = error("start", emptyMonthAndYearStartDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the month value is invalid for the start date" in new SetUp {
        val startDate: Map[String, String]    = populateFormValueMap(startKey, month14AsString, year2021AsString)
        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)
        val formData: Map[String, String]     = startDate ++ validEndDate
        val expectedErrors: Seq[FormError]    = error("start.month", invalidStartMonthKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year value is invalid for the start date" in new SetUp {
        val startDate: Map[String, String]    = populateFormValueMap(startKey, month10AsString, invalidYearValue)
        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)
        val formData: Map[String, String]     = startDate ++ validEndDate
        val expectedErrors: Seq[FormError]    = error("start.year", invalidStartYearKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year length is less than 4 digits" in new SetUp {
        val yearWithInvalidLength             = "202"
        val startDate: Map[String, String]    = populateFormValueMap(startKey, month10AsString, yearWithInvalidLength)
        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)
        val formData: Map[String, String]     = startDate ++ validEndDate
        val expectedErrors: Seq[FormError]    = error("start.year", invalidStartYearKey)

        checkForError(form, formData, expectedErrors)
      }

      "start date has invalid length of the year" in new SetUp {
        val yearWithInvalidLength             = "20211"
        val startDate: Map[String, String]    = populateFormValueMap(startKey, month10AsString, yearWithInvalidLength)
        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)
        val formData: Map[String, String]     = startDate ++ validEndDate
        val expectedErrors: Seq[FormError]    = error("start.year", invalidStartYearKey)

        checkForError(form, formData, expectedErrors)
      }

      "start date is not within ETMP statement date period" in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap(startKey, month10AsString, (etmpStatementYear - 1).toString)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)
        val formData: Map[String, String]     = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] =
          error(startKey, "cf.form.error.startDate.date-earlier-than-system-start-date")

        checkForError(form, formData, expectedErrors)
      }
    }

    "transform start date correctly" when {

      "valid year and month are provided" in new SetUp {
        val formData: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString) ++
          Map(s"$endKey.month" -> month10AsString, s"$endKey.year" -> year2021AsString)

        val boundForm = form.bind(formData)

        boundForm.get.start mustBe validDate
      }
    }

    "populate CashTransactionDates form correctly (or with correct error) for input end date" when {

      "the month of end date is blank" in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)
        val endDate: Map[String, String]        = populateFormValueMap(endKey, emptyString, year2021AsString)
        val formData: Map[String, String]       = validStartDate ++ endDate
        val expectedErrors: Seq[FormError]      = error("end.month", emptyMonthEndDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year of end date is blank " in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)
        val endDate: Map[String, String]        = populateFormValueMap(endKey, month10AsString, emptyString)
        val formData: Map[String, String]       = validStartDate ++ endDate
        val expectedErrors: Seq[FormError]      = error("end.year", emptyYearEndDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the month and year of end date are empty" in new SetUp {
        val startDate: Map[String, String]    = populateFormValueMap(startKey, month10AsString, year2021AsString)
        val validEndDate: Map[String, String] = populateFormValueMap(endKey, emptyString, emptyString)
        val formData: Map[String, String]     = startDate ++ validEndDate
        val expectedErrors: Seq[FormError]    = error(endKey, emptyMonthAndYearEndDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the month value is invalid for the end date" in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          Map(s"$endKey.month" -> month14AsString, s"$endKey.year" -> year2021AsString)

        val formData: Map[String, String]  = validStartDate ++ endDate
        val expectedErrors: Seq[FormError] = Seq(FormError("end.month", invalidEndMonthKey))

        checkForError(form, formData, expectedErrors)
      }

      "the year value is invalid for the end date" in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)
        val endDate: Map[String, String]        = populateFormValueMap(endKey, month10AsString, invalidYearValue)
        val formData: Map[String, String]       = validStartDate ++ endDate
        val expectedErrors: Seq[FormError]      = error("end.year", invalidEndYearKey)

        checkForError(form, formData, expectedErrors)
      }

      "end date is in future" in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)
        val endDate: Map[String, String]        = populateFormValueMap(endKey, month10AsString, futureYear.toString)
        val formData: Map[String, String]       = validStartDate ++ endDate
        val expectedErrors: Seq[FormError]      = error(endKey, "cf.form.error.end-future-date")

        checkForError(form, formData, expectedErrors)
      }

      "end date has invalid length of the year" in new SetUp {
        val yearWithInvalidLength               = "20112"
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)
        val endDate: Map[String, String]        = populateFormValueMap(endKey, month10AsString, yearWithInvalidLength)
        val formData: Map[String, String]       = validStartDate ++ endDate
        val expectedErrors: Seq[FormError]      = error("end.year", invalidEndYearKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year length is less than 4 digits" in new SetUp {
        val yearWithInvalidLength          = "202"
        val startDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap(endKey, month10AsString, yearWithInvalidLength)

        val formData: Map[String, String]  = startDate ++ endDate
        val expectedErrors: Seq[FormError] = error("end.year", invalidEndYearKey)

        checkForError(form, formData, expectedErrors)
      }

      "end date is not within ETMP statement date period" in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap(endKey, month10AsString, (etmpStatementYear - 1).toString)

        val formData: Map[String, String]  = validStartDate ++ endDate
        val expectedErrors: Seq[FormError] = error(endKey, "cf.form.error.endDate.date-earlier-than-system-start-date")

        checkForError(form, formData, expectedErrors)
      }
    }

    "transform end date correctly" when {

      "valid year and month are provided and they are the current month and year" in new SetUp {
        val expectedEndDate: LocalDate = LocalDate.now.minusDays(1)

        form.bind(validDatesWithCurrentMonthAsEnd).get.end mustBe expectedEndDate
      }

      "valid year and month are provided and they are not the current month and year" in new SetUp {
        val expectedEndDate: LocalDate = LocalDate.of(year, month, day31)

        form.bind(completeValidDates).get.end mustBe expectedEndDate
      }
    }

    trait SetUp {
      implicit val clock: Clock = Clock.system(ZoneOffset.UTC)

      val form: Form[CashTransactionDates] = new SelectTransactionsFormProvider().apply()

      val year  = 2021
      val month = 10
      val day1  = 1
      val day31 = 31

      val todayMinusADay: Int  = LocalDate.now.getDayOfMonth - 1
      val validDate: LocalDate = LocalDate.of(year, month, day1)

      val futureYear: Int   = LocalDate.now().getYear + 1
      val etmpStatementYear = 2019

      val year2021AsString = "2021"
      val month10AsString  = "10"
      val month14AsString  = "14"

      val currentMonth = LocalDate.now.getMonthValue.toString
      val currentYear  = LocalDate.now.getYear.toString

      val startKey         = "start"
      val endKey           = "end"
      val invalidYearValue = "-"

      val emptyMonthStartDateErrorKey        = "cf.form.error.start.date.missing.month"
      val emptyYearStartDateErrorKey         = "cf.form.error.start.date.missing.year"
      val emptyMonthAndYearStartDateErrorKey = "cf.form.error.start.date-number-missing"
      val invalidStartMonthKey               = "cf.form.error.start.date.invalid.month"
      val invalidStartYearKey                = "cf.form.error.start.date.invalid.year"
      val invalidStartDateKey                = "cf.form.error.start.date.invalid.real-date"

      val emptyMonthEndDateErrorKey        = "cf.form.error.end.date.missing.month"
      val emptyYearEndDateErrorKey         = "cf.form.error.end.date.missing.year"
      val emptyMonthAndYearEndDateErrorKey = "cf.form.error.end.date-number-missing"
      val invalidEndMonthKey               = "cf.form.error.end.date.invalid.month"
      val invalidEndYearKey                = "cf.form.error.end.date.invalid.year"
      val invalidEndDateKey                = "cf.form.error.end.date.invalid.real-date"

      lazy val completeValidDates: Map[String, String] =
        populateFormValueMap(startKey, month10AsString, year2021AsString) ++
          Map(s"$endKey.month" -> month10AsString, s"$endKey.year" -> year2021AsString)

      lazy val validDatesWithCurrentMonthAsEnd: Map[String, String] =
        populateFormValueMap(startKey, month10AsString, year2021AsString) ++
          Map(s"$endKey.month" -> currentMonth, s"$endKey.year" -> currentYear)

      def populateFormValueMap(key: String, month: String, year: String): Map[String, String] =
        Map(s"$key.month" -> month, s"$key.year" -> year)
    }
  }
}
