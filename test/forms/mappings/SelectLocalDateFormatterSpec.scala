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

class SelectLocalDateFormatterSpec extends SpecBase {

  "bind" must {

    "return the correct LocalDate when the supplied data is valid" in new SetUp {
      val localYear = 2022
      val localMonth = 10

      localDateFormatter.bind(key, bindDataValid) shouldBe Right(
        LocalDate.of(localYear, localMonth, day)
      )
    }

    "return the correct FormError with key when the supplied data has empty month and year" in new SetUp {
      localDateFormatterWithEmptyMonthAndYear.bind(key, bindDataDateWithEmptyMonthAndYear) shouldBe
        Left(Seq(FormError("start.month", List(emptyMonthAndYearMsgKey), List())))
    }

    "return the correct FormError with keys when the supplied data is empty month" in new SetUp {
      localDateFormatter.bind(key, bindDataDateWithEmptyMonth) shouldBe
        Left(Seq(FormError("start.month", List(emptyMonthMsgKey), List())))
    }

    "return the correct FormError with keys when the supplied data empty year" in new SetUp {
      localDateFormatter.bind(key, bindDataDateWithEmptyYear) shouldBe
        Left(Seq(FormError("start.year", List(emptyYearMsgKey), List())))
    }

    "return the correct FormError with keys when the supplied data is invalid month" in new SetUp {

      localDateFormatter.bind(key, bindDataInValidMonth) shouldBe
        Left(Seq(FormError("start.month", List(invalidDateMsgKey), List())))
    }

    "return the correct FormError with keys when the supplied data is invalid year" in new SetUp {
      localDateFormatter.bind(key, bindDataInValidYear) shouldBe
        Left(Seq(FormError("start.year", List(invalidDateMsgKey), List())))
    }

  }

  "updateFormErrorKeys" must {
    "append the month in the existing key when month is incorrect" in new SetUp {

      localDateFormatter.updateFormErrorKeys(
        key,
        month1,
        year
      ) shouldBe s"$key.month"
    }

    "append the year in the existing key when year is incorrect" in new SetUp {

      localDateFormatter.updateFormErrorKeys(
        key,
        month,
        year2
      ) shouldBe s"$key.year"
    }
  }

  "formErrorKeysInCaseOfEmptyOrNonNumericValues" must {

    "return key.month as updated key when month value is empty" in new SetUp {

      val formDataWithEmptyMonth: Map[String, String] = Map(s"$key.month" -> emptyString, s"$key.year" -> "2021")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyMonth
      ) shouldBe s"$key.month"
    }

    "return key.year as updated key when year value is empty" in new SetUp {

      val formDataWithEmptyYear: Map[String, String] = Map(s"$key.month" -> "10", s"$key.year" -> emptyString)

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyYear
      ) shouldBe s"$key.year"
    }

    "return key.month as updated key when month value is not numeric" in new SetUp {

      val formDataWithNonNumericMonth: Map[String, String] = Map(
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
        Map(s"$key.month" -> "10", s"$key.year" -> "et")

      localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithNonNumericYear
      ) shouldBe s"$key.year"
    }

    "return the correct LocalDate when the supplied data is valid and useLastDayOfMonth is true" in new SetUp {

      val lastDay: Int = 31

      localDateFormatterWithLastDay.bind(key, bindDataValidForLastDay) shouldBe Right(
        LocalDate.of(year, month, lastDay)
      )
    }
  }

  trait SetUp {

    val year: Int = 2023
    val year1: Int = -1
    val year2: Int = -2022
    val month: Int = 12
    val month1: Int = 14
    val day: Int = 1

    val key = "start"
    val emptyMonthAndYearMsgKey = "cf.form.error.start.date.empty.month.year"
    val emptyMonthMsgKey = "cf.form.error.start.date.empty.month"
    val emptyYearMsgKey = "cf.form.error.start.date.empty.year"
    val invalidDateMsgKey = "cf.form.error.start.date.invalid"

    val bindDataValid: Map[String, String] = Map(
      "start.day" -> day.toString, "start.month" -> "10", "start.year" -> "2022")

    val bindDataEmptyDate: Map[String, String] = Map(
      "start.day" -> day.toString, "start.month" -> emptyString, "start.year" -> emptyString)

    val localDateFormatter = new SelectLocalDateFormatter(
      emptyMonthAndYearMsgKey,
      emptyMonthMsgKey,
      emptyYearMsgKey,
      invalidDateMsgKey,
      Seq(),
      useLastDayOfMonth = false
    )

    val localDateFormatterWithEmptyMonthAndYear = new SelectLocalDateFormatter(
      emptyMonthAndYearMsgKey,
      emptyMonthMsgKey,
      emptyYearMsgKey,
      invalidDateMsgKey,
      Seq(),
      useLastDayOfMonth = false
    )

    val localDateFormatterWithLastDay = new SelectLocalDateFormatter(
      emptyMonthAndYearMsgKey,
      emptyMonthMsgKey,
      emptyYearMsgKey,
      invalidDateMsgKey,
      Seq(),
      useLastDayOfMonth = true
    )

    val bindDataDateWithEmptyMonthAndYear: Map[String, String] =
      Map("start.day" -> "1", "start.month" -> emptyString, "start.year" -> emptyString)

    val bindDataDateWithEmptyMonth: Map[String, String] =
      Map("start.day" -> "1", "start.month" -> emptyString, "start.year" -> "2022")

    val bindDataDateWithEmptyYear: Map[String, String] =
      Map("start.day" -> "1", "start.month" -> "10", "start.year" -> emptyString)

    val bindDataInValidDate: Map[String, String] =
      Map("start.day" -> "1", "start.month" -> "14", "start.year" -> "2023")

    val bindDataInValidMonth: Map[String, String] =
      Map("start.day" -> "1", "start.month" -> "14", "start.year" -> "2022")

    val bindDataInValidYear: Map[String, String] =
      Map("start.day" -> "1", "start.month" -> "10", "start.year" -> "-")

    val bindDataValidForLastDay: Map[String, String] = Map(
      "start.month" -> month.toString,
      "start.year" -> year.toString
    )
  }
}
