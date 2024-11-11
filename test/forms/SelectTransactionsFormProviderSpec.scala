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
        val startDate: Map[String, String] = populateFormValueMap(startKey, emptyString, year2021AsString)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.month", emptyMonthStartDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year value of start date is blank" in new SetUp {
        val startDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, emptyString)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.year", emptyYearStartDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the month and year values of start date are empty" in new SetUp {
        val startDate: Map[String, String] = populateFormValueMap(startKey, emptyString, emptyString)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.month", emptyMonthAndYearStartDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the month value is invalid for the start date" in new SetUp {
        val startDate: Map[String, String] = populateFormValueMap(startKey, month14AsString, year2021AsString)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.month", invalidStartDateKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year value is invalid for the start date" in new SetUp {
        val invalidYearValue = "-"

        val startDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, invalidYearValue)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("start.year", invalidStartDateKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year length is less than 4 digits" in new SetUp {
        val yearWithInvalidLength = "202"

        val startDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, yearWithInvalidLength)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error(startKey, invalidYearLengthKey)

        checkForError(form, formData, expectedErrors)
      }

      "start date has invalid length of the year" in new SetUp {
        val yearWithInvalidLength = "20211"

        val startDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, yearWithInvalidLength)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error(startKey, invalidYearLengthKey)

        checkForError(form, formData, expectedErrors)
      }

      "start date is not within ETMP statement date period" in new SetUp {
        val startDate: Map[String, String] =
          populateFormValueMap(startKey, month10AsString, (etmpStatementYear - 1).toString)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, year2021AsString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] =
          error(startKey, "cf.form.error.startDate.date-earlier-than-system-start-date")

        checkForError(form, formData, expectedErrors)
      }
    }

    "populate CashTransactionDates form correctly (or with correct error) for input end date" when {

      "end date is valid and uses the last day of the month" in new SetUp {
        val expectedEndDate: LocalDate = LocalDate.of(year, month, day31)
        form.bind(completeValidDates).get mustBe CashTransactionDates(start = validDate, end = expectedEndDate)
      }

      "the month of end date is blank" in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] = populateFormValueMap(endKey, emptyString, year2021AsString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.month", emptyMonthEndDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year of end date is blank " in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, emptyString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.year", emptyYearEndDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the month and year of end date are empty" in new SetUp {
        val startDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val validEndDate: Map[String, String] = populateFormValueMap(endKey, emptyString, emptyString)

        val formData: Map[String, String] = startDate ++ validEndDate

        val expectedErrors: Seq[FormError] = error("end.month", emptyMonthAndYearEndDateErrorKey)

        checkForError(form, formData, expectedErrors)
      }

      "the month value is invalid for the end date" in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          Map(s"$endKey.month" -> month14AsString, s"$endKey.year" -> year2021AsString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = Seq(FormError("end.month", invalidEndDateKey))

        checkForError(form, formData, expectedErrors)
      }

      "the year value is invalid for the end date" in new SetUp {
        val invalidYearValue = "-"

        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] = populateFormValueMap(endKey, month14AsString, invalidYearValue)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error("end.year", invalidEndDateKey)

        checkForError(form, formData, expectedErrors)
      }

      "end date is in future" in new SetUp {
        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, futureYear.toString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error(endKey, "cf.form.error.end-future-date")

        checkForError(form, formData, expectedErrors)
      }

      "end date has invalid length of the year" in new SetUp {
        val yearWithInvalidLength = "20112"

        val validStartDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] = populateFormValueMap(endKey, month10AsString, yearWithInvalidLength)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error(endKey, invalidYearLengthKey)

        checkForError(form, formData, expectedErrors)
      }

      "the year length is less than 4 digits" in new SetUp {
        val yearWithInvalidLength = "202"

        val startDate: Map[String, String] = populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap(endKey, month10AsString, yearWithInvalidLength)

        val formData: Map[String, String] = startDate ++ endDate

        val expectedErrors: Seq[FormError] = error(endKey, invalidYearLengthKey)

        checkForError(form, formData, expectedErrors)
      }

      "end date is not within ETMP statement date period" in new SetUp {
        val validStartDate: Map[String, String] =
          populateFormValueMap(startKey, month10AsString, year2021AsString)

        val endDate: Map[String, String] =
          populateFormValueMap(endKey, month10AsString, (etmpStatementYear - 1).toString)

        val formData: Map[String, String] = validStartDate ++ endDate

        val expectedErrors: Seq[FormError] = error(endKey, "cf.form.error.endDate.date-earlier-than-system-start-date")

        checkForError(form, formData, expectedErrors)
      }
    }
  }

  trait SetUp {
    implicit val clock: Clock = Clock.system(ZoneOffset.UTC)

    val form: Form[CashTransactionDates] = new SelectTransactionsFormProvider().apply()

    val year = 2021
    val month = 10
    val day = 1
    val day31 = 31

    val validDate: LocalDate = LocalDate.of(year, month, day)
    val futureYear: Int = LocalDate.now().getYear + 1
    val etmpStatementYear = 2019

    val year2021AsString = "2021"
    val month10AsString = "10"
    val month14AsString = "14"

    val startKey = "start"
    val endKey = "end"

    val emptyMonthStartDateErrorKey = "cf.cash-account.transactions.request.start.date.empty.month"
    val emptyYearStartDateErrorKey = "cf.cash-account.transactions.request.start.date.empty.year"
    val emptyMonthAndYearStartDateErrorKey = "cf.cash-account.transactions.request.start.date.empty.month.year"
    val invalidStartDateKey = "cf.cash-account.transactions.request.start.date.invalid"

    val emptyMonthEndDateErrorKey = "cf.cash-account.transactions.request.end.date.empty.month"
    val emptyYearEndDateErrorKey = "cf.cash-account.transactions.request.end.date.empty.year"
    val emptyMonthAndYearEndDateErrorKey = "cf.cash-account.transactions.request.end.date.empty.month.year"
    val invalidEndDateKey = "cf.cash-account.transactions.request.end.date.invalid"

    val invalidYearLengthKey = "date.year.length.invalid"

    lazy val completeValidDates: Map[String, String] =
      populateFormValueMap(startKey, month10AsString, year2021AsString) ++
        Map(s"$endKey.month" -> month10AsString, s"$endKey.year" -> year2021AsString)

    def populateFormValueMap(key: String,
                             month: String,
                             year: String): Map[String, String] =
      Map(s"$key.day" -> day.toString, s"$key.month" -> month, s"$key.year" -> year)
  }
}
