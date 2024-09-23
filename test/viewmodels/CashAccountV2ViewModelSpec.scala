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

import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import models.{
  AccountStatusOpen,
  CDSCashBalance,
  CashAccount,
  CashAccountViewModel,
  CashDailyStatement,
  CashTransactions,
  Declaration,
  Payment,
  Transaction,
  Withdrawal
}
import config.AppConfig
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import utils.TestData.*
import utils.Utils.{LinkComponentValues, emptyH1Component, emptyH2InnerComponent, emptyPComponent, h2Component, hmrcNewTabLinkComponent, linkComponent}
import views.html.components.{cash_account_balance, daily_statements_v2}

import java.time.LocalDate

class CashAccountV2ViewModelSpec extends SpecBase {

  "apply method" should {

    "return correct contents" in new Setup {
      val cashAccountViewModel: CashAccountV2ViewModel =
        createCashAccountV2ViewModel(eoriNumber, cashAccount, cashTransactions)

      shouldProduceCorrectTitle(cashAccountViewModel.pageTitle)
      shouldProduceCorrectBackLink(cashAccountViewModel.backLink)
      shouldProduceCorrectAccountBalance(cashAccountViewModel.cashAccountBalance, eoriNumber, cashAccount)
      shouldProduceCorrectRequestTransactionsHeading(cashAccountViewModel.requestTransactionsHeading)
      shouldProduceCorrectDownloadCSVFileLinkUrl(cashAccountViewModel.downloadCSVFileLinkUrl)
      shouldOutputCorrectHelpAndSupportGuidance(cashAccountViewModel.helpAndSupportGuidance)
      shouldContainCorrectDailyStatementsComponent(app, cashAccountViewModel.dailyStatements, cashTransactions)
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
        .apply(model = CashAccountViewModel(eori, account))

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
        linkMessageKey = "cf.cash-account.transactions.request-transactions.download-csv.url",
        location = controllers.routes.RequestTransactionsController.onPageLoad().url,
        postLinkMessageKey = Some("cf.cash-account.transactions.request-transactions.download-csv.post-message"),
        enableLineBreakBeforePostMessage = true)
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

  private def shouldContainCorrectDailyStatementsComponent(app: Application,
                                                           dailyStatementsComponent: HtmlFormat.Appendable,
                                                           cashTransactions: CashTransactions)
                                                          (implicit msgs: Messages): Assertion = {
    val expectedDailyStatementsComponent =
      app.injector.instanceOf[daily_statements_v2].apply(CashAccountDailyStatementsViewModel(cashTransactions))

    dailyStatementsComponent mustBe expectedDailyStatementsComponent
  }

  trait Setup {

    val eoriNumber = "test_eori"
    val can = "12345678"
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

    def createCashAccountV2ViewModel(eori: String,
                                     account: CashAccount,
                                     cashTrans: CashTransactions): CashAccountV2ViewModel =
      CashAccountV2ViewModel(eori, account, cashTrans)
  }

}
