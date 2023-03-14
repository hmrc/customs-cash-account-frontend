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

import java.time.{LocalDate, LocalDateTime}
import org.scalatest.matchers.should.Matchers._
import forms.mappings.Constraints
import play.api.data.validation.{Invalid, Valid, ValidationError}

class ConstraintsSpec extends SpecBase with Constraints {

  "Constraints" should {

    "currentDate" must {
      "current date should return a local date time" in new Setup {
        val target = currentDate
        ld mustBe target
      }
    }

    "beforeCurrentMonth" must {
      "return Valid if request is equal to current month" in new Setup {
        val result = beforeCurrentMonth("error.min").apply(ld)
        result mustBe Valid
      }

      "return valid result for current date" in new Setup {
        val result = beforeCurrentMonth("error.min").apply(ld)
        result mustBe Valid
      }

      "return valid result for date in past" in {
        def oldDate: LocalDate = LocalDate.of(2008, 1, 1)
        val result = beforeCurrentMonth("error.min").apply(oldDate)
        result mustBe Valid
      }

      "return invalid result is years are within 6" in new Setup {
        def monthOld: LocalDate = LocalDateTime.now().minusMonths(5).toLocalDate
        val result = beforeCurrentMonth("error.min").apply(monthOld)
        result mustBe Invalid(List(ValidationError(List("error.min"))))
      }
    }

    "beforeCurrentDate" must {
      "return Valid if request before or equal to current date" in new Setup {
        val result = beforeCurrentDate("error.min").apply(ld)
        result mustBe Valid
      }

      "return invalid if request is after current date" in new Setup {
        def futureMonth: LocalDate = LocalDateTime.now().plusMonths(5).toLocalDate
        val result = beforeCurrentDate("error.min").apply(futureMonth)
        result mustBe Invalid(List(ValidationError(List("error.min"))))
      }
    }
  }

  trait Setup {
    def ld: LocalDate = LocalDateTime.now().toLocalDate
  }
}
