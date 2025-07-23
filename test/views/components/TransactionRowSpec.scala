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

package views.components

import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import utils.SpecBase

class TransactionRowSpec extends SpecBase {

  "TransactionRow" should {
    "return a sequence of two correctly constructed TableRow instances" in {
      val transactionSpan    = "Date"
      val transactionMessage = "01 Jan 2025"
      val amountSpan         = "Amount"
      val amount             = "£123.45"

      val result: Seq[TableRow] = TransactionRow(transactionSpan, transactionMessage, amountSpan, amount)

      result should have size 2

      val firstRow = result.head
      firstRow.classes           shouldBe "govuk-table__cell govuk-!-font-weight-regular"
      firstRow.content.asHtml.body should include(
        s"""<span class="hmrc-responsive-table__heading" aria-hidden="true">$transactionSpan</span>"""
      )
      firstRow.content.asHtml.body should include(transactionMessage)

      val secondRow = result(1)
      secondRow.classes           shouldBe "govuk-table__cell--numeric amount"
      secondRow.content.asHtml.body should include(
        s"""<span class="hmrc-responsive-table__heading amount" aria-hidden="true">$amountSpan</span>"""
      )
      secondRow.content.asHtml.body should include(amount)
    }
  }
}
