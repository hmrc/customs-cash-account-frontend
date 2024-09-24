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
import views.html.components.cash_account_balance

import java.time.LocalDate

class TooManyTransactionsViewModelSpec extends SpecBase {

  "apply method" should {

    "return correct contents" in new Setup {
      val tooManyTransactionsViewModel: TooManyTransactionsViewModel =
        TooManyTransactionsViewModel(eoriNumber, cashAccount)

      shouldProduceCorrectTitle(tooManyTransactionsViewModel.pageTitle)
      shouldProduceCorrectBackLink(tooManyTransactionsViewModel.backLink)
      shouldProduceCorrectAccountBalance(tooManyTransactionsViewModel.cashAccountBalance, eoriNumber, cashAccount)
      shouldProduceCorrectRequestTransactionsHeading(tooManyTransactionsViewModel.requestTransactionsHeading)
      shouldProduceCorrectDownloadCSVFileLinkUrl(tooManyTransactionsViewModel.downloadCSVFileLinkUrl)
      shouldOutputCorrectHelpAndSupportGuidance(tooManyTransactionsViewModel.helpAndSupportGuidance)
    }
  }

  private def shouldProduceCorrectTitle(title: String)(implicit msgs: Messages): Assertion = {
    title mustBe msgs("cf.cash-account.detail.title")
  }

  private def shouldProduceCorrectBackLink(linkStr: String)(implicit appConfig: AppConfig): Assertion = {
    linkStr mustBe appConfig.customsFinancialsFrontendHomepage
  }

  private def shouldProduceCorrectAccountBalance(accBalance: HtmlFormat.Appendable,
                                                 eori: String,
                                                 account: CashAccount)
                                                (implicit msgs: Messages, appConfig: AppConfig): Assertion = {
    val expectedAccBalance: HtmlFormat.Appendable =
      new cash_account_balance(emptyH1Component, emptyH2InnerComponent, emptyPComponent)
        .apply(model = CashAccountViewModel(eori, account), showLastTransactionsHeading = false)

    accBalance mustBe expectedAccBalance
  }

  private def shouldProduceCorrectRequestTransactionsHeading(heading: HtmlFormat.Appendable)
                                                            (implicit msgs: Messages): Assertion = {
    heading mustBe h2Component(
      msgKey = "cf.cash-account.transactions.request-transactions.heading",
      id = Some("request-transactions-heading"))
  }

  private def shouldProduceCorrectDownloadCSVFileLinkUrl(link: HtmlFormat.Appendable)
                                                        (implicit msgs: Messages): Assertion = {
    link mustBe linkComponent(
      LinkComponentValues(
        pId = Some("download-scv-file"),
        location = controllers.routes.RequestTransactionsController.onPageLoad().url,
        preLinkMessageKey = Some("cf.cash-account.transactions.too-many-transactions.hint02"),
        linkMessageKey = "cf.cash-account.transactions.too-many-transactions.hint03",
        postLinkMessageKey = Some("cf.cash-account.transactions.too-many-transactions.hint04"),
        enableLineBreakBeforePostMessage = true,
        pClass = "govuk-body govuk-!-margin-bottom-9")
    )
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

    val declaration1: Declaration =
      Declaration(MOVEMENT_REF_NUMBER, Some(EORI_NUMBER), EORI_NUMBER, Some(DECLARANT_REF), DATE, AMOUNT,
        Seq(TAX_GROUP), Some(SECURE_MOVEMENT_REF_NUMBER))

    val declaration2: Declaration =
      Declaration(MOVEMENT_REF_NUMBER, Some(EORI_NUMBER), EORI_NUMBER, Some(DECLARANT_REF), DATE_1, AMOUNT,
        Seq(TAX_GROUP), Some(SECURE_MOVEMENT_REF_NUMBER))

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
  }
}
