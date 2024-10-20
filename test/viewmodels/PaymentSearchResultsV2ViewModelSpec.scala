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

package viewmodels

import config.AppConfig
import models.*
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import utils.SpecBase
import utils.TestData.*
import utils.Utils.*
import viewmodels.pagination.{ListPaginationViewModel, MetaData}

class PaymentSearchResultsV2ViewModelSpec extends SpecBase {

  "apply method" should {

    "return correct contents without pagination" when {

      "search results are less than records per page in config" in new Setup {

        val viewModel = PaymentSearchResultsViewModel.apply(searchValue = PAYMENT_SEARCH_VALUE,
          account = cashAccount, paymentsWithdrawalsAndTransfers = SEQ_OF_PAYMENT_DETAILS_01, pageNo = Some(1))

        shouldProduceCorrectTitle(viewModel.pageTitle)
        shouldProduceCorrectBackLink(viewModel.backLink)
        shouldProduceCorrectAccountBalance(viewModel.accountDetails, eoriNumber, cashAccount)
        shouldProduceCorrectSearchResultsHeader(viewModel.searchResultsHeader)
        shouldProducePaymentSearchResultSection(viewModel.paymentSearchResultSection)
        shouldOutputCorrectHelpAndSupportGuidance(viewModel.helpAndSupportGuidance)
        shouldNotContainPaginationModel(viewModel.paginationModel)
      }
    }

    "return correct contents with pagination" when {

      "search results are more than records per page in config" in new Setup {

        val viewModel = PaymentSearchResultsViewModel.apply(searchValue = PAYMENT_SEARCH_VALUE,
          account = cashAccount, paymentsWithdrawalsAndTransfers = SEQ_OF_PAYMENT_DETAILS_02, pageNo = Some(1))

        shouldProduceCorrectTitle(viewModel.pageTitle)
        shouldProduceCorrectBackLink(viewModel.backLink)
        shouldProduceCorrectAccountBalance(viewModel.accountDetails, eoriNumber, cashAccount)
        shouldProduceCorrectSearchResultsHeader(viewModel.searchResultsHeader)
        shouldProducePaymentSearchResultSection(viewModel.paymentSearchResultSection)
        shouldOutputCorrectHelpAndSupportGuidance(viewModel.helpAndSupportGuidance)
        shouldContainCorrectPaginationModel(viewModel.paginationModel.get)
      }
    }
  }

  private def shouldProduceCorrectTitle(title: String)(implicit msgs: Messages): Assertion = {
    title mustBe msgs("cf.cash-account.detail.title")
  }

  private def shouldProduceCorrectBackLink(linkStr: String): Assertion = {
    linkStr mustBe controllers.routes.CashAccountV2Controller.showAccountDetails(None).url
  }

  private def shouldProduceCorrectSearchResultsHeader(searchResultsHeader: HtmlFormat.Appendable)(implicit msgs: Messages): Assertion = {
    searchResultsHeader.body.toString.contains(
      msgs("cf.cash-account.detail.declaration.search-title", PAYMENT_SEARCH_VALUE)) mustBe true
  }


  private def shouldProduceCorrectAccountBalance(accountDetails: HtmlFormat.Appendable,
                                                 eori: String,
                                                 account: CashAccount)
                                                (implicit msgs: Messages): Assertion = {
    val htmlContent = accountDetails.body.toString
    htmlContent.contains(msgs("cf.cash-account.detail.account", account.number)) mustBe true
    htmlContent.contains(eori) mustBe false

  }

  private def shouldProducePaymentSearchResultSection(accountDetails: HtmlFormat.Appendable): Assertion = {

    val htmlContent = accountDetails.body.toString

    htmlContent.contains("title=\"Date\"") mustBe true
    htmlContent.contains("title=\"Transaction type\"") mustBe true
    htmlContent.contains("title=\"Credit\"") mustBe true
    htmlContent.contains("title=\"Debit\"") mustBe true
    htmlContent.contains("title=\"Balance\"") mustBe false
  }


  private def shouldOutputCorrectHelpAndSupportGuidance(guidanceRow: GuidanceRow)
                                                       (implicit msgs: Messages, config: AppConfig) = {

    guidanceRow mustBe GuidanceRow(h2Heading = h2Component(
      id = Some("search-transactions-support-message-heading"),
      msgKey = "site.support.heading"
    ),
      link = Some(hmrcNewTabLinkComponent(linkMessage = "cf.cash-account.help-and-support.link.text",
        href = config.cashAccountForCdsDeclarationsUrl,
        preLinkMessage = Some("cf.cash-account.help-and-support.link.text.pre.v2"),
        postLinkMessage = Some("cf.cash-account.help-and-support.link.text.post")))
    )
  }


  private def shouldContainCorrectPaginationModel(paginationModel: ListPaginationViewModel): Assertion = {
    paginationModel.results mustBe MetaData(PAGE_1, PAGE_5, PAGE_40, PAGE_1, PAGE_8)
    paginationModel.pageNumber mustBe 1
  }

  private def shouldNotContainPaginationModel(paginationModel: Option[ListPaginationViewModel]): Assertion = {
    paginationModel mustBe empty
  }

  trait Setup {

    val eoriNumber: String = "test_eori"
    val can: String = "12345678"
    val balance: BigDecimal = BigDecimal(8788.00)

    val app: Application = application.build()

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msgs: Messages = messages(app)

    val cashAccount: CashAccount = CashAccount(number = can,
      owner = eoriNumber,
      status = AccountStatusOpen,
      balances = CDSCashBalance(Some(balance)))
  }
}
