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

package viewmodels.pagination

import utils.SpecBase
import utils.TestData.{
  PAGE_1, PAGE_2, PAGE_3, PAGE_10, PAGE_101, PAGE_11, PAGE_150, PAGE_20, PAGE_21, PAGE_30, PAGE_4,
  PAGE_40, PAGE_100, PAGE_400, PAGE_5, PAGE_50, PAGE_6, PAGE_8
}

class MetaDataSpec extends SpecBase {

  "apply" should {

    "populate correct totalPages" when {

      "totalNumberOfMovements is not a whole number" in {

        MetaData(PAGE_3, PAGE_2, PAGE_1).totalPages mustBe 2
        MetaData(PAGE_5, PAGE_2, PAGE_1).totalPages mustBe 3
        MetaData(PAGE_10, PAGE_3, PAGE_1).totalPages mustBe 4
      }

      "totalNumberOfMovements is a whole number" in {

        MetaData(PAGE_2, PAGE_1, PAGE_1).totalPages mustBe 2
        MetaData(PAGE_6, PAGE_2, PAGE_1).totalPages mustBe 3
        MetaData(PAGE_8, PAGE_2, PAGE_1).totalPages mustBe 4
      }
    }

    "return numberOfMovementsPerPage value" when {
      "current page is 1" in {

        MetaData(PAGE_20, PAGE_10, PAGE_1).to mustBe 10
        MetaData(PAGE_40, PAGE_20, PAGE_1).to mustBe 20
        MetaData(PAGE_100, PAGE_50, PAGE_1).to mustBe 50
      }
    }

    "return totalNumberOfMovements' value" when {
      "number of movement times by the current page number is greater than the total number of movements" in {

        MetaData(PAGE_11, PAGE_10, PAGE_2).to mustBe 11
        MetaData(PAGE_21, PAGE_10, PAGE_3).to mustBe 21
        MetaData(PAGE_101, PAGE_50, PAGE_3).to mustBe 101
      }
    }

    "return from value as 1" when {
      "current page is 1" in {

        MetaData(PAGE_2, PAGE_1, PAGE_1).from mustBe 1
      }
    }

    "return number of movements per page + 1" when {
      "current page is 2" in {

        MetaData(PAGE_30, PAGE_10, PAGE_2).from mustBe 11
        MetaData(PAGE_5, PAGE_2, PAGE_2).from mustBe 3
        MetaData(PAGE_150, PAGE_50, PAGE_2).from mustBe 51
      }
    }

    "return (number of movements per page times by (current page minus 1)) plus 1" when {
      "on page other than 1 and 2" in {

        MetaData(PAGE_30, PAGE_10, PAGE_3).from mustBe 21
        MetaData(PAGE_10, PAGE_2, PAGE_4).from mustBe 7
        MetaData(PAGE_400, PAGE_50, PAGE_5).from mustBe 201
      }
    }

  }
}
