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

import play.api.i18n.Messages
import play.api.test.Helpers
import utils.SpecBase

class DateFormatSpec extends SpecBase {

  implicit val messages: Messages = Helpers.stubMessages()

  "CurrencyFormatters.formatCurrencyAmount" should {

    "format a number to the given number of decimals" in {
      Formatters.formatCurrencyAmount(amount = 999.6565) must be ("£999.66")
    }

    "include trailing zero if there is any significant decimal place" in {
      Formatters.formatCurrencyAmount(amount = 999.1) must be ("£999.10")
    }

    "include zero decimals if explicitly requested" in {
      Formatters.formatCurrencyAmount( amount = 999.00) must be ("£999.00")
    }

    "prefix the amount with the currency symbol for the requested locale" in {
      Formatters.formatCurrencyAmount(amount = 999.6565) must be ("£999.66")
    }

    "include grouping separator where requested" in {
      Formatters.formatCurrencyAmount(amount = 9999999.99) must be ("£9,999,999.99")
    }

    "by default" should {
      "format to 2 decimal places, with grouping, for the UK locale" in {
        Formatters.formatCurrencyAmount(amount = 1999.6565) must be ("£1,999.66")
      }
    }
  }
}
