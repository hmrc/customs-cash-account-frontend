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

import models.{CashDailyStatement, CashTransactions, Declaration, Payment, Transaction, Transfer, Withdrawal}
import play.api.i18n.Messages
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.components.daily_statements_v2
import viewmodels.CashAccountDailyStatementsViewModel
import utils.TestData.*
import views.ViewTestHelper

import java.time.LocalDate

class DailyStatementsV2Spec extends ViewTestHelper {

  "component" should {

    "display correct contents" when {

      "there are no transaction to display" in new Setup {
        val view: Document = componentView(CashAccountDailyStatementsViewModel(CashTransactions(Seq(), Seq()), None))

        shouldDisplayTransForLastSixMonthsHeading(view)
        shouldDisplayNoTransactionsFromLastSixMonthsMessage(view)
        shouldNotDisplayTable(view)
      }

      "transactions are available to display" in new Setup {
        val view: Document = componentView(CashAccountDailyStatementsViewModel(cashTransactions, None))

        shouldDisplayTransForLastSixMonthsHeading(view)
        shouldNotDisplayNoTransactionsFromLastSixMonthsMessage(view)
        shouldDisplayTableHeaders(view)
        shouldDisplayTableElements(view)
      }
    }
  }

  private def shouldDisplayTransForLastSixMonthsHeading(viewDocument: Document)(implicit msgs: Messages) =
    viewDocument.getElementById("transactions-for-last-six-months-heading").text() mustBe
      msgs("cf.cash-account.transactions.transactions-for-last-six-months.heading")

  private def shouldDisplayNoTransactionsFromLastSixMonthsMessage(viewDocument: Document)(implicit msgs: Messages) =
    viewDocument.getElementById("no-transactions-for-last-six-months-text").text() mustBe
      msgs("cf.cash-account.transactions.no-transactions-for-last-six-months")

  private def shouldNotDisplayNoTransactionsFromLastSixMonthsMessage(viewDocument: Document) =
    Option(viewDocument.getElementById("no-transactions-for-last-six-months-text")) mustBe empty

  private def shouldDisplayTableHeaders(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.html().contains(msgs("cf.cash-account.detail.date")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.transaction-type")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.credit")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.debit")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.balance")) mustBe true
  }

  private def shouldDisplayTableElements(viewDocument: Document) = {
    val tableRows = viewDocument.getElementsByClass("govuk-table__row")

    val index_4  = 4
    val index_5  = 5
    val index_6  = 6
    val index_7  = 7
    val index_8  = 8
    val index_9  = 9
    val index_10 = 10

    val row1  = tableRows.get(1).html()
    val row2  = tableRows.get(2).html()
    val row3  = tableRows.get(3).html()
    val row4  = tableRows.get(index_4).html()
    val row5  = tableRows.get(index_5).html()
    val row6  = tableRows.get(index_6).html()
    val row7  = tableRows.get(index_7).html()
    val row8  = tableRows.get(index_8).html()
    val row9  = tableRows.get(index_9).html()
    val row10 = tableRows.get(index_10).html()

    tableRows.size() mustBe 11

    row1  must include("20 July 2020")
    row2  must include("20 July 2020")
    row3  must include("20 July 2020")
    row4  must include("20 July 2020")
    row5  must include("20 July 2020")
    row6  must include("18 July 2020")
    row7  must include("18 July 2020")
    row8  must include("18 July 2020")
    row9  must include("18 July 2020")
    row10 must include("18 July 2020")
  }

  private def shouldNotDisplayTableHeaders(viewDocument: Document) = {
    Option(viewDocument.getElementById("transaction-date")) mustBe empty
    Option(viewDocument.getElementById("transaction-type")) mustBe empty
    Option(viewDocument.getElementById("transaction-credit")) mustBe empty
    Option(viewDocument.getElementById("transaction-debit")) mustBe empty
    Option(viewDocument.getElementById("transaction-balance")) mustBe empty
  }

  private def shouldNotDisplayTable(viewDocument: Document) = {
    shouldNotDisplayTableHeaders(viewDocument)
    val tableRosElementsByClass = viewDocument.getElementsByClass("hmrc-responsive-table__heading")

    tableRosElementsByClass.size() mustBe 0
  }

  trait Setup {
    val declaration1: Declaration =
      Declaration(
        MOVEMENT_REF_NUMBER,
        Some(EORI_NUMBER),
        EORI_NUMBER,
        Some(DECLARANT_REF),
        DATE,
        AMOUNT,
        Seq(TAX_GROUP),
        Some(SECURE_MOVEMENT_REF_NUMBER)
      )

    val declaration2: Declaration =
      Declaration(
        MOVEMENT_REF_NUMBER,
        Some(EORI_NUMBER),
        EORI_NUMBER,
        Some(DECLARANT_REF),
        DATE_1,
        AMOUNT,
        Seq(TAX_GROUP),
        Some(SECURE_MOVEMENT_REF_NUMBER)
      )

    val pendingTransactions: Seq[Declaration] = Seq(declaration1, declaration2)

    val date1: LocalDate = LocalDate.parse("2020-07-18")
    val date2: LocalDate = LocalDate.parse("2020-07-20")

    val declaration3: Declaration = declaration1.copy(date = date1)
    val declaration4: Declaration = declaration2.copy(date = date1)
    val declaration5: Declaration = declaration1.copy(date = date2)
    val declaration6: Declaration = declaration2.copy(date = date2)

    private val otherTransactions =
      Seq(Transaction(123.45, Payment, None), Transaction(-432.87, Withdrawal, Some("77665544")))

    private val otherTransactions1 =
      Seq(Transaction(123.45, Payment, None), Transaction(-432.87, Transfer, Some("77665544")))

    val dailyStatement1: CashDailyStatement =
      CashDailyStatement(date1, 500.0, 1000.00, Seq(declaration3, declaration4), otherTransactions)

    val dailyStatement2: CashDailyStatement =
      CashDailyStatement(date2, 600.0, 1200.00, Seq(declaration5, declaration6), otherTransactions1)

    val dailyStatements: Seq[CashDailyStatement] = Seq(dailyStatement1, dailyStatement2)

    val cashTransactions: CashTransactions = CashTransactions(pendingTransactions, dailyStatements)

    def componentView(model: CashAccountDailyStatementsViewModel): Document =
      Jsoup.parse(app.injector.instanceOf[daily_statements_v2].apply(model).body)
  }
}
