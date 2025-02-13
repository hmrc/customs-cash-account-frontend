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

package views.components

import org.scalatest.matchers.should.Matchers.should
import uk.gov.hmrc.govukfrontend.views.Aliases.HeadCell
import utils.SpecBase

class PaymentSearchResultHeaderV2Spec extends SpecBase {

  "apply" should {

    "produce correct output" in {

      val header: Seq[HeadCell] = PaymentSearchResultHeader()

      header.size mustBe 4
      header.head.content.toString.contains("Date") mustBe true
      header(1).content.toString.contains("Transaction type") mustBe true
      header(2).content.toString.contains("Credit") mustBe true
      header.last.content.toString.contains("Debit") mustBe true
      header.last.format.get.contains("numeric") mustBe true
    }
  }

}
