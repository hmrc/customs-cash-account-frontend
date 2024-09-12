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

import models.domain.{CAN, EORI}
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import models.{AccountStatusOpen, CDSAccountStatus, CDSCashBalance, CashAccount, CashAccountViewModel,
  CashDailyStatement, CashTransactions, Declaration, Payment, Transaction, Withdrawal}
import config.AppConfig
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import utils.TestData.*
import utils.Utils.{LinkComponentValues, emptyH1Component, h2Component, linkComponent}
import views.html.components.cash_account_balance

import java.time.LocalDate

class CashAccountViewModelV2Spec extends SpecBase {

  "apply method correct viewmodel" should {

    "return correct contents" in new Setup {
      val cashAccountViewModel: CashAccountViewModelV2 =
        createCashAccountViewModel1(eoriNumber, cashAccount, cashTransactions)

      shouldDisplayCorrectTitle(cashAccountViewModel.pageTitle)
      shouldDisplayCorrectBackLink(cashAccountViewModel.backLink)
      shouldDisplayCorrectAccountBalance(cashAccountViewModel.cashAccountBalance, eoriNumber, cashAccount)
      shouldDisplayCorrectRequestTransactionsHeading(cashAccountViewModel.requestTransactionsHeading)
      shouldDisplayCorrectDownloadCSVFileLinkUrl(cashAccountViewModel.downloadCSVFileLinkUrl)
      shouldProduceCorrectDailyStatementViewModel(cashAccountViewModel.dailyStatementsViewModel, cashTransactions)
    }
  }

  private def shouldDisplayCorrectTitle(title: String)(implicit msgs: Messages): Assertion = {
    title mustBe msgs("cf.cash-account.detail.title")
  }

  private def shouldDisplayCorrectBackLink(linkStr: String)(implicit appConfig: AppConfig): Assertion = {
    linkStr mustBe appConfig.customsFinancialsFrontendHomepage
  }

  private def shouldDisplayCorrectAccountBalance(accBalance: HtmlFormat.Appendable,
                                                 eori: EORI,
                                                 account: CashAccount)
                                                (implicit msgs: Messages, appConfig: AppConfig): Assertion = {
    val expectedAccBalance: HtmlFormat.Appendable =
      new cash_account_balance(emptyH1Component).apply(model = CashAccountViewModel(eori, account))

    accBalance mustBe expectedAccBalance
  }

  private def shouldDisplayCorrectRequestTransactionsHeading(heading: HtmlFormat.Appendable)
                                                            (implicit msgs: Messages): Assertion = {
    heading mustBe h2Component(
      msgKey = "cf.cash-account.transactions.request-transactions.heading",
      id = Some("request-transactions-heading"))
    }

  private def shouldDisplayCorrectDownloadCSVFileLinkUrl(link: HtmlFormat.Appendable)
                                                        (implicit msgs: Messages, config: AppConfig): Assertion = {
    link mustBe linkComponent(
      LinkComponentValues(
        pId = Some("download-scv-file"),
        linkMessageKey = "cf.cash-account.transactions.request-transactions.download-csv.url",
        location = config.cashAccountForCdsDeclarationsUrl,
        postLinkMessageKey = Some("cf.cash-account.transactions.request-transactions.download-csv.post-message"))
    )
  }

  private def shouldProduceCorrectDailyStatementViewModel(model: CashAccountDailyStatementsViewModel,
                                                          cashTransactions: CashTransactions)
                                                         (implicit msgs: Messages, config: AppConfig): Assertion = {
    model.dailyStatements.size mustBe CashAccountDailyStatementsViewModel(cashTransactions).dailyStatements.size
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

    def createCashAccountViewModel1(eori: EORI,
                                    account: CashAccount,
                                    cashTrans: CashTransactions): CashAccountViewModelV2 =
      CashAccountViewModelV2(eori, account, cashTrans)
  }

}
