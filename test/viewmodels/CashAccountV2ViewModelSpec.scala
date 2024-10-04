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
  AccountStatusOpen, CDSCashBalance, CashAccount, CashAccountViewModel, CashDailyStatement,
  CashStatementFile, CashStatementsByMonth, CashStatementsForEori, CashTransactions, Declaration, EoriHistory,
  FileFormat, Payment, Transaction, Withdrawal
}
import models.metadata.CashStatementFileMetadata
import models.FileRole.CashStatement
import config.AppConfig
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import utils.TestData.*
import utils.Utils.{
  LinkComponentValues, emptyH1Component, emptyH2InnerComponent, emptyPComponent, h2Component,
  hmrcNewTabLinkComponent, linkComponent, notificationPanelComponent, pComponent
}
import viewmodels.pagination.ListPaginationViewModel
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
        shouldProduceCorrectDownloadCSVFileLink(cashAccountViewModel.downloadCSVFileLinkUrl)
        shouldOutputCorrectHelpAndSupportGuidance(cashAccountViewModel.helpAndSupportGuidance)
        shouldContainCorrectDailyStatementsSection(app, cashAccountViewModel.dailyStatementsSection.get, cashTransactions)
      }

      "maxTransactionsExceeded is true" in new Setup {

        val cashAccountViewModel: CashAccountV2ViewModel =
          createCashAccountV2ViewModel(eoriNumber, cashAccount, cashTransactions02, cashStatementsForEori)

        shouldProduceCorrectTitle(cashAccountViewModel.pageTitle)
        shouldProduceCorrectBackLink(cashAccountViewModel.backLink)
        shouldProduceCorrectAccountBalanceWithoutLastTxnHeading(cashAccountViewModel.cashAccountBalance,
          eoriNumber, cashAccount)
        shouldProduceCorrectTooManyTransactionsSection(cashAccountViewModel.tooManyTransactionsSection.get)
        shouldProduceCorrectDownloadCSVFileLinkForMaxTransactionExceeded(cashAccountViewModel.downloadCSVFileLinkUrl)
        shouldOutputCorrectHelpAndSupportGuidance(cashAccountViewModel.helpAndSupportGuidance)
      }

      "statements are available and notification panel is displayed" in new Setup {

        val cashAccountViewModel: CashAccountV2ViewModel =
          createCashAccountV2ViewModel(eoriNumber, cashAccount, cashTransactions, cashStatementsForEori)

        shouldProduceCorrectTitle(cashAccountViewModel.pageTitle)
        shouldProduceCorrectBackLink(cashAccountViewModel.backLink)
        shouldProduceCorrectAccountBalance(cashAccountViewModel.cashAccountBalance, eoriNumber, cashAccount)
        shouldContainNotificationPanel(cashAccountViewModel.cashStatementNotification.get)
        shouldProduceCorrectDownloadCSVFileLink(cashAccountViewModel.downloadCSVFileLinkUrl)
        shouldOutputCorrectHelpAndSupportGuidance(cashAccountViewModel.helpAndSupportGuidance)
        shouldContainCorrectDailyStatementsSection(app, cashAccountViewModel.dailyStatementsSection.get, cashTransactions)
      }
    }

    "populate the pagination model correctly" when {

      "daily statements are more than 30 in number" in new Setup {
        val cashAccountViewModel: CashAccountV2ViewModel =
          createCashAccountV2ViewModel(
            eoriNumber, cashAccount, cashTransactionsWithMoreThan30Records, cashStatementsForEori, Some(1))

        shouldProduceCorrectTitle(cashAccountViewModel.pageTitle)
        shouldProduceCorrectBackLink(cashAccountViewModel.backLink)
        shouldProduceCorrectAccountBalance(cashAccountViewModel.cashAccountBalance, eoriNumber, cashAccount)
        shouldContainNotificationPanel(cashAccountViewModel.cashStatementNotification.get)
        shouldProduceCorrectDownloadCSVFileLink(cashAccountViewModel.downloadCSVFileLinkUrl)
        shouldOutputCorrectHelpAndSupportGuidance(cashAccountViewModel.helpAndSupportGuidance)
        shouldContainCorrectPaginationModel(cashAccountViewModel.paginationModel.get)
      }

      "daily statements are less than 30 in number" in new Setup {
        val cashAccountViewModel: CashAccountV2ViewModel =
          createCashAccountV2ViewModel(
            eoriNumber, cashAccount, cashTransactions, cashStatementsForEori, Some(1))

        shouldProduceCorrectTitle(cashAccountViewModel.pageTitle)
        shouldProduceCorrectBackLink(cashAccountViewModel.backLink)
        shouldProduceCorrectAccountBalance(cashAccountViewModel.cashAccountBalance, eoriNumber, cashAccount)
        shouldContainNotificationPanel(cashAccountViewModel.cashStatementNotification.get)
        shouldProduceCorrectDownloadCSVFileLink(cashAccountViewModel.downloadCSVFileLinkUrl)
        shouldOutputCorrectHelpAndSupportGuidance(cashAccountViewModel.helpAndSupportGuidance)
        shouldNotContainPaginationModel(cashAccountViewModel.paginationModel)
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
        .apply(model = CashAccountViewModel(eori, account), displayLastSixMonthsHeading = false)

    accBalance mustBe expectedAccBalance
  }

  private def shouldContainNotificationPanel(notification: HtmlFormat.Appendable)
                                            (implicit msgs: Messages, appConfig: AppConfig): Assertion = {

    notification mustBe notificationPanelComponent(
      showNotification = true,
      preMessage = msgs("cf.cash-account.requested.statements.available.text.pre"),
      linkUrl = appConfig.requestedStatements(CashStatement),
      linkText = msgs("cf.cash-account.requested.statements.available.link.text"),
      postMessage = msgs("cf.cash-account.requested.statements.available.text.post"))
  }

  private def shouldProduceCorrectAccountBalanceWithoutLastTxnHeading(accBalance: HtmlFormat.Appendable,
                                                                      eori: String,
                                                                      account: CashAccount)
                                                                     (implicit msgs: Messages,
                                                                      appConfig: AppConfig): Assertion = {
    val expectedAccBalance: HtmlFormat.Appendable =
      new cash_account_balance(emptyH1Component, emptyH2InnerComponent, emptyPComponent)
        .apply(model = CashAccountViewModel(eori, account), displayLastSixMonthsHeading = false)

    accBalance mustBe expectedAccBalance
  }

  private def shouldProduceCorrectTooManyTransactionsSection(section: TooManyTransactionsSection)
                                                            (implicit msgs: Messages): Assertion = {
    section.heading mustBe h2Component(
      msgKey = "cf.cash-account.transactions.transactions-for-last-six-months.heading",
      id = Some("last-six-month-transactions-heading"))

    section.paragraph mustBe pComponent(
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
        location = controllers.routes.SelectTransactionsController.onPageLoad().url,
        postLinkMessageKey = Some("cf.cash-account.transactions.request-transactions.download-csv.post-message"),
        enableLineBreakBeforePostMessage = true)
    )
  }

  private def shouldProduceCorrectDownloadCSVFileLinkForMaxTransactionExceeded(link: HtmlFormat.Appendable)
                                                                              (implicit msgs: Messages): Assertion = {
    link mustBe linkComponent(
      LinkComponentValues(
        pId = Some("download-scv-file"),
        location = controllers.routes.SelectTransactionsController.onPageLoad().url,
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
        postLinkMessage = Some("cf.cash-account.help-and-support.link.text.post")))
    )

  }

  private def shouldContainCorrectDailyStatementsSection(app: Application,
                                                         section: DailyStatementsSection,
                                                         cashTransactions: CashTransactions)
                                                        (implicit msgs: Messages, config: AppConfig): Assertion = {
    val expectedDailyStatementsComponent =
      app.injector.instanceOf[daily_statements_v2].apply(CashAccountDailyStatementsViewModel(cashTransactions, None))

    section.dailyStatements mustBe expectedDailyStatementsComponent

    section.requestTransactionsHeading mustBe h2Component(
      msgKey = "cf.cash-account.transactions.request-transactions.heading",
      id = Some("request-transactions-heading"),
      classes = "govuk-heading-m govuk-!-margin-top-9")
  }

  private def shouldContainCorrectPaginationModel(paginationModel: ListPaginationViewModel)
                                                 (implicit config: AppConfig): Assertion = {
    paginationModel mustBe
      ListPaginationViewModel(
        PAGE_32, 1, PAGE_30, controllers.routes.CashAccountV2Controller.showAccountDetails(None).url)
  }

  private def shouldNotContainPaginationModel(paginationModel: Option[ListPaginationViewModel]): Assertion = {
    paginationModel mustBe empty
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
    val totalElements = 8

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

    val dailyStatementsMoreThan30: Seq[CashDailyStatement] =
      Seq(dailyStatement1, dailyStatement2, dailyStatement1, dailyStatement2, dailyStatement1, dailyStatement2,
        dailyStatement1, dailyStatement2)

    val cashTransactions: CashTransactions = CashTransactions(pendingTransactions, dailyStatements)

    val cashTransactionsWithMoreThan30Records: CashTransactions =
      CashTransactions(pendingTransactions, dailyStatementsMoreThan30)

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
                                     statements: Seq[CashStatementsForEori],
                                     pageNo: Option[Int] = None): CashAccountV2ViewModel =
      CashAccountV2ViewModel(eori, account, cashTrans, statements, pageNo)
  }
}
