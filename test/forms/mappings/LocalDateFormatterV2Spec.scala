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

package forms.mappings

import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.data.FormError
import utils.SpecBase

import java.time.LocalDate

class LocalDateFormatterV2Spec extends SpecBase {
  "bind" must {

    "return the correct LocalDate when the supplied data is valid" in new SetUp {

      val year  = 2022
      val month = 10
      val day   = 1

      localDateFormatter.bind(key, bindDataValid) shouldBe Right(
        LocalDate.of(year, month, day)
      )
    }

    "return the correct FormError with keys when the supplied data is invalid" in new SetUp {

      localDateFormatter.bind(key, bindDataDateWithEmptyMonth) shouldBe
        Left(Seq(FormError("start.month", List(monthMsgKey), List())))

      localDateFormatter.bind(key, bindDataDateWithEmptyYear) shouldBe
        Left(Seq(FormError("start.year", List(yearMsgKey), List())))

      localDateFormatter.bind(key, bindDataDateWithEmptyMonthAndYear) shouldBe
        Left(Seq(FormError("start", List(missingDateMsgKey), List())))

      localDateFormatter.bind(key, bindDataInvalidDate) shouldBe
        Left(Seq(FormError("start", List(invalidDateMsgKey), List())))

      localDateFormatter.bind(key, bindDataInvalidMonth) shouldBe
        Left(Seq(FormError("start.month", List(invalidMonthKey), List())))

      localDateFormatter.bind(key, bindDataInvalidYear) shouldBe
        Left(Seq(FormError("start.year", List(invalidYearKey), List())))
    }
  }

  "formErrorKeysInCaseOfEmptyOrNonNumericValues" must {

    "return key.month as updated key when month value is empty" in new SetUp {

      val formDataWithEmptyDay: Map[String, String] =
        Map(s"$key.month" -> "", s"$key.year" -> "2021")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyDay
      ) shouldBe s"$key.month"
    }

    "return key.year as updated key when year value is empty" in new SetUp {

      val formDataWithEmptyDay: Map[String, String] =
        Map(s"$key.month" -> "10", s"$key.year" -> "")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyDay
      ) shouldBe s"$key.year"
    }

    "return key.month as updated key when month value is not numeric" in new SetUp {

      val formDataWithNonNumericMonth: Map[String, String] = Map(
        s"$key.month" -> "test",
        s"$key.year"  -> "2021"
      )

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithNonNumericMonth
      ) shouldBe s"$key.month"
    }

    "return key.year as updated key when year value is not numeric" in new SetUp {
      val formDataWithNonNumericYear: Map[String, String] =
        Map(s"$key.month" -> "10", s"$key.year" -> "et")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithNonNumericYear
      ) shouldBe s"$key.year"
    }
  }

  trait SetUp {
    val key               = "start"
    val missingDateMsgKey = "cf.form.error.start.date-number-missing"
    val invalidMonthKey   = "cf.form.error.start.date.invalid.month"
    val invalidYearKey    = "cf.form.error.start.date.invalid.year"
    val monthMsgKey       = "cf.form.error.start.date.missing.month"
    val yearMsgKey        = "cf.form.error.start.date.missing.year"
    val invalidDateMsgKey = "cf.form.error.start.date.invalid.real-date"

    val bindDataValid: Map[String, String]     =
      Map("start.month" -> "10", "start.year" -> "2022")
    val bindDataEmptyDate: Map[String, String] =
      Map("start.month" -> "", "start.year" -> "")

    val localDateFormatter = new LocalDateFormatterV2(
      emptyStartMonth = "cf.form.error.start.date.missing.month",
      emptyStartYear = "cf.form.error.start.date.missing.year",
      emptyEndMonth = "cf.form.error.end.date.missing.month",
      emptyEndYear = "cf.form.error.end.date.missing.year",
      emptyStartDate = "cf.form.error.start.date-number-missing",
      emptyEndDate = "cf.form.error.end.date-number-missing",
      invalidMonth = "cf.form.error.start.date.invalid.month",
      invalidYear = "cf.form.error.start.date.invalid.year",
      invalidDate = "cf.form.error.start.date.invalid.real-date"
    )

    val bindDataDateWithEmptyMonthAndYear: Map[String, String] =
      Map("start.month" -> "", "start.year" -> "")

    val bindDataDateWithEmptyMonth: Map[String, String] =
      Map("start.month" -> "", "start.year" -> "2022")

    val bindDataDateWithEmptyYear: Map[String, String] =
      Map("start.month" -> "10", "start.year" -> "")

    val bindDataInvalidDate: Map[String, String] =
      Map("start.month" -> "14", "start.year" -> "202p")

    val bindDataInvalidMonth: Map[String, String] =
      Map("start.month" -> "14", "start.year" -> "2022")

    val bindDataInvalidYear: Map[String, String] =
      Map("start.month" -> "10", "start.year" -> "-")
  }
}
