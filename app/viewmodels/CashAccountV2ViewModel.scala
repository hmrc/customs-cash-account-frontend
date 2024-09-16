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
import models.CashTransactions
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.{cash_account_balance, daily_statements_v2, h1}
import models.CashAccountViewModel
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTable

case class GuidanceRow(h2Heading: HtmlFormat.Appendable,
                       link: Option[HtmlFormat.Appendable] = None,
                       paragraph: Option[HtmlFormat.Appendable] = None)

case class CashAccountV2ViewModel(pageTitle: String,
                                  backLink: String,
                                  cashAccountBalance: HtmlFormat.Appendable,
                                  dailyStatements: HtmlFormat.Appendable,
                                  requestTransactionsHeading: HtmlFormat.Appendable,
                                  downloadCSVFileLinkUrl: HtmlFormat.Appendable,
                                  helpAndSupportGuidance: GuidanceRow)

object CashAccountV2ViewModel {

  def apply(eori: EORI,
            account: CashAccount,
            cashTrans: CashTransactions)(implicit msgs: Messages, config: AppConfig): CashAccountV2ViewModel = {

    val dailyStatementsComponent: HtmlFormat.Appendable =
      new daily_statements_v2(emptyGovUkTableComponent).apply(CashAccountDailyStatementsViewModel(cashTrans))

    val cashAccountBalance: HtmlFormat.Appendable =
      new cash_account_balance(emptyH1Component).apply(model = CashAccountViewModel(eori, account))

    val requestTransactionsHeading: HtmlFormat.Appendable =
      h2Component(
        msgKey = "cf.cash-account.transactions.request-transactions.heading",
        id = Some("request-transactions-heading"))

    CashAccountV2ViewModel(
      pageTitle = msgs("cf.cash-account.detail.title"),
      backLink = config.customsFinancialsFrontendHomepage,
      cashAccountBalance = cashAccountBalance,
      dailyStatements = dailyStatementsComponent,
      requestTransactionsHeading = requestTransactionsHeading,
      downloadCSVFileLinkUrl = downloadCSVFileLinkUrl,
      helpAndSupportGuidance = helpAndSupport)
  }

  private def downloadCSVFileLinkUrl(implicit msgs: Messages): HtmlFormat.Appendable = {
    linkComponent(
      LinkComponentValues(
        pId = Some("download-scv-file"),
        linkMessageKey = "cf.cash-account.transactions.request-transactions.download-csv.url",
        location = controllers.routes.RequestTransactionsController.onPageLoad().url,
        postLinkMessageKey = Some("cf.cash-account.transactions.request-transactions.download-csv.post-message"),
        enableLineBreakBeforePostMessage = true))
  }

  private def helpAndSupport(implicit appConfig: AppConfig, messages: Messages): GuidanceRow = {
    GuidanceRow(
      h2Heading = h2Component(
        id = Some("search-transactions-support-message-heading"),
        msgKey = "site.support.heading"
      ),

      link = Some(hmrcNewTabLinkComponent(linkMessage = "cf.cash-account.help-and-support.link.text",
        href = appConfig.cashAccountForCdsDeclarationsUrl,
        preLinkMessage = Some("cf.cash-account.help-and-support.link.text.pre"),
        postLinkMessage = Some("cf.cash-account.help-and-support.link.text.post")))
    )
  }
}
