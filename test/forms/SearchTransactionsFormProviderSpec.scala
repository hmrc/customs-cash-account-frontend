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

package forms

import play.api.data.FormError
import utils.SpecBase

class SearchTransactionsFormProviderSpec extends SpecBase {

  "apply" should {

    "produce error if no value is provided" in {
      val form = new SearchTransactionsFormProvider()()

      val data = Map("value" -> emptyString)

      form.bind(data).errors mustBe Seq(FormError("value", List("cf.search.form.error.required"), List()))
    }

    "produce no error if some value is provided" in {
      val form = new SearchTransactionsFormProvider()()

      val inputValue = "GAGG1126910LP531340"
      val data       = Map("value" -> inputValue)

      form.bind(data).get mustBe inputValue
    }
  }
}
