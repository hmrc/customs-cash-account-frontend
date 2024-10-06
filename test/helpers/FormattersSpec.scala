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

import helpers.Formatters.*
import utils.SpecBase
import play.api.Application
import play.api.i18n.Messages
import utils.TestData.{
  DAY_1, DAY_15, DAY_10, MONTH_1, MONTH_2, MONTH_3, MONTH_4, MONTH_5, MONTH_6, MONTH_7, MONTH_8, MONTH_9,
  MONTH_10, MONTH_11, MONTH_12, YEAR_2021
}

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

  "dateAsMonth" should {
    "return month as a valid value" in new Setup {
      dateAsMonth(date) mustBe "July"
    }
  }

  "dateAsMonthAndYear" should {
    "return month and year as a valid value" in new Setup {
      dateAsMonthAndYear(date) mustBe "July 2020"
    }
  }

  "parseDateString" should {
    "return correct date object for valid date string" in new Setup {
      parseDateString("15 January 2021") mustBe LocalDate.of(YEAR_2021, MONTH_1, DAY_15)
      parseDateString("15 February 2021") mustBe LocalDate.of(YEAR_2021, MONTH_2, DAY_15)
      parseDateString("15 March 2021") mustBe LocalDate.of(YEAR_2021, MONTH_3, DAY_15)
      parseDateString("15 April 2021") mustBe LocalDate.of(YEAR_2021, MONTH_4, DAY_15)
      parseDateString("10 May 2021") mustBe LocalDate.of(YEAR_2021, MONTH_5, DAY_10)
      parseDateString("15 June 2021") mustBe LocalDate.of(YEAR_2021, MONTH_6, DAY_15)
      parseDateString("15 July 2021") mustBe LocalDate.of(YEAR_2021, MONTH_7, DAY_15)
      parseDateString("10 August 2021") mustBe LocalDate.of(YEAR_2021, MONTH_8, DAY_10)
      parseDateString("15 September 2021") mustBe LocalDate.of(YEAR_2021, MONTH_9, DAY_15)
      parseDateString("15 October 2021") mustBe LocalDate.of(YEAR_2021, MONTH_10, DAY_15)
      parseDateString("10 November 2021") mustBe LocalDate.of(YEAR_2021, MONTH_11, DAY_10)
      parseDateString("15 December 2021") mustBe LocalDate.of(YEAR_2021, MONTH_12, DAY_15)

      parseDateString("1 Ionawr 2021") mustBe LocalDate.of(YEAR_2021, MONTH_1, DAY_1)
      parseDateString("15 Chwefror 2021") mustBe LocalDate.of(YEAR_2021, MONTH_2, DAY_15)
      parseDateString("15 Mawrth 2021") mustBe LocalDate.of(YEAR_2021, MONTH_3, DAY_15)
      parseDateString("15 Ebrill 2021") mustBe LocalDate.of(YEAR_2021, MONTH_4, DAY_15)
      parseDateString("15 Mai 2021") mustBe LocalDate.of(YEAR_2021, MONTH_5, DAY_15)
      parseDateString("15 Mehefin 2021") mustBe LocalDate.of(YEAR_2021, MONTH_6, DAY_15)
      parseDateString("15 Gorffennaf 2021") mustBe LocalDate.of(YEAR_2021, MONTH_7, DAY_15)
      parseDateString("15 Awst 2021") mustBe LocalDate.of(YEAR_2021, MONTH_8, DAY_15)
      parseDateString("15 Medi 2021") mustBe LocalDate.of(YEAR_2021, MONTH_9, DAY_15)
      parseDateString("15 Hydref 2021") mustBe LocalDate.of(YEAR_2021, MONTH_10, DAY_15)
      parseDateString("15 Tachwedd 2021") mustBe LocalDate.of(YEAR_2021, MONTH_11, DAY_15)
      parseDateString("15 Rhagfyr 2021") mustBe LocalDate.of(YEAR_2021, MONTH_12, DAY_15)
    }

    "throw exception for the invalid date string" in new Setup {
      intercept[RuntimeException] {
        parseDateString("2021 December 20")
      }

      intercept[RuntimeException] {
        parseDateString("15 December2021")
      }
    }
  }

  trait Setup {
    val app: Application = application.build()
    implicit val msg: Messages = messages(app)
    val date: LocalDate = LocalDate.parse("2020-07-21")
  }
}
