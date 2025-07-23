/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.data.{FieldMapping, Form}
import utils.SpecBase
import utils.TestData.{DAY_1, MONTH_10, MONTH_2, YEAR_2020, YEAR_2023}

import java.time.LocalDate

class SelectMappingsSpec extends SpecBase {

  object TestMappings extends SelectMappings {
    def booleanMapping: FieldMapping[Boolean]     = boolean()
    def textMapping: FieldMapping[String]         = text()
    def localDateMapping: FieldMapping[LocalDate] = localDate(
      "error.startMonth",
      "error.startYear",
      "error.endMonth",
      "error.endYear",
      "error.startDate",
      "error.endDate",
      "error.invalidMonth",
      "error.invalidYear",
      "error.invalidDate"
    )
  }

  "boolean mapping" should {
    "bind true when input is 'true'" in {
      val form = Form("value" -> TestMappings.booleanMapping)
      form.bind(Map("value" -> "true")).value shouldBe Some(true)
    }

    "bind false when input is 'false'" in {
      val form = Form("value" -> TestMappings.booleanMapping)
      form.bind(Map("value" -> "false")).value shouldBe Some(false)
    }

    "return error for invalid boolean string" in {
      val form   = Form("value" -> TestMappings.booleanMapping)
      val result = form.bind(Map("value" -> "notabool"))
      result.errors                should have length 1
      result.errors.head.message shouldBe "error.boolean"
    }

    "return error when field is missing" in {
      val form   = Form("value" -> TestMappings.booleanMapping)
      val result = form.bind(Map.empty[String, String])
      result.errors.head.message shouldBe "error.required"
    }
  }

  "text mapping" should {
    "bind valid string" in {
      val form = Form("value" -> TestMappings.textMapping)
      form.bind(Map("value" -> "hello")).value shouldBe Some("hello")
    }

    "return error when field is empty" in {
      val form   = Form("value" -> TestMappings.booleanMapping)
      val result = form.bind(Map("value" -> ""))
      result.errors.head.message shouldBe "error.required"
    }

    "return error when field is missing" in {
      val form   = Form("value" -> TestMappings.booleanMapping)
      val result = form.bind(Map.empty)
      result.errors.head.message shouldBe "error.required"
    }
  }

  "localDate mapping" should {
    "bind a valid date" in {
      val form = Form("date" -> TestMappings.localDateMapping)

      val result = form.bind(
        Map(
          "date.day"   -> "01",
          "date.month" -> "10",
          "date.year"  -> "2023"
        )
      )
      result.value shouldBe Some(LocalDate.of(YEAR_2023, MONTH_10, DAY_1))
    }

    "return error for invalid date input" in {
      val form = Form("date" -> TestMappings.localDateMapping)

      val result = form.bind(
        Map(
          "date.day"   -> "31",
          "date.month" -> "2",
          "date.year"  -> "2020"
        )
      )

      result.value shouldBe Some(LocalDate.of(YEAR_2020, MONTH_2, DAY_1))
    }

    "return error for missing fields" in {
      val form = Form("date" -> TestMappings.localDateMapping)

      val result = form.bind(Map.empty)

      result.errors                should not be empty
      result.errors.map(_.message) should contain("Unknown")
    }
  }
}
