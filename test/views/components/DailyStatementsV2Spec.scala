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
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.components.daily_statements_v2
import viewmodels.CashAccountDailyStatementsViewModel
import utils.TestData.*

import java.time.LocalDate

class DailyStatementsV2Spec extends SpecBase {

  "component" should {

    "display correct contents" when {

      "there are no transaction to display" in new Setup {
        val view: Document = componentView(CashAccountDailyStatementsViewModel(CashTransactions(Seq(), Seq())))

        shouldNotDisplayTable(view)
      }

      "transactions are available to display" in new Setup {
        val view: Document = componentView(CashAccountDailyStatementsViewModel(cashTransactions))

        shouldDisplayTableHeaders(view)
        shouldDisplayTableElements(view)
      }
    }
  }

  private def shouldDisplayTableHeaders(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.getElementById("transaction-date").text() mustBe msgs("cf.cash-account.detail.date")
    viewDocument.getElementById("transaction-type").text() mustBe msgs("cf.cash-account.detail.transaction-type")
    viewDocument.getElementById("transaction-credit").text() mustBe msgs("cf.cash-account.detail.credit")
    viewDocument.getElementById("transaction-debit").text() mustBe msgs("cf.cash-account.detail.debit")
    viewDocument.getElementById("transaction-balance").text() mustBe msgs("cf.cash-account.detail.balance")
  }

  private def shouldDisplayTableElements(viewDocument: Document)(implicit msgs: Messages) = {
    val tableRosElementsByClass = viewDocument.getElementsByClass("hmrc-responsive-table__heading")

    tableRosElementsByClass.size() must be > 0
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
    val app: Application = application.build()
    implicit val msgs: Messages = messages(app)

    val declaration1: Declaration =
      Declaration(MOVEMENT_REF_NUMBER, Some(EORI_NUMBER), EORI_NUMBER, Some(DECLARANT_REF), DATE, AMOUNT,
        Seq(TAX_GROUP), Some(SECURE_MOVEMENT_REF_NUMBER))

    val declaration2: Declaration =
      Declaration(MOVEMENT_REF_NUMBER, Some(EORI_NUMBER), EORI_NUMBER, Some(DECLARANT_REF), DATE_1, AMOUNT,
        Seq(TAX_GROUP), Some(SECURE_MOVEMENT_REF_NUMBER))

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
