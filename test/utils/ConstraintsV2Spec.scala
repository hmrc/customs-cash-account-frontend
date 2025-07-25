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

package utils

import forms.mappings.ConstraintsV2
import play.api.data.validation.{Invalid, Valid, ValidationError, ValidationResult}
import utils.RegexPatterns.{mrnRegex, paymentRegex, ucrRegex}

import java.time.{LocalDate, LocalDateTime}

class ConstraintsV2Spec extends SpecBase with ConstraintsV2 {

  "ConstraintsV2" should {

    "currentDate" must {

      "current date should return a local date time" in new Setup {
        val target: LocalDate = currentDate
        ld mustBe target
      }
    }

    "beforeCurrentMonth" must {

      "return Valid if request is equal to current month" in new Setup {
        val result: ValidationResult = beforeCurrentMonth("error.min").apply(ld)
        result mustBe Valid
      }

      "return valid result for current date" in new Setup {
        val result: ValidationResult = beforeCurrentMonth("error.min").apply(ld)
        result mustBe Valid
      }

      "return valid result for date in past" in {
        val day   = 1
        val month = 1
        val year  = 2008

        def oldDate: LocalDate = LocalDate.of(year, month, day)

        val result = beforeCurrentMonth("error.min").apply(oldDate)
        result mustBe Valid
      }

      "return invalid result if request date is not within 6 months in the past and " +
        "request and current date years are not same" in new Setup {
          val eightMonths = 8
          val month9th    = 9

          def monthOld: LocalDate = LocalDateTime.now().minusMonths(eightMonths).toLocalDate

          val result: ValidationResult = beforeCurrentMonth("error.min").apply(monthOld)

          if (LocalDateTime.now().getMonthValue < month9th) {
            result mustBe Invalid(List(ValidationError(List("error.min"))))
          } else {
            result mustBe Valid
          }
        }

      "return invalid result if request is after current month" in new Setup {
        def monthOld: LocalDate = LocalDateTime.now().plusMonths(2).toLocalDate

        val result: ValidationResult = beforeCurrentMonth("error.min").apply(monthOld)
        result mustBe Invalid(List(ValidationError(List("error.min"))))
      }

      "return invalid result if request is after current year" in new Setup {
        def monthOld: LocalDate = LocalDateTime.now().plusYears(1).toLocalDate

        val result: ValidationResult = beforeCurrentMonth("error.min").apply(monthOld)
        result mustBe Invalid(List(ValidationError(List("error.min"))))
      }
    }

    "beforeCurrentDate" must {
      "return Valid if request before or equal to current date" in new Setup {
        val result: ValidationResult = beforeCurrentDate("error.min").apply(ld)

        result mustBe Valid
      }

      "return invalid if request is after current date" in new Setup {
        val months = 6

        def futureMonth: LocalDate = LocalDateTime.now().plusMonths(months).toLocalDate

        val result: ValidationResult = beforeCurrentDate("error.min").apply(futureMonth)
        result mustBe Invalid(List(ValidationError(List("error.min"))))
      }
    }

    "validateSearchInput" must {

      "return Valid when given a valid MRN" in new Setup {
        val result: ValidationResult = validateSearchInput("error.key")("GDRC1345317D1113315")
        result mustBe Valid
      }

      "return Invalid when given an invalid MRN" in new Setup {
        val result: ValidationResult = validateSearchInput("error.key")("123DR_1345!D1113315He22")
        result mustBe Invalid(Seq(ValidationError("error.key", patterns: _*)))
      }

      "return Valid when given an valid UCR" in new Setup {
        val result: ValidationResult = validateSearchInput("error.key")("GB11685628909193-9182-888-416D")
        result mustBe Valid
      }

      "return Invalid when given an invalid UCR" in new Setup {
        val result: ValidationResult = validateSearchInput("error.key")("GB1234567890_1134!7456-914+121D")
        result mustBe Invalid(Seq(ValidationError("error.key", patterns: _*)))
      }

      "return Valid when given a valid payment amount" in new Setup {
        val paymentResult: ValidationResult = validateSearchInput("error.key")("£30.00")
        paymentResult mustBe Valid
      }

      "return Invalid when given an invalid payment amount" in new Setup {
        val paymentResult: ValidationResult = validateSearchInput("error.key")("-_£30.00.00")
        paymentResult mustBe Invalid(Seq(ValidationError("error.key", patterns: _*)))
      }
    }
  }

  trait Setup {
    val patterns: Seq[String] = Seq(mrnRegex.regex, paymentRegex.regex, ucrRegex.regex)

    def ld: LocalDate = LocalDateTime.now().toLocalDate
  }
}
