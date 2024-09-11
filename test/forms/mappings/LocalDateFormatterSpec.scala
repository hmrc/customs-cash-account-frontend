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

class LocalDateFormatterSpec extends SpecBase {
  "bind" must {

    "return the correct LocalDate when the supplied data is valid" in new SetUp {

      val year = 2022
      val month = 10
      val day = 12

      localDateFormatter.bind(key, bindDataValid) shouldBe Right(
        LocalDate.of(year, month, day)
      )
    }

    "return the correct FormError with keys when the supplied data is invalid" in new SetUp {

      localDateFormatter.bind(key, bindDataDateWithEmptyDay) shouldBe
        Left(Seq(FormError("start.day", List(dayMsgKey), List())))

      localDateFormatter.bind(key, bindDataDateWithEmptyMonth) shouldBe
        Left(Seq(FormError("start.month", List(monthMsgKey), List())))

      localDateFormatter.bind(key, bindDataDateWithEmptyYear) shouldBe
        Left(Seq(FormError("start.year", List(yearMsgKey), List())))

      localDateFormatter.bind(key, bindDataInValidDate) shouldBe
        Left(Seq(FormError("start.day", List(invalidDateMsgKey), List())))

      localDateFormatter.bind(key, bindDataInValidMonth) shouldBe
        Left(Seq(FormError("start.month", List(invalidDateMsgKey), List())))

      localDateFormatter.bind(key, bindDataInValidYear) shouldBe
        Left(Seq(FormError("start.year", List(invalidMsgKey), List())))
    }
  }

  "updateFormErrorKeys" must {
    val year = 2023
    val year1 = -1
    val year2 = -2022
    val month = 12
    val month1 = 14
    val day = 32
    val day1 = 12

    "append the day in the existing key when day is incorrect or all(day, month and year) are incorrect" in new SetUp {

      localDateFormatter.updateFormErrorKeys(
        key,
        day,
        month,
        year
      ) shouldBe s"$key.day"

      localDateFormatter.updateFormErrorKeys(
        key,
        day,
        month1,
        year1
      ) shouldBe s"$key.day"
    }

    "append the month in the existing key when month is incorrect" in new SetUp {

      localDateFormatter.updateFormErrorKeys(
        key,
        day1,
        month1,
        year
      ) shouldBe s"$key.month"
    }

    "append the year in the existing key when year is incorrect" in new SetUp {

      localDateFormatter.updateFormErrorKeys(
        key,
        day1,
        month,
        year2
      ) shouldBe s"$key.year"
    }
  }

  "formErrorKeysInCaseOfEmptyOrNonNumericValues" must {

    "return key.day as updated key when day value is empty" in new SetUp {

      val formDataWithEmptyDay: Map[String, String] =
        Map(s"$key.day" -> "", s"$key.month" -> "10", s"$key.year" -> "2021")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyDay
      ) shouldBe s"$key.day"
    }

    "return key.month as updated key when month value is empty" in new SetUp {

      val formDataWithEmptyDay: Map[String, String] =
        Map(s"$key.day" -> "10", s"$key.month" -> "", s"$key.year" -> "2021")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyDay
      ) shouldBe s"$key.month"
    }

    "return key.year as updated key when year value is empty" in new SetUp {

      val formDataWithEmptyDay: Map[String, String] =
        Map(s"$key.day" -> "10", s"$key.month" -> "10", s"$key.year" -> "")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyDay
      ) shouldBe s"$key.year"
    }

    "return key.day as updated key when all date fields are empty" in new SetUp {

      val formDataWithEmptyDay: Map[String, String] =
        Map(s"$key.day" -> "", s"$key.month" -> "", s"$key.year" -> "")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyDay
      ) shouldBe s"$key.day"
    }

    "return key.day as updated key when day value is not numeric" in new SetUp {

      val formDataWithNonNumericDay: Map[String, String] =
        Map(s"$key.day" -> "se", s"$key.month" -> "10", s"$key.year" -> "2021")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithNonNumericDay
      ) shouldBe s"$key.day"
    }

    "return key.month as updated key when month value is not numeric" in new SetUp {

      val formDataWithNonNumericMonth: Map[String, String] = Map(
        s"$key.day" -> "10",
        s"$key.month" -> "test",
        s"$key.year" -> "2021"
      )

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithNonNumericMonth
      ) shouldBe s"$key.month"
    }

    "return key.year as updated key when year value is not numeric" in new SetUp {
      val formDataWithNonNumericYear: Map[String, String] =
        Map(s"$key.day" -> "10", s"$key.month" -> "10", s"$key.year" -> "et")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithNonNumericYear
      ) shouldBe s"$key.year"
    }
  }

  trait SetUp {
    val key = "start"
    val invalidMsgKey = "cf.form.error.start.date-number-invalid"
    val dayMsgKey = "cf.form.error.start.date.invalid.day"
    val monthMsgKey = "cf.form.error.start.date.invalid.month"
    val yearMsgKey = "cf.form.error.start.date.invalid.year"
    val invalidDateMsgKey = "cf.form.error.start.date.invalid.real-date"
    val bindDataValid: Map[String, String] =
      Map("start.day" -> "12", "start.month" -> "10", "start.year" -> "2022")
    val bindDataEmptyDate: Map[String, String] =
      Map("start.day" -> "", "start.month" -> "", "start.year" -> "")

    val localDateFormatter = new LocalDateFormatter(
      invalidMsgKey,
      dayMsgKey,
      monthMsgKey,
      yearMsgKey,
      invalidDateMsgKey,
      Seq()
    )

    val bindDataDateWithEmptyDay: Map[String, String] =
      Map("start.day" -> "", "start.month" -> "10", "start.year" -> "2022")

    val bindDataDateWithEmptyMonth: Map[String, String] =
      Map("start.day" -> "10", "start.month" -> "", "start.year" -> "2022")

    val bindDataDateWithEmptyYear: Map[String, String] =
      Map("start.day" -> "10", "start.month" -> "10", "start.year" -> "")

    val bindDataInValidDate: Map[String, String] =
      Map("start.day" -> "34", "start.month" -> "14", "start.year" -> "2023")

    val bindDataInValidMonth: Map[String, String] =
      Map("start.day" -> "10", "start.month" -> "14", "start.year" -> "2022")

    val bindDataInValidYear: Map[String, String] =
      Map("start.day" -> "10", "start.month" -> "10", "start.year" -> "-")
  }
}