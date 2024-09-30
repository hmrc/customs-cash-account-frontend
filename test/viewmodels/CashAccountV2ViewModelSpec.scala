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
import models._
import models.FileRole.CashStatement
import models.metadata.CashStatementFileMetadata
import config.AppConfig
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import utils.TestData._
import utils.Utils._
import views.html.components.{cash_account_balance, daily_statements_v2}

import java.time.LocalDate

class CashAccountV2ViewModelSpec extends SpecBase {

  "apply method" should {

    "return correct contents" when {

      "maxTransactionsExceeded is None" in new Setup {

        val cashAccountViewModel: CashAccountV2ViewModel =
          createCashAccountV2ViewModel(eoriNumber, cashAccount, cashTransactions, cashStatementsForEori)

        shouldProduceCorrectTitle(cashAccountViewModel.pageTitle)
        shouldProduceCorrectBackLink(cashAccountViewModel.backLink)
        shouldProduceCorrectAccountBalance(cashAccountViewModel.cashAccountBalance, eoriNumber, cashAccount)
        shouldProduceCorrectRequestTransactionsHeading(cashAccountViewModel.requestTransactionsHeading)
        shouldProduceCorrectDownloadCSVFileLink(cashAccountViewModel.downloadCSVFileLinkUrl)
        shouldOutputCorrectHelpAndSupportGuidance(cashAccountViewModel.helpAndSupportGuidance)
        shouldContainCorrectDailyStatementsComponent(app, cashAccountViewModel.dailyStatements, cashTransactions)
      }

      "when maxTransactionsExceeded is true" in new Setup {

        val cashAccountViewModel: CashAccountV2ViewModel =
          createCashAccountV2ViewModel(eoriNumber, cashAccount, cashTransactions02, cashStatementsForEori)

        shouldProduceCorrectTitle(cashAccountViewModel.pageTitle)
        shouldProduceCorrectBackLink(cashAccountViewModel.backLink)
        shouldProduceCorrectAccountBalanceWithoutLastTxnHeading(cashAccountViewModel.cashAccountBalance,
          eoriNumber, cashAccount)
        shouldProduceCorrectTooManyTransactionsHeading(cashAccountViewModel.tooManyTransactionsHeading.get)
        shouldProduceCorrectTooManyTransactionsStatement(cashAccountViewModel.tooManyTransactionsStatement.get)
        shouldProduceCorrectDownloadCSVFileLinkForMaxTransactionExceeded(cashAccountViewModel.downloadCSVFileLinkUrl)
        shouldOutputCorrectHelpAndSupportGuidance(cashAccountViewModel.helpAndSupportGuidance)
      }
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

  private def shouldProduceCorrectAccountBalanceWithoutLastTxnHeading(
                                                                       accBalance: HtmlFormat.Appendable,
                                                                       eori: String,
                                                                       account: CashAccount
                                                                     )(implicit msgs: Messages,
                                                                       appConfig: AppConfig): Assertion = {
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

  private def shouldProduceCorrectTooManyTransactionsHeading(heading: HtmlFormat.Appendable)
                                                            (implicit msgs: Messages): Assertion = {
    heading mustBe h2Component(
      msgKey = "cf.cash-account.transactions.transactions-for-last-six-months.heading",
      id = Some("last-six-month-transactions-heading"))
  }

  private def shouldProduceCorrectTooManyTransactionsStatement(heading: HtmlFormat.Appendable)
                                                              (implicit msgs: Messages): Assertion = {
    heading mustBe pComponent(
      id = Some("exceeded-threshold-statement"),
      messageKey = "cf.cash-account.transactions.too-many-transactions.hint01",
      classes = "govuk-body govuk-!-margin-bottom-0 govuk-!-margin-top-7")
  }

  private def shouldProduceCorrectDownloadCSVFileLink(link: HtmlFormat.Appendable)
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

  private def shouldProduceCorrectDownloadCSVFileLinkForMaxTransactionExceeded(link: HtmlFormat.Appendable)
                                                                              (implicit msgs: Messages): Assertion = {
    link mustBe linkComponent(
      LinkComponentValues(
        pId = Some("download-scv-file"),
        location = controllers.routes.RequestTransactionsController.onPageLoad().url,
        preLinkMessageKey = Some("cf.cash-account.transactions.too-many-transactions.hint02"),
        linkMessageKey = "cf.cash-account.transactions.too-many-transactions.hint03",
        postLinkMessageKey = Some("cf.cash-account.transactions.too-many-transactions.hint04"),
        enableLineBreakBeforePostMessage = true,
        pClass = "govuk-body govuk-!-margin-bottom-9"))
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
        postLinkMessage = Some("cf.cash-account.help-and-support.link.text.post"))),
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

    val eoriNumber: String = "test_eori"
    val can: String = "12345678"
    val balance: BigDecimal = BigDecimal(8788.00)

    val yearStart: Int = 2024
    val monthStart: Int = 5
    val dayStart: Int = 5
    val yearEnd: Int = 2025
    val monthEnd: Int = 8
    val dayEnd: Int = 8
    val size: Long = 300L

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

    val cashTransactions02: CashTransactions = CashTransactions(pendingTransactions, dailyStatements, Some(true))

    val eoriHistory: EoriHistory = EoriHistory(eori = eoriNumber, validFrom = Some(LocalDate.now()), validUntil = None)

    val cashStatementFile: CashStatementFile = CashStatementFile(
      filename = "statement1.pdf",
      downloadURL = "statement1",
      size = size,
      metadata = CashStatementFileMetadata(
        periodStartYear = yearStart,
        periodStartMonth = monthStart,
        periodStartDay = dayStart,
        periodEndYear = yearEnd,
        periodEndMonth = monthEnd,
        periodEndDay = dayEnd,
        fileFormat = FileFormat.Csv,
        fileRole = CashStatement,
        statementRequestId = Some("abc-defg-1234-abc")
      ),
      eori = eoriNumber
    )

    val currentStatements: Seq[CashStatementsByMonth] = Seq(CashStatementsByMonth(date1, Seq(cashStatementFile)))
    val requestedStatements: Seq[CashStatementsByMonth] = Seq(CashStatementsByMonth(date2, Seq(cashStatementFile)))

    val cashStatementsForEori: Seq[CashStatementsForEori] = Seq(
      CashStatementsForEori(
        eoriHistory = eoriHistory,
        currentStatements = currentStatements,
        requestedStatements = requestedStatements
      )
    )

    def createCashAccountV2ViewModel(eori: String,
                                     account: CashAccount,
                                     cashTrans: CashTransactions,
                                     statements: Seq[CashStatementsForEori]): CashAccountV2ViewModel =
      CashAccountV2ViewModel(eori, account, cashTrans, statements)
  }
}
