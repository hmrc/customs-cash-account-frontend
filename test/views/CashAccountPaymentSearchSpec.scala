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

import models.*
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import play.api.i18n.Messages
import utils.SpecBase
import utils.TestData.*
import viewmodels.PaymentSearchResultsViewModel
import views.html.cash_account_payment_search

class CashAccountPaymentSearchSpec extends SpecBase with ViewTestHelper {

  "view" should {

    "display correct contents" when {

      "there are no transactions" in new Setup {
        val view: Document = createView(viewModel01)

        shouldContainCorrectTitle(view)
        shouldContainBackLinkUrl(view)
        shouldContainCorrectAccountDetails(view, can)
        shouldContainCorrectHeading(view)
        shouldContainNoTransactionMessage(view)
        shouldNotContainPaymentSearchResultStatements(view)
        shouldContainCorrectHelpAndSupportGuidance(view)
        shouldNotDisplayPaginationComponent(view)
      }

      "records are equal or less than maximum no of records per page" in new Setup {

        val view: Document = createView(viewModel02)

        shouldContainCorrectTitle(view)
        shouldContainBackLinkUrl(view)
        shouldContainCorrectAccountDetails(view, can)
        shouldContainCorrectHeading(view)
        shouldContainPaymentSearchResultStatements(view)
        shouldContainCorrectHelpAndSupportGuidance(view)
        shouldNotDisplayPaginationComponent(view)
      }

      "records are more than maximum no of records per page (currently 30) " in new Setup {
        val view: Document = createView(viewModel03)

        shouldContainCorrectTitle(view)
        shouldContainBackLinkUrl(view)
        shouldContainCorrectAccountDetails(view, can)
        shouldContainCorrectHeading(view)
        shouldContainPaymentSearchResultStatements(view)
        shouldContainCorrectHelpAndSupportGuidance(view)
        shouldDisplayPaginationComponent(view)
      }
    }
  }

  private def shouldContainCorrectTitle(view: Document): Assertion = {
    view.title() mustBe s"${messages("cf.cash-account.detail.title")} - ${messages("service.name")} - GOV.UK"
  }

  private def shouldContainBackLinkUrl(view: Document): Assertion = {
    view.html().contains(controllers.routes.CashAccountV2Controller.showAccountDetails(None).url) mustBe true
  }

  private def shouldContainCorrectAccountDetails(viewDocument: Document, accNumber: String
                                                )(implicit msgs: Messages) = {

    viewDocument.getElementById("account-number").text() mustBe msgs("cf.cash-account.detail.account", accNumber)
    viewDocument.html().contains(msgs("cf.cash-account.detail.heading")) mustBe true
    Option(viewDocument.getElementById("balance-available")) mustBe None
  }

  private def shouldContainCorrectHeading(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.getElementById("search-results-message-heading").text() mustBe
      msgs("cf.cash-account.detail.declaration.search-title", PAYMENT_SEARCH_VALUE)
  }

  private def shouldContainNoTransactionMessage(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.getElementById("no-transactions-to-display").text() mustBe
      msgs("cf.cash-account.transactions.no-transactions.message")
  }

  private def shouldContainCorrectHelpAndSupportGuidance(viewDocument: Document)(implicit msgs: Messages) = {

    val supportHeading = viewDocument.getElementById("search-transactions-support-message-heading")
    supportHeading.text() mustBe msgs("site.support.heading")

    viewDocument.html().contains("https://www.gov.uk/guidance/use-a-cash-account-for-cds-declarations") mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.help-and-support.link.text")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.help-and-support.link.text.pre.v2")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.help-and-support.link.text.post")) mustBe true
  }

  private def shouldContainPaymentSearchResultStatements(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.html().contains(msgs("cf.cash-account.detail.date")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.transaction-type")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.credit")) mustBe true
    viewDocument.html().contains(msgs("cf.cash-account.detail.debit")) mustBe true

    val tableRowsElementsByClass = viewDocument.getElementsByClass("hmrc-responsive-table__heading")

    tableRowsElementsByClass.size() must be > 0
  }

  private def shouldNotContainPaymentSearchResultStatements(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.html().contains(msgs("cf.cash-account.detail.date")) mustBe false
    viewDocument.html().contains(msgs("cf.cash-account.detail.transaction-type")) mustBe false
    viewDocument.html().contains(msgs("cf.cash-account.detail.credit")) mustBe false
    viewDocument.html().contains(msgs("cf.cash-account.detail.debit")) mustBe false

    val tableRosElementsByClass = viewDocument.getElementsByClass("hmrc-responsive-table__heading")

    tableRosElementsByClass.size() mustBe 0
  }

  private def shouldDisplayPaginationComponent(viewDocument: Document) = {
    shouldContainTheElement(view = viewDocument, classes = Some("govuk-pagination"))
  }

  private def shouldNotDisplayPaginationComponent(viewDocument: Document) = {
    shouldNotContainTheElement(view = viewDocument, classes = Some("govuk-pagination"))
  }

  trait Setup {
    val eoriNumber: String = "test_eori"
    val can: String = "12345678"
    val balance: BigDecimal = BigDecimal(8788.00)

    val cashAccount: CashAccount = CashAccount(number = can,
      owner = eoriNumber,
      status = AccountStatusOpen,
      balances = CDSCashBalance(Some(balance)))

    val viewModel01: PaymentSearchResultsViewModel = PaymentSearchResultsViewModel.apply(
      searchValue = PAYMENT_SEARCH_VALUE, account = cashAccount,
      paymentsWithdrawalsAndTransfers = Seq.empty, pageNo = Some(1))

    val viewModel02: PaymentSearchResultsViewModel = PaymentSearchResultsViewModel.apply(
      searchValue = PAYMENT_SEARCH_VALUE, account = cashAccount,
      paymentsWithdrawalsAndTransfers = SEQ_OF_PAYMENT_DETAILS_01, pageNo = Some(1))

    val viewModel03: PaymentSearchResultsViewModel = PaymentSearchResultsViewModel.apply(
      searchValue = PAYMENT_SEARCH_VALUE, account = cashAccount,
      paymentsWithdrawalsAndTransfers = SEQ_OF_PAYMENT_DETAILS_02, pageNo = Some(1))

    protected def createView(viewModel: PaymentSearchResultsViewModel): Document = {
      Jsoup.parse(app.injector.instanceOf[cash_account_payment_search].apply(viewModel).body)
    }
  }
}
