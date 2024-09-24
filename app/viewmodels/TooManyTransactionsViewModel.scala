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
import models.domain.EORI
import utils.Utils.*
import play.twirl.api.HtmlFormat
import views.html.components.cash_account_balance
import models.CashAccountViewModel
import play.api.i18n.Messages

case class TooManyTransactionsViewModel(pageTitle: String,
                                        backLink: String,
                                        cashAccountBalance: HtmlFormat.Appendable,
                                        requestTransactionsHeading: HtmlFormat.Appendable,
                                        downloadCSVFileLinkUrl: HtmlFormat.Appendable,
                                        helpAndSupportGuidance: GuidanceRow)

object TooManyTransactionsViewModel {

  def apply(eori: EORI,
            account: CashAccount)(implicit msgs: Messages, config: AppConfig): TooManyTransactionsViewModel = {

    val cashAccountBalance: HtmlFormat.Appendable =
      new cash_account_balance(emptyH1Component, emptyH2InnerComponent, emptyPComponent)
        .apply(model = CashAccountViewModel(eori, account), showLastTransactionsHeading = false)

    val requestTransactionsHeading: HtmlFormat.Appendable =
      h2Component(
        msgKey = "cf.cash-account.transactions.request-transactions.heading",
        id = Some("request-transactions-heading"))

    TooManyTransactionsViewModel(
      pageTitle = msgs("cf.cash-account.detail.title"),
      backLink = config.customsFinancialsFrontendHomepage,
      cashAccountBalance = cashAccountBalance,
      requestTransactionsHeading = requestTransactionsHeading,
      downloadCSVFileLinkUrl = downloadCSVFileLinkUrl,
      helpAndSupportGuidance = helpAndSupport)
  }

  private def downloadCSVFileLinkUrl(implicit msgs: Messages): HtmlFormat.Appendable = {
    linkComponent(
      LinkComponentValues(
        pId = Some("download-scv-file"),
        location = controllers.routes.RequestTransactionsController.onPageLoad().url,
        preLinkMessageKey = Some("cf.cash-account.transactions.too-many-transactions.hint02"),
        linkMessageKey = "cf.cash-account.transactions.too-many-transactions.hint03",
        postLinkMessageKey = Some("cf.cash-account.transactions.too-many-transactions.hint04"),
        enableLineBreakBeforePostMessage = true,
        pClass = "govuk-body govuk-!-margin-bottom-9"))
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
}
