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

class FormattersSpec extends SpecBase {
  "Formatters" must {

    "fileSizeFormat size of 1 returns 1KB" in {
      val size: Long = 1
      val result = Formatters.fileSizeFormat(size)
      result mustBe "1KB"
    }

    "fileSizeFormat size of 1000000000 returns 1000.0MB" in {
      val size: Long = 1000000000
      val result = Formatters.fileSizeFormat(size)
      result mustBe "1000.0MB"
    }

    "fileSizeFormat size of 1000 returns " in {
      val size: Long = 100000
      val result = Formatters.fileSizeFormat(size)
      result mustBe "100KB"
    }
  }
}
