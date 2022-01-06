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

package views.components

import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object TransactionRow {

  def apply(transactionSpan: String,
            transactionMessage: String,
            amountSpan: String,
            amount: String): Seq[TableRow] = {
    Seq(
      TableRow(
        classes = "govuk-table__cell govuk-!-font-weight-regular",
        content = HtmlContent(
          s"""
           <span class="hmrc-responsive-table__heading" aria-hidden="true">$transactionSpan</span>
           $transactionMessage
           """
        )),
      TableRow(
        classes = "govuk-table__cell--numeric amount",
        content = HtmlContent(
          s"""
           <span class="hmrc-responsive-table__heading amount" aria-hidden="true">$amountSpan</span>
           $amount
           """
        ))
    )
  }

}


