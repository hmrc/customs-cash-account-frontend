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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, HtmlContent}

object PaymentHeader {

  def apply()(implicit messages: Messages): Seq[HeadCell] = {

    Seq(
      HeadCell(
        classes = "first-column-width",
        content = HtmlContent(
          s"""
                    <abbr title="${messages("cf.cash-account.detail.movement-reference-number")}">
                    ${messages("cf.cash-account.detail.Declarationmrn")}
                    </abbr>
                    """)
      ),
      HeadCell(
        classes = "second-column-width",
        content = HtmlContent(
          s"""
                    <abbr title="${messages("cf.cash-account.detail.unique-consignment-reference")}">
                    ${messages("cf.cash-account.detail.Declarationucr")}
                    </abbr>
                    """)
      ),
      HeadCell(
        content = HtmlContent(
          s"""
                  ${messages("cf.cash-account.detail.declarant")}
                  <abbr title="${messages("cf.cash-account.detail.eori-definition")}">
                  ${messages("cf.cash-account.detail.eori")}
                  </abbr>
                  """)
      ),
      HeadCell(
        format = Some("numeric"),
        content = HtmlContent(
          s"""
                    ${messages("cf.cash-account.detail.amount")}
                    """)
      ),
    )
  }
}
