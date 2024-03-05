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

import play.api.data.FormError
import utils.SpecBase

class FormattersSpec extends SpecBase {

  "booleanFormatter" should {
    "return correct value" when {
      "key is bound correctly" in {

        val formatterOb = new Formatters {}

        formatterOb.booleanFormatter("test_key", "invalid_key").bind(
          "test_key", Map("test_key" -> "true")) mustBe Right(true)

        formatterOb.booleanFormatter("test_key", "invalid_key").bind(
          "test_key", Map("test_key" -> "false")) mustBe Right(false)
      }
    }

    "return error when invalid key is found" in {

      val formatterOb = new Formatters {}
      val key = "test_key"
      val invalidKey = "invalid_key"

      formatterOb.booleanFormatter(key, invalidKey).bind(
        "test_key", Map(key -> "invalid_value")) mustBe Left(Seq(FormError(key, invalidKey)))
    }
  }

  "decimalFormatter" should {
    "return correct value when key is bound" in {

      val formatterOb = new Formatters {}
      val key = "test_key"
      val invalidKey = "invalid_key"

      formatterOb.decimalFormatter(key, invalidKey).bind(
        key, Map(key -> "99.0")) mustBe Right("99.0")
    }

    "return error when key is not bound successfully" in {

      val formatterOb = new Formatters {}
      val key = "test_key"
      val invalidKey = "invalid_key"

      formatterOb.decimalFormatter(key, invalidKey).bind(
        key, Map(key -> "invalid")) mustBe Left(Seq(FormError(key, invalidKey)))
    }
  }
}
