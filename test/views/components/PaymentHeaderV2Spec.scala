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

import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, HtmlContent}
import utils.SpecBase

class PaymentHeaderV2Spec extends SpecBase {

  "apply" should {

    "produce correct output" in new Setup {

      val headerOb: Seq[HeadCell] = PaymentHeaderV2()

      headerOb.size mustBe 5
      headerOb must be(an[Seq[HeadCell]])
    }
  }

  trait Setup {
    protected def expectedHeaderCells: Seq[HeadCell] =
      Seq(
        HeadCell(
          classes = "first-column-width",
          content = HtmlContent(s"""
                <abbr title="${messages("cf.cash-account.detail.date")}">
                    ${messages("cf.cash-account.detail.date")}
                </abbr>
                """)
        ),
        HeadCell(
          classes = "second-column-width",
          content = HtmlContent(s"""
                <abbr title="${messages("cf.cash-account.detail.transaction-type")}">
                ${messages("cf.cash-account.detail.transaction-type")}
                </abbr>
                """)
        ),
        HeadCell(
          content = HtmlContent(s"""
                <abbr title="${messages("cf.cash-account.detail.credit")}">
                ${messages("cf.cash-account.detail.credit")}
                </abbr>
                """)
        ),
        HeadCell(
          format = Some("numeric"),
          content = HtmlContent(s"""
                <abbr title="${messages("cf.cash-account.detail.debit")}">
                ${messages("cf.cash-account.detail.debit")}
                </abbr>
                """)
        ),
        HeadCell(
          format = Some("numeric"),
          content = HtmlContent(s"""
                      ${messages("cf.cash-account.detail.balance")}
                      """)
        )
      )
  }

}
