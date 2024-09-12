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

package views

import config.AppConfig
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import html.cash_account_v2
import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CashDailyStatement, CashTransactions, Declaration, Payment, Transaction, Withdrawal}
import viewmodels.CashAccountViewModelV2
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestData.*

import java.time.LocalDate

class CashAccountV2Spec extends ViewTestHelper {

  "view" should {

    "display correct contents" when {

      "there are no transactions" in new Setup {
        val view: Document = createView(viewModelWithNoTransactions)

        titleShouldBeCorrect(view, "cf.cash-account.detail.title")
        shouldContainBackLinkUrl(view, appConfig.customsFinancialsFrontendHomepage)
        shouldContainCorrectAccountBalanceDetails(view, can)
        //shouldContainCorrectSearchForTransactionsInputTextDetails(view)
        shouldNotContainCashAccountDailyStatements(view)
        shouldContainCorrectRequestTransactionsHeading(view)
        shouldContainCorrectDownloadCSVFileLinkUrl(view)
      }

      "transactions are present in the model" in new Setup {
        val view: Document = createView(viewModelWithTransactions)

        titleShouldBeCorrect(view, "cf.cash-account.detail.title")
        shouldContainBackLinkUrl(view, appConfig.customsFinancialsFrontendHomepage)
        shouldContainCorrectAccountBalanceDetails(view, can)
        //shouldContainCorrectSearchForTransactionsInputTextDetails(view)
        shouldContainCashAccountDailyStatements(view)
        shouldContainCorrectRequestTransactionsHeading(view)
        shouldContainCorrectDownloadCSVFileLinkUrl(view)
      }
    }
  }

  private def shouldContainCorrectAccountBalanceDetails(viewDocument: Document,
                                                        accNumber: String)
                                                       (implicit msgs: Messages) = {
    viewDocument.getElementById("account-number").text() mustBe
      msgs("cf.cash-account.detail.account", accNumber)

    viewDocument.html().contains(msgs("cf.cash-account.detail.heading")) mustBe true
    viewDocument.getElementById("balance-available").text() mustBe "£8,788.00 available"
  }

  private def shouldContainCorrectSearchForTransactionsInputTextDetails(viewDocument: Document)
                                                                       (implicit msgs: Messages)= {
    viewDocument.getElementById("search-transactions").text() mustBe
      msgs("cf.cash-account.detail.account")
  }

  private def shouldContainCorrectRequestTransactionsHeading(viewDocument: Document)
                                                            (implicit msgs: Messages) = {
    viewDocument.getElementById("request-transactions-heading").text() mustBe
      msgs("cf.cash-account.transactions.request-transactions.heading")
  }

  private def shouldContainCorrectDownloadCSVFileLinkUrl(viewDocument: Document)
                                                        (implicit msgs: Messages, config: AppConfig) = {
    val element: Element = viewDocument.getElementById("download-scv-file")
    element.getElementsByAttribute("href").text() mustBe
      msgs("cf.cash-account.transactions.request-transactions.download-csv.url")

    element.html().contains(msgs("cf.cash-account.transactions.request-transactions.download-csv.url")) mustBe true
  }

  private def shouldContainCashAccountDailyStatements(viewDocument: Document)
                                                     (implicit msgs: Messages, config: AppConfig) = {
    viewDocument.html().contains(msgs("cf.cash-account.detail.date")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.transaction-type")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.credit")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.debit")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.balance")) mustBe true

    val tableRowsElementsByClass = viewDocument.getElementsByClass("hmrc-responsive-table__heading")

    tableRowsElementsByClass.size() must be > 0
  }

  private def shouldNotContainCashAccountDailyStatements(viewDocument: Document)
                                                     (implicit msgs: Messages, config: AppConfig) = {
    Option(viewDocument.getElementById("transaction-date")) mustBe empty
    Option(viewDocument.getElementById("transaction-type")) mustBe empty
    Option(viewDocument.getElementById("transaction-credit")) mustBe empty
    Option(viewDocument.getElementById("transaction-debit")) mustBe empty
    Option(viewDocument.getElementById("transaction-balance")) mustBe empty

    val tableRosElementsByClass = viewDocument.getElementsByClass("hmrc-responsive-table__heading")

    tableRosElementsByClass.size() mustBe 0
  }

  trait Setup {
    /*val app: Application = application.build()
    implicit val msgs: Messages = messages(app)
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()*/

    val eoriNumber = "test_eori"
    val can = "12345678"
    val balance: BigDecimal = BigDecimal(8788.00)

    val cashAccount: CashAccount = CashAccount(number = can,
      owner = eoriNumber,
      status = AccountStatusOpen,
      balances = CDSCashBalance(Some(balance)))

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
    val declaration4: Declaration = declaration2.copy(date = date2)

    private val otherTransactions =
      Seq(Transaction(123.45, Payment, None), Transaction(-432.87, Withdrawal, Some("77665544")))

    val dailyStatement1: CashDailyStatement =
      CashDailyStatement(date1, 500.0, 1000.00, Seq(declaration3, declaration4), otherTransactions)

    val dailyStatement2: CashDailyStatement =
      CashDailyStatement(date2, 600.0, 1200.00, Seq(declaration3, declaration4), otherTransactions)

    val dailyStatements: Seq[CashDailyStatement] = Seq(dailyStatement1, dailyStatement2)

    val cashTransactions: CashTransactions = CashTransactions(pendingTransactions, dailyStatements)

    val viewModelWithTransactions: CashAccountViewModelV2 =
      CashAccountViewModelV2(eoriNumber, cashAccount, cashTransactions)

    val viewModelWithNoTransactions: CashAccountViewModelV2 =
      CashAccountViewModelV2(eoriNumber, cashAccount, cashTransactions.copy(Seq(), Seq()))

    protected def createView(viewModel: CashAccountViewModelV2): Document = {
      Jsoup.parse(app.injector.instanceOf[cash_account_v2].apply(viewModel).body)
    }
  }
}
