/*
 * Copyright 2024 HM Revenue & Customs
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

package forms

import models.JamieFormFields
import play.api.data.FormError
import utils.SpecBase
import utils.RegexPatterns.noDigitsRegex

import scala.collection.immutable.ArraySeq

class JamieFormProviderSpec extends SpecBase {
    "apply" should {
      "produce an error is no value is input" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> emptyString, "age" -> emptyString)

        form.bind(data).errors must contain (FormError("name", "error.required"))
        form.bind(data).errors must contain (FormError("age", "error.number"))
      }

      "produce an error when a name is entered but no age" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "Jamie", "age" -> emptyString)

        form.bind(data).errors mustBe Seq(FormError("age", List("error.number")))
      }

      "produce an error when a age is entered but no name" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> emptyString, "age" -> "30")

        form.bind(data).errors mustBe Seq(FormError("name", List("error.required")))
      }

      "produce an error when both values are numbers" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "28", "age" -> "38")

        form.bind(data).errors mustBe Seq(
          FormError("name", List("Enter your name"), ArraySeq(noDigitsRegex.regex)))
      }

      "produce an error when name is correct but number out of required range" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "Jamie", "age" -> "214")

        form.bind(data).errors mustBe Seq(FormError("age", List("Enter a number between 1 - 120")))
      }

      "produce an error when name has a number in it" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "J4mie", "age" -> "22")

        form.bind(data).errors mustBe Seq(
          FormError("name", List("Enter your name"), ArraySeq(noDigitsRegex.regex)))
      }

      "produce an error when both values are a lettered strings" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "Jamie", "age" -> "Two")

        form.bind(data).errors mustBe Seq(FormError("age", List("error.number")))
      }

      "produce no error when both values are entered" in {
        val form = new JamieFormProvider()()

        val testName = "Jamie"
        val testAge = 28
        val data = Map("name" -> testName, "age" -> testAge.toString)

        form.bind(data).get == JamieFormFields(testName, testAge)
      }
    }
}
