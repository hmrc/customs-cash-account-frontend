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
      helpers.Constants.REQUEST_COULD_NOT_BE_PROCESSED mustBe request
    }

    "DUPLICATE_SUBMISSION is 004" in new Setup {
      helpers.Constants.DUPLICATE_SUBMISSION mustBe duplicate
    }

    "ACCOUNT_DOES_NOT_EXIST is 92" in new Setup {
      helpers.Constants.ACCOUNT_DOES_NOT_EXIST mustBe account
    }

    "INVALID_EORI is 102" in new Setup {
      helpers.Constants.INVALID_EORI mustBe invalid
    }

    "ENTRY_ALREADY_EXISTS is 124" in new Setup {
      helpers.Constants.ENTRY_ALREADY_EXISTS mustBe entry
    }

    "EXCEEDED_MAXIMUM is 602" in new Setup {
      helpers.Constants.EXCEEDED_MAXIMUM mustBe exceeded
    }
  }

  trait Setup {
    val request: String = "003-Request could not be processed"
    val duplicate: String = "004-Duplicate submission acknowledgment reference"
    val account: String = "092-The account does not exist within ETMP"
    val invalid: String = "102-Invalid EORI number"
    val entry: String = "124-Entry already exists for the same period"
    val exceeded: String = "602-Exceeded maximum threshold of transactions"
  }
}
