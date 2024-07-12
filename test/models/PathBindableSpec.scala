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

package models

import utils.SpecBase

class PathBindableSpec extends SpecBase {

  "optionBindable" must {

    "bind a right path param to optional string" in {
      val result = domain.optionBindable.bind("key", "value")

      result.map { result => result mustBe Some("value") }
    }

    "bind a left path param to optional string" in {
      val result = domain.optionBindable.bind("key", "value")

      result.left.map { result => result mustBe "value" }
    }

    "unbind an optional string value to path param" in {
      val result = domain.optionBindable.unbind("key", Some("value"))

      result mustBe "value"
    }
  }
}
