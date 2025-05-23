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

import java.time
import java.time.{LocalDate, YearMonth}

class SelectYearMonthFormatterSpec extends SpecBase {

  "bind" must {

    "return the correct LocalDate when the supplied data is valid" in new SetUp {
      val localYear  = 2022
      val localMonth = 10

      yearMonthFormatter.bind(key, bindDataValid) shouldBe Right(YearMonth.of(localYear, localMonth))
    }

    "return the correct FormError with key when the supplied data has empty month and year" in new SetUp {
      yearMonthFormatterWithEmptyMonthAndYear.bind(key, bindDataDateWithEmptyMonthAndYear) shouldBe
        Left(Seq(FormError("start.month", List(emptyMonthAndYearMsgKey), List())))
    }

    "return the correct FormError with keys when the supplied data is empty month" in new SetUp {
      yearMonthFormatter.bind(key, bindDataDateWithEmptyMonth) shouldBe
        Left(Seq(FormError("start.month", List(emptyMonthMsgKey), List())))
    }

    "return the correct FormError with keys when the supplied data empty year" in new SetUp {
      yearMonthFormatter.bind(key, bindDataDateWithEmptyYear) shouldBe
        Left(Seq(FormError("start.year", List(emptyYearMsgKey), List())))
    }

    "return the correct FormError with keys when the supplied data is invalid month" in new SetUp {
      yearMonthFormatter.bind(key, bindDataInValidMonth) shouldBe
        Left(Seq(FormError("start.month", List(invalidDateMsgKey), List())))
    }

    "return the correct FormError with keys when the supplied data is invalid year" in new SetUp {
      yearMonthFormatter.bind(key, bindDataInValidYear) shouldBe
        Left(Seq(FormError("start.year", List(invalidDateMsgKey), List())))
    }
  }

  "unbind" must {

    "return the correct plain data" in new SetUp {
      yearMonthFormatter.unbind(key, YearMonth.of(year, month)) mustBe Map(
        s"$key.month" -> month.toString,
        s"$key.year"  -> year.toString
      )
    }
  }

  "updateFormErrorKeys" must {
    "append the month in the existing key when month is incorrect" in new SetUp {

      yearMonthFormatter.updateFormErrorKeys(
        key,
        month1,
        year
      ) shouldBe s"$key.month"
    }

    "append the year in the existing key when year is incorrect" in new SetUp {

      yearMonthFormatter.updateFormErrorKeys(
        key,
        month,
        year2
      ) shouldBe s"$key.year"
    }
  }

  "formErrorKeysInCaseOfEmptyOrNonNumericValues" must {

    "return key.month as updated key when month value is empty" in new SetUp {

      val formDataWithEmptyMonth: Map[String, String] = Map(s"$key.month" -> emptyString, s"$key.year" -> "2021")

      yearMonthFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyMonth
      ) shouldBe s"$key.month"
    }

    "return key.year as updated key when year value is empty" in new SetUp {

      val formDataWithEmptyYear: Map[String, String] = Map(s"$key.month" -> "10", s"$key.year" -> emptyString)

      yearMonthFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithEmptyYear
      ) shouldBe s"$key.year"
    }

    "return key.month as updated key when month value is not numeric" in new SetUp {

      val formDataWithNonNumericMonth: Map[String, String] = Map(
        s"$key.month" -> "test",
        s"$key.year"  -> "2021"
      )

      yearMonthFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithNonNumericMonth
      ) shouldBe s"$key.month"
    }

    "return key.year as updated key when year value is not numeric" in new SetUp {
      val formDataWithNonNumericYear: Map[String, String] =
        Map(s"$key.month" -> "10", s"$key.year" -> "et")

      yearMonthFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
        key,
        formDataWithNonNumericYear
      ) shouldBe s"$key.year"
    }
  }

  trait SetUp {

    val year: Int            = 2023
    val year2: Int           = -2022
    val month: Int           = 12
    val month1: Int          = 14
    val day: Int             = 1
    val yesterday: LocalDate = LocalDate.now.minusDays(1)

    val key                     = "start"
    val emptyMonthAndYearMsgKey = "cf.cash-account.transactions.request.start.date.empty.month.year"
    val emptyMonthMsgKey        = "cf.cash-account.transactions.request.start.date.empty.month"
    val emptyYearMsgKey         = "cf.cash-account.transactions.request.start.date.empty.year"
    val invalidDateMsgKey       = "cf.cash-account.transactions.request.start.date.invalid"

    val bindDataValid: Map[String, String] = Map("start.month" -> "10", "start.year" -> "2022")

    val bindDataForCurrentMonth: Map[String, String] = Map(
      "start.month" -> s"${LocalDate.now.getMonthValue}",
      "start.year"  -> s"${LocalDate.now.getYear}"
    )

    val yearMonthFormatter = new SelectYearMonthFormatter(
      emptyMonthAndYearMsgKey,
      emptyMonthMsgKey,
      emptyYearMsgKey,
      invalidDateMsgKey,
      Seq()
    )

    val yearMonthFormatterWithEmptyMonthAndYear = new SelectYearMonthFormatter(
      emptyMonthAndYearMsgKey,
      emptyMonthMsgKey,
      emptyYearMsgKey,
      invalidDateMsgKey,
      Seq()
    )

    val yearMonthFormatterWithLastDay = new SelectYearMonthFormatter(
      emptyMonthAndYearMsgKey,
      emptyMonthMsgKey,
      emptyYearMsgKey,
      invalidDateMsgKey,
      Seq()
    )

    val bindDataDateWithEmptyMonthAndYear: Map[String, String] =
      Map("start.month" -> emptyString, "start.year" -> emptyString)

    val bindDataDateWithEmptyMonth: Map[String, String] =
      Map("start.month" -> emptyString, "start.year" -> "2022")

    val bindDataDateWithEmptyYear: Map[String, String] =
      Map("start.month" -> "10", "start.year" -> emptyString)

    val bindDataInValidMonth: Map[String, String] =
      Map("start.month" -> "14", "start.year" -> "2022")

    val bindDataInValidYear: Map[String, String] =
      Map("start.month" -> "10", "start.year" -> "-")

    val bindDataValidForLastDay: Map[String, String] = Map(
      "start.month" -> month.toString,
      "start.year"  -> year.toString
    )
  }
}
