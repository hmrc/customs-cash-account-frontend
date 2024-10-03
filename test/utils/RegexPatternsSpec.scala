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

class RegexPatternsSpec extends SpecBase {

  "MRN/UCR Regex" should {

    "match valid regex" in {
      val validInputs: Seq[String] = Seq(
        "AB123456",
        "ABCD1234567890",
        "AB123456AB123456",
        "AB123456 1234-5678-123 456A",
        "ABC123456 1234-5678-123 456A")

      validInputs.foreach { input =>
        RegexPatterns.mrnUCRRegex.findFirstIn(input) mustBe Some(input)
      }
    }

    "not a match invalid regex" in {
      val invalidInputs: Seq[String] = Seq(
        "A123456",
        "ABCDE123456",
        "AB12345",
        "AB123456ABC1234567",
        "AB123456 1234-5678-123-456A")

      invalidInputs.foreach { input =>
        RegexPatterns.mrnUCRRegex.findFirstIn(input) mustBe None
      }
    }
  }

  "Super MRN/UCR Regex" should {

    "match valid regex" in {
      val validInputs: Seq[String] = Seq(
        "12AB123456789012",
        "12AB12345678901234",
        "12AB1234567890123456")

      validInputs.foreach { input =>
        RegexPatterns.superMRNUCRRegex.findFirstIn(input) mustBe Some(input)
      }
    }

    "not match invalid regex" in {
      val invalidInputs: Seq[String] = Seq(
        "123AB123456789012",
        "12AB123456")

      invalidInputs.foreach { input =>
        RegexPatterns.superMRNUCRRegex.findFirstIn(input) mustBe None
      }
    }
  }

  "Payment Regex" should {

    "match valid payment amounts" in {
      val validInputs: Seq[String] = Seq(
        "£123",
        "£123.45",
        "-£123",
        "-£123.45")

      validInputs.foreach { input =>
        RegexPatterns.paymentRegex.findFirstIn(input) mustBe Some(input)
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
        RegexPatterns.paymentRegex.findFirstIn(input) mustBe None
      }
    }
  }
}
