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
import models.CashAccount
import models.response.PaymentsWithdrawalsAndTransfer
import play.twirl.api.HtmlFormat
import views.html.components.payment_search_results_v2
import play.api.i18n.Messages
import utils.Utils.{
  emptyGovUkTableComponent, emptyH1InnerComponent, h2Component,
  h2InnerComponent, hmrcNewTabLinkComponent
}
import viewmodels.pagination.ListPaginationViewModel


case class PaymentSearchResultsViewModel(pageTitle: String,
                                         backLink: String,
                                         accountDetails: HtmlFormat.Appendable,
                                         searchResultsHeader: HtmlFormat.Appendable,
                                         paymentSearchResultSection: HtmlFormat.Appendable,
                                         helpAndSupportGuidance: GuidanceRow,
                                         paginationModel: Option[ListPaginationViewModel] = None)

object PaymentSearchResultsViewModel {

  def apply(searchValue: String,
            account: CashAccount,
            paymentsWithdrawalsAndTransfers: Seq[PaymentsWithdrawalsAndTransfer],
            pageNo: Option[Int])(implicit msgs: Messages, config: AppConfig): PaymentSearchResultsViewModel = {

    val populateAccountDetails: HtmlFormat.Appendable =
      h2InnerComponent(id = Some("account-number"), innerMsg = account.number, msgKey = "cf.cash-account.detail.account")

    val totalDailyStatementsSize: Int = paymentsWithdrawalsAndTransfers.size

    PaymentSearchResultsViewModel(
      pageTitle = msgs("cf.cash-account.detail.title"),
      backLink = controllers.routes.CashAccountV2Controller.showAccountDetails(pageNo).url,
      accountDetails = populateAccountDetails,
      searchResultsHeader = populateSearchResultsHeader(searchValue),
      paymentSearchResultSection = populatePaymentSearchResultSection(paymentsWithdrawalsAndTransfers, pageNo),
      helpAndSupportGuidance = helpAndSupport,
      paginationModel = populatePaginationModel(searchValue, pageNo, totalDailyStatementsSize))
  }

  private def populateSearchResultsHeader(searchInput: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    emptyH1InnerComponent(msg = "cf.cash-account.detail.declaration.search-title", innerMsg = searchInput)
  }

  private def populatePaymentSearchResultSection(seqOfXyz: Seq[PaymentsWithdrawalsAndTransfer],
                                                 pageNo: Option[Int] = None)(
                                                  implicit msgs: Messages, config: AppConfig): HtmlFormat.Appendable = {

    new payment_search_results_v2(emptyGovUkTableComponent)
      .apply(PaymentSearchResultStatementsViewModel(seqOfXyz, Some(pageNo.getOrElse(1))))
  }

  private def helpAndSupport(implicit appConfig: AppConfig, messages: Messages): GuidanceRow = {
    GuidanceRow(
      h2Heading = h2Component(
        id = Some("search-transactions-support-message-heading"),
        msgKey = "site.support.heading"
      ),

      link = Some(hmrcNewTabLinkComponent(linkMessage = "cf.cash-account.help-and-support.link.text",
        href = appConfig.cashAccountForCdsDeclarationsUrl,
        preLinkMessage = Some("cf.cash-account.help-and-support.link.text.pre.v2"),
        postLinkMessage = Some("cf.cash-account.help-and-support.link.text.post")))
    )
  }

  private def populatePaginationModel(searchValue: String,
                                      pageNo: Option[Int],
                                      totalDailyStatementsSize: Int)
                                     (implicit config: AppConfig) = {
    val isPaginationDisabled = totalDailyStatementsSize <= config.numberOfRecordsPerPage

    if (isPaginationDisabled) {
      None
    } else {
      Some(ListPaginationViewModel(
        totalNumberOfItems = totalDailyStatementsSize,
        currentPage = pageNo.getOrElse(1),
        numberOfItemsPerPage = config.numberOfRecordsPerPage,
        href = controllers.routes.CashAccountPaymentSearchController.search(searchValue, None).url))
    }
  }
}
