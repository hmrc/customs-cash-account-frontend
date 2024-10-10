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
import models.{CashAccount, CashAccountViewModel}
import models.domain.EORI
import models.response.PaymentsWithdrawalsAndTransfer
import utils.Utils.*
import play.twirl.api.HtmlFormat
import views.html.components.{cash_account_balance, payment_search_results_v2}
import play.api.i18n.Messages
import viewmodels.pagination.ListPaginationViewModel


case class PaymentSearchResultsViewModel(pageTitle: String,
                                         backLink: String,
                                         cashAccountBalance: HtmlFormat.Appendable,
                                         paymentSearchResultSection: HtmlFormat.Appendable,
                                         helpAndSupportGuidance: GuidanceRow,
                                         paginationModel: Option[ListPaginationViewModel] = None)

object PaymentSearchResultsViewModel {

  def apply(eori: EORI,
            account: CashAccount,
            paymentsWithdrawalsAndTransfers: Seq[PaymentsWithdrawalsAndTransfer],
            pageNo: Option[Int])(implicit msgs: Messages, config: AppConfig): PaymentSearchResultsViewModel = {

    val cashAccountBalance: HtmlFormat.Appendable =
      new cash_account_balance(emptyH1Component, emptyH2InnerComponent, emptyPComponent)
        .apply(model = CashAccountViewModel(eori, account), showBalance = false, displayLastSixMonthsHeading = false)

    val totalDailyStatementsSize: Int = paymentsWithdrawalsAndTransfers.size

    PaymentSearchResultsViewModel(
      pageTitle = msgs("cf.cash-account.detail.title"),
      backLink = config.customsFinancialsFrontendHomepage,
      cashAccountBalance = cashAccountBalance,
      paymentSearchResultSection = populatePaymentSearchResultSection(paymentsWithdrawalsAndTransfers, pageNo),
      helpAndSupportGuidance = helpAndSupport,
      paginationModel = populatePaginationModel(pageNo, totalDailyStatementsSize))
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

  private def populatePaginationModel(pageNo: Option[Int],
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
        href = controllers.routes.CashAccountV2Controller.showAccountDetails(None).url))
    }
  }
}
