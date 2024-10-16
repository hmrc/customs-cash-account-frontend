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

package utils

import utils.RegexPatterns.{mrnRegex, paymentRegex, ucrRegex}

class RegexPatternsSpec extends SpecBase {

  "MRN Regex" should {

    "match valid regex" in {
      val validInputs: Seq[String] = Seq(
        "ABCD123456789012",
        "ABCD12345678901234",
        "ABCD1234567890123456")

      validInputs.foreach { input =>
        mrnRegex.findFirstIn(input) mustBe Some(input)
      }
    }

    "not match invalid regex" in {
      val invalidInputs: Seq[String] = Seq(
        "AB12",
        "ABCD123456789012345678",
        "GHRT122317@DC239177")

      invalidInputs.foreach { input =>
        mrnRegex.findFirstIn(input) mustBe None
      }
    }
  }

  "Payment Regex" should {

    "match valid payment amounts" in {
      val validInputs: Seq[String] = Seq(
        "£123",
        "£123.45",
        "-£123",
        "-£123.45",
        "£1234",
        "£1234.56",
        "£12345",
        "£12345.67")

      validInputs.foreach { input =>
        paymentRegex.findFirstIn(input) mustBe Some(input)
      }
    }

    "not match invalid payment amounts" in {
      val invalidInputs: Seq[String] = Seq(
        "123",
        "-123",
        "£123.4",
        "£123.456",
        "-£123.456")

      invalidInputs.foreach { input =>
        paymentRegex.findFirstIn(input) mustBe None
      }
    }
  }

  "UCR Regex" should {

    "match valid regex" in {
      val validInputs: Seq[String] = Seq("GB1234567890 1134-7456-914 121D")

      validInputs.foreach { input =>
        ucrRegex.findFirstIn(input) mustBe Some(input)
      }
    }

    "not match invalid regex" in {
      val invalidInputs: Seq[String] = Seq(
        "GB1234567890-1134!7456_914 121D",
        "GB1234567890 1134-7456-914 121D 3031D",
        "123GB4567440 5734 3356 914 81D")

      invalidInputs.foreach { input =>
        ucrRegex.findFirstIn(input) mustBe None
      }
    }
  }
}
