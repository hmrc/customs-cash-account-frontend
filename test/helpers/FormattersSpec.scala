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

package helpers

import helpers.Formatters.{ddMMyyyyDateFormatter, formatCurrencyAmount, yyyyMMddDateFormatter, yyyyMMddHHmmssDateFormatter}
import utils.SpecBase

import java.time.{LocalDate, LocalDateTime}

class FormattersSpec extends SpecBase {

  "fileSizeFormat" must {

    "returns 1KB for size 1" in {
      val size: Long = 1

      val result = Formatters.fileSizeFormat(size)
      result mustBe "1KB"
    }

    "return 1000.0MB for size of 1000000000" in {
      val size: Long = 1000000000

      val result = Formatters.fileSizeFormat(size)
      result mustBe "1000.0MB"
    }

    "return correct value for size of 1000 " in {
      val size: Long = 100000

      val result = Formatters.fileSizeFormat(size)
      result mustBe "100KB"
    }
  }

  "yyyyMMddDateFormatter" should {

    "return date in correct pattern" in {
      val year = 2024
      val month = 1
      val dayOfMonth = 29

      yyyyMMddDateFormatter.format(LocalDate.of(year, month, dayOfMonth)) mustBe "2024-01-29"
    }
  }

  "yyyyMMddHHmmssDateFormatter" should {

    "return date in correct pattern" in {
      val year = 2024
      val month = 1
      val dayOfMonth = 29
      val hourOfDay = 14
      val minOfHour = 6
      val secsOfMin = 26

      yyyyMMddHHmmssDateFormatter.format(LocalDateTime.of(
        year,
        month,
        dayOfMonth,
        hourOfDay,
        minOfHour,
        secsOfMin)) mustBe "20240129140626"
    }
  }

  "ddMMyyyyDateFormatter" should {

    "return date in correct pattern" in {
      val year = 2024
      val month = 1
      val dayOfMonth = 29

      ddMMyyyyDateFormatter.format(LocalDate.of(year, month, dayOfMonth)) mustBe "29012024"
    }
  }

  "formatCurrencyAmount" should {

    "return result as £0 when value is 0" in {
      formatCurrencyAmount(BigDecimal(0)) mustBe "£0"
    }

    "return result with two decimal points when value is greater than 0" in {
      val wholeValue = 200
      val valueWithTwoDecimalPoint = 530.45
      val valueWithFourDigitsAndTwoDecimalPoint = 3489.00
      val wholeValueWithFiveDigits = 54000
      val valueWithSixDigitsWithTwoDecimalPoints = 554678.56

      formatCurrencyAmount(BigDecimal(2)) mustBe "£2.00"
      formatCurrencyAmount(BigDecimal(wholeValue)) mustBe "£200.00"
      formatCurrencyAmount(BigDecimal(valueWithTwoDecimalPoint)) mustBe "£530.45"
      formatCurrencyAmount(BigDecimal(valueWithFourDigitsAndTwoDecimalPoint)) mustBe "£3,489.00"
      formatCurrencyAmount(BigDecimal(wholeValueWithFiveDigits)) mustBe "£54,000.00"
      formatCurrencyAmount(BigDecimal(valueWithSixDigitsWithTwoDecimalPoints)) mustBe "£554,678.56"
    }
  }

}
