/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels

import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, HtmlContent, Value}
import utils.SpecBase

class SummaryListRowSpec extends SpecBase {
  "SummaryListRow" should {

    "construct properly with all fields provided" in {
      val value1  = Value(content = HtmlContent("Value 1"))
      val value2  = Value(content = HtmlContent("Value 2"))
      val actions = Actions(items = Seq.empty)
      val row     = SummaryListRow(
        value = value1,
        secondValue = Some(value2),
        classes = "some-css-class",
        actions = Some(actions)
      )

      row.value       shouldBe value1
      row.secondValue shouldBe Some(value2)
      row.classes     shouldBe "some-css-class"
      row.actions     shouldBe Some(actions)
    }

    "construct properly when optional fields are not provided" in {
      val value1 = Value(content = HtmlContent("Only Value"))
      val row    = SummaryListRow(
        value = value1,
        secondValue = None,
        classes = "single-value"
      )

      row.value       shouldBe value1
      row.secondValue shouldBe None
      row.classes     shouldBe "single-value"
      row.actions     shouldBe None
    }
  }
}
