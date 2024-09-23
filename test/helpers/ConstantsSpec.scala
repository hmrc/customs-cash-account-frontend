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

import utils.SpecBase
import helpers.Constants._

class ConstantsSpec extends SpecBase {

  "ConstantsSpec" should {
    "REQUEST_COULD_NOT_BE_PROCESSED is 003" in new Setup {
      helpers.Constants.REQUEST_COULD_NOT_BE_PROCESSED mustBe three
    }

    "DUPLICATE_SUBMISSION is 004" in new Setup {
      helpers.Constants.DUPLICATE_SUBMISSION mustBe four
    }

    "ACCOUNT_DOES_NOT_EXIST is 92" in new Setup {
      helpers.Constants.ACCOUNT_DOES_NOT_EXIST mustBe ninetyTwo
    }

    "INVALID_EORI is 102" in new Setup {
      helpers.Constants.INVALID_EORI mustBe hundredTwo
    }

    "ENTRY_ALREADY_EXISTS is 124" in new Setup {
      helpers.Constants.ENTRY_ALREADY_EXISTS mustBe hundredTwentyFour
    }

    "EXCEEDED_MAXIMUM is 602" in new Setup {
      helpers.Constants.EXCEEDED_MAXIMUM mustBe sixHundredTwo
    }
  }

  trait Setup {
    val three = "003"
    val four = "004"
    val ninetyTwo = "092"
    val hundredTwo = "102"
    val hundredTwentyFour = "124"
    val sixHundredTwo = "602"
  }
}
