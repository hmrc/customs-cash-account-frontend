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
import models.FileRole.CashStatement
import models.{CashAccount, CashAccountViewModel, CashStatementsForEori, CashTransactions}
import models.domain.EORI
import utils.Utils.*
import play.twirl.api.HtmlFormat
import views.html.components.{cash_account_balance, daily_statements_v2}
import play.api.i18n.Messages

case class GuidanceRow(h2Heading: HtmlFormat.Appendable,
                       link: Option[HtmlFormat.Appendable] = None,
                       paragraph: Option[HtmlFormat.Appendable] = None)

case class TooManyTransactionsSection(heading: HtmlFormat.Appendable,
                                      paragraph: HtmlFormat.Appendable)

case class DailyStatementsSection(dailyStatements: HtmlFormat.Appendable,
                                  requestTransactionsHeading: HtmlFormat.Appendable)

case class CashAccountV2ViewModel(pageTitle: String,
                                  backLink: String,
                                  cashAccountBalance: HtmlFormat.Appendable,
                                  dailyStatementsSection: Option[DailyStatementsSection] = None,
                                  downloadCSVFileLinkUrl: HtmlFormat.Appendable,
                                  tooManyTransactionsSection: Option[TooManyTransactionsSection] = None,
                                  cashStatementNotification: HtmlFormat.Appendable,
                                  helpAndSupportGuidance: GuidanceRow)

object CashAccountV2ViewModel {

  def apply(eori: EORI,
            account: CashAccount,
            cashTrans: CashTransactions,
            statements: Seq[CashStatementsForEori]
           )(implicit msgs: Messages, config: AppConfig): CashAccountV2ViewModel = {

    val hasRequestedStatements: Boolean = statements.exists(_.requestedStatements.nonEmpty)

    val hasMaxTransactionsExceeded: Boolean = cashTrans.maxTransactionsExceeded.getOrElse(false)

    val cashAccountBalance: HtmlFormat.Appendable =
      new cash_account_balance(emptyH1Component, emptyH2InnerComponent, emptyPComponent)
        .apply(model = CashAccountViewModel(eori, account), displayLastSixMonthsHeading = false)

    CashAccountV2ViewModel(
      pageTitle = msgs("cf.cash-account.detail.title"),
      backLink = config.customsFinancialsFrontendHomepage,
      cashAccountBalance = cashAccountBalance,
      dailyStatementsSection = populateDailyStatementsSection(cashTrans),
      downloadCSVFileLinkUrl = downloadCSVFileLinkUrl(hasMaxTransactionsExceeded),
      tooManyTransactionsSection = populateTooManyTransactionsSection(hasMaxTransactionsExceeded),
      cashStatementNotification = populateNotificationPanel(hasRequestedStatements),
      helpAndSupportGuidance = helpAndSupport)
  }

  private def populateNotificationPanel(hasRequestedStatements: Boolean)
                                       (implicit msgs: Messages, config: AppConfig) = {
    if (hasRequestedStatements) {
      notificationPanelComponent(
        showNotification = true,
        preMessage = msgs("cf.cash-account.requested.statements.available.text.pre"),
        linkUrl = config.requestedStatements(CashStatement),
        linkText = msgs("cf.cash-account.requested.statements.available.link.text"),
        postMessage = msgs("cf.cash-account.requested.statements.available.text.post"))
    } else {
      HtmlFormat.empty
    }
  }

  private def populateDailyStatementsSection(cashTrans: CashTransactions)
                                            (implicit msgs: Messages): Option[DailyStatementsSection] = {
    val hasMaxTransactionsExceeded = cashTrans.maxTransactionsExceeded.getOrElse(false)

    if (hasMaxTransactionsExceeded) {
      None
    } else {
      val dailyStatements: HtmlFormat.Appendable =
        new daily_statements_v2(emptyGovUkTableComponent).apply(CashAccountDailyStatementsViewModel(cashTrans))

      val requestTransactionsHeading: HtmlFormat.Appendable = h2Component(
        msgKey = "cf.cash-account.transactions.request-transactions.heading",
        id = Some("request-transactions-heading"),
        classes = "govuk-heading-m govuk-!-margin-top-9")

      Some(DailyStatementsSection(dailyStatements, requestTransactionsHeading))
    }
  }

  private def populateTooManyTransactionsSection(hasMaxTransactionsExceeded: Boolean)
                                                (implicit msgs: Messages): Option[TooManyTransactionsSection] = {
    if (hasMaxTransactionsExceeded) {
      val heading = h2Component(
        msgKey = "cf.cash-account.transactions.transactions-for-last-six-months.heading",
        id = Some("last-six-month-transactions-heading"))

      val paragraph = pComponent(
        id = Some("exceeded-threshold-statement"),
        messageKey = "cf.cash-account.transactions.too-many-transactions.hint01",
        classes = "govuk-body govuk-!-margin-bottom-0 govuk-!-margin-top-7")

      Some(TooManyTransactionsSection(heading, paragraph))
    } else {
      None
    }
  }

  private def downloadCSVFileLinkUrl(hasMaxTransactionsExceeded: Boolean
                                    )(implicit msgs: Messages): HtmlFormat.Appendable = {

    if (hasMaxTransactionsExceeded) {
      linkComponent(
        LinkComponentValues(
          pId = Some("download-scv-file"),
          location = controllers.routes.RequestTransactionsController.onPageLoad().url,
          preLinkMessageKey = Some("cf.cash-account.transactions.too-many-transactions.hint02"),
          linkMessageKey = "cf.cash-account.transactions.too-many-transactions.hint03",
          postLinkMessageKey = Some("cf.cash-account.transactions.too-many-transactions.hint04"),
          enableLineBreakBeforePostMessage = true,
          pClass = "govuk-body govuk-!-margin-bottom-9"))

    } else {
      linkComponent(
        LinkComponentValues(
          pId = Some("download-scv-file"),
          linkMessageKey = "cf.cash-account.transactions.request-transactions.download-csv.url",
          location = controllers.routes.RequestTransactionsController.onPageLoad().url,
          postLinkMessageKey = Some("cf.cash-account.transactions.request-transactions.download-csv.post-message"),
          enableLineBreakBeforePostMessage = true))
    }
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
