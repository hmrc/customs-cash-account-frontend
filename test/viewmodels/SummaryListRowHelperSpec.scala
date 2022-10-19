/*
 * Copyright 2022 HM Revenue & Customs
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

import utils.SpecBase
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions

class SummaryListRowHelperSpec extends SpecBase with SummaryListRowHelper {

  "summaryListRow" should {
    "correctly return a summary list row" in {
      val result = summaryListRow("something", Some("something"), Actions())
      result.actions mustBe Some(Actions())
      result.value mustBe Value(HtmlContent("something"))
      result.secondValue mustBe Some(Value(HtmlContent("something"), classes = ""))
    }
  }
}