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

package controllers

import config.AppConfig
import connectors.{
  CustomsDataStoreConnector,
  CustomsFinancialsApiConnector,
  NoTransactionsAvailable,
  TooManyTransactionsRequested,
  UnknownException
}
import forms.SearchTransactionsFormProvider
import models.email.{UndeliverableEmail, UnverifiedEmail}
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
  Transfer,
  Withdrawal
}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.Application
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase
import views.html.{
  cash_account_no_transactions_v2,
  cash_account_no_transactions_with_balance,
  cash_account_transactions_not_available,
  cash_account_v2
}
import java.time.LocalDate
import scala.concurrent.Future
import scala.util.Random
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.twirl.api.HtmlFormat
import utils.Utils.poundSymbol
import viewmodels.{CashAccountV2ViewModel, GuidanceRow}

class CashAccountV2ControllerSpec extends SpecBase {

  "show account details" must {
    "return OK" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        status(result) mustEqual OK
      }
    }

    "check view cash account details" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }

    "display transactions unavailable if the call to ACC31 fails" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view: cash_account_transactions_not_available =
        app.injector.instanceOf[cash_account_transactions_not_available]

      val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
      val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(CashAccountViewModel(
          eori, cashAccount), appConfig.transactionsTimeoutFlag)(request, messages, appConfig).toString()

      }
    }

    "display no transactions if the call to ACC31 returns no transactions with balance " +
      "if balance is greater than 0" in new Setup {

      private val availableAccountBalance: Int = 100
      val newCashAccount: CashAccount = cashAccount.copy(balances = CDSCashBalance(Some(availableAccountBalance)))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(newCashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view: cash_account_no_transactions_with_balance =
        app.injector.instanceOf[cash_account_no_transactions_with_balance]

      val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
      val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(CashAccountViewModel(
          eori, newCashAccount))(request, messages, appConfig).toString()

        contentAsString(result) must include regex "search and download any previous transactions as a CSV file"
      }
    }

    "display no transactions if the call to ACC31 returns no cashDailyStatements and balance is empty" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount.copy(balances = CDSCashBalance(None)))))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view: cash_account_no_transactions_v2 = app.injector.instanceOf[cash_account_no_transactions_v2]
      val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
      val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          CashAccountViewModel(
            eori,
            cashAccount.copy(balances = CDSCashBalance(Some(0)))))(request, messages, appConfig).toString()

        contentAsString(result) must include regex messages("cf.cash-account.top-up.guidance.text.pre")
        contentAsString(result) must include regex messages("cf.cash-account.top-up.guidance.text.link")
        contentAsString(result) must include regex messages("cf.cash-account.top-up.guidance.text.post")
      }
    }

    "display no transactions if the call to ACC31 returns no cashDailyStatements and balance is 0" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount.copy(balances = CDSCashBalance(Some(0))))))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view: cash_account_no_transactions_v2 = app.injector.instanceOf[cash_account_no_transactions_v2]
      val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
      val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          CashAccountViewModel(
            eori,
            cashAccount.copy(balances = CDSCashBalance(Some(0)))))(request, messages, appConfig).toString()

        contentAsString(result) must include regex messages("cf.cash-account.top-up.guidance.text.pre")
        contentAsString(result) must include regex messages("cf.cash-account.top-up.guidance.text.link")
        contentAsString(result) must include regex messages("cf.cash-account.top-up.guidance.text.post")
      }
    }

    "display page with no transactions for the last six months message when ACC31 call succeed" +
      " but contains no dailyStatements" in new Setup {
      val cashTransactionsWithNoStatements: CashTransactions = CashTransactions(Seq(), Seq())

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(any, any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionsWithNoStatements)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      implicit val msgs: Messages = messages(app)

      running(app) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) must
          include regex msgs("cf.cash-account.transactions.no-transactions-for-last-six-months")
      }
    }

    "display cash account no transactions' page when user does not have Cash account and balance is zero" in new Setup {

      val app: Application = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector)
        ).build()

      implicit val msgs: Messages = messages(app)
      implicit val config: AppConfig = appConfig(app)

      val cashAccountWithZeroBalance: CashAccount = cashAccount.copy(balances = CDSCashBalance(Some(0)))

      val cashAccountNoTransactionsView: cash_account_no_transactions_v2 =
        app.injector.instanceOf[cash_account_no_transactions_v2]

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccountWithZeroBalance)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(any, any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      running(app) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          cashAccountNoTransactionsView(CashAccountViewModel(eori, cashAccountWithZeroBalance)).toString
      }

    }

    "display too many results page if the call to ACC31 returns 091-the query has exceeded threshold error" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CashAccountV2Controller.tooManyTransactions().url
      }
    }

    "display too many results page if maxTransactionsExceeded value is true" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse02)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        status(result) mustEqual OK
      }
    }

    "return Page Not Found if account does not exist" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "include a link to the cash transactions request page when feature switch is set to true" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
        val result = route(app, request).value

        contentAsString(result) must include regex "The CSV file will be available to download within 48 hours"
      }
    }

    "redirect to the cash account unavailable page" when {
      "ACC27 fails to fetch the user's account details" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any)).thenReturn(
          Future.failed(nonFatalResponse)
        )

        val app: Application = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.CashAccountV2Controller.showAccountUnavailable.url
        }
      }
    }

    "redirect to 'verify your email' page when an unverified email response is received" in new Setup {
      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Left(UnverifiedEmail)))

      val app: Application = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      private val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)

      running(app) {
        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.EmailController.showUnverified().url
      }
    }

    "redirect to 'Undelivered email' page when an undelivered email response is received" in new Setup {
      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Left(UndeliverableEmail("test@test.com"))))

      private val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)

      val app: Application = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      running(app) {
        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.EmailController.showUndeliverable().url
      }
    }

    "returns OK when an error occurs while retrieving the email" in new Setup {
      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.failed(new RuntimeException("Error occurred")))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      private val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)

      running(app) {
        val result = route(app, request).value

        status(result) mustBe OK
      }
    }
  }

  "showAccountUnavailable" must {
    "return OK" in new Setup {
      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.showAccountUnavailable.url)
        val result = route(app, request).value

        status(result) mustEqual OK
      }
    }
  }

  "showUnableToDownloadCSV" must {

    "return OK" in new Setup {
      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.showUnableToDownloadCSV().url)
        val result = route(app, request).value

        status(result) mustEqual OK
      }
    }
  }

  "tooManyTransactions" must {
    "return OK" in new Setup {

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      when(mockCustomsFinancialsApiConnector.getCashAccount(any)(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.tooManyTransactions().url)
        val result = route(app, request).value

        status(result) mustEqual OK
      }
    }

    "return NotFound" in new Setup {

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      when(mockCustomsFinancialsApiConnector.getCashAccount(any)(any, any))
        .thenReturn(Future.successful(None))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountV2Controller.tooManyTransactions().url)
        val result = route(app, request).value

        status(result) mustEqual NOT_FOUND
      }
    }
  }

  "onSubmit" must {

    "return SEE_OTHER when form submission is successful" in new Setup {
      val app: Application = application
        .overrides(bind[DeclarationDetailController].toInstance(mockDeclarationDetailController))
        .build()

      running(app) {
        val request = FakeRequest(POST, routes.CashAccountV2Controller.onSubmit(page).url)
          .withFormUrlEncodedBody("value" -> "GHRT122910DC537220")

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        val expectedRedirectUrl =
          routes.DeclarationDetailController.displaySearchDetails(page, "GHRT122910DC537220").url

        redirectLocation(result).value mustEqual expectedRedirectUrl
      }
    }

    "return same page when form submission is unsuccessful with error validation present" in new Setup {
      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector)).build()

      running(app) {
        val formWithErrors: Form[String] = new SearchTransactionsFormProvider()
          .apply()
          .bind(Map("value" -> emptyString))

        val view = app.injector.instanceOf[cash_account_v2]

        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        val viewModel = CashAccountV2ViewModel(
          pageTitle = emptyString,
          backLink = emptyString,
          cashAccountBalance = HtmlFormat.empty,
          cashStatementNotification = None,
          dailyStatementsSection = None,
          tooManyTransactionsSection = None,
          downloadCSVFileLinkUrl = HtmlFormat.empty,
          helpAndSupportGuidance = GuidanceRow(HtmlFormat.empty, None, None),
          paginationModel = None)

        val request = FakeRequest(POST, routes.CashAccountV2Controller.onSubmit(page).url)
          .withFormUrlEncodedBody("value" -> emptyString)

        val result = route(app, request).value
        status(result) mustEqual SEE_OTHER

        val renderedView = view.apply(formWithErrors, viewModel)(messages(app), appConfig(app), request).body
        renderedView must include("There is a problem")
        renderedView must include(s"Enter an MRN, UCR or exact payment amount that includes $poundSymbol")
      }
    }
  }

  trait Setup {

    val cashAccountNumber = "1234567"
    val eori = "exampleEori"
    val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"
    val page: Option[Int] = Some(1)

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]
    val mockDeclarationDetailController: DeclarationDetailController = mock[DeclarationDetailController]

    val cashAccount: CashAccount = CashAccount(cashAccountNumber, eori, AccountStatusOpen,
      CDSCashBalance(Some(BigDecimal(123456.78))))

    val listOfPendingTransactions: Seq[Declaration] =
      Seq(Declaration("pendingDeclarationID", Some("pendingImporterEORI"), "pendingDeclarantEORINumber",
        Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil, Some(sMRN)))

    val cashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 0.0, 1000.00,
        Seq(Declaration("mrn1", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
          LocalDate.parse("2020-07-18"), -84.00, Nil, Some(sMRN)),
          Declaration("mrn2", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
            LocalDate.parse("2020-07-18"), -65.00, Nil, Some(sMRN))),

        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))),

      CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 1200.00,
        Seq(Declaration("mrn3", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
          LocalDate.parse("2020-07-20"), -90.00, Nil, Some(sMRN)),
          Declaration("mrn4", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
            LocalDate.parse("2020-07-20"), -30.00, Nil, Some(sMRN))),
        Seq(Transaction(67.89, Payment, None))))

    val nonFatalResponse: UpstreamErrorResponse = UpstreamErrorResponse(
      "ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)

    val cashTransactionResponse: CashTransactions = CashTransactions(
      listOfPendingTransactions, cashDailyStatements)

    val cashTransactionResponse02: CashTransactions = CashTransactions(
      listOfPendingTransactions, cashDailyStatements, Some(true))
  }

  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def randomFloat: Float = Random.nextFloat()

  def randomLong: Long = Random.nextLong()

  def randomBigDecimal: BigDecimal = BigDecimal(randomFloat.toString)

  def randomLocalDate: LocalDate = {
    val monthsToSubtract: Int = 36
    LocalDate.now().minusMonths(Random.nextInt(monthsToSubtract))
  }

  def randomCashTransaction(howMany: Int): CashTransactions = {
    val pendingStatementsNumber: Int = 20
    CashTransactions(randomPendingDailyStatements(pendingStatementsNumber), randomCashDailyStatements(howMany))
  }

  def randomDeclaration: Declaration = {
    val randomStringLength: Int = 10

    Declaration(randomString(randomStringLength),
      Some(randomString(randomStringLength)),
      randomString(randomStringLength),
      Some(randomString(randomStringLength)),
      randomLocalDate,
      randomBigDecimal,
      Nil,
      Some("ic62zbad-75fa-445f-962b-cc92311686b8e"))
  }

  def randomCashDailyStatement: CashDailyStatement = {
    val declarations: Int = 5
    val otherTransactions: Int = 7

    CashDailyStatement(randomLocalDate,
      randomBigDecimal,
      randomBigDecimal,
      randomDeclarations(declarations),
      randomTransactions(otherTransactions))
  }

  val types: Seq[String] = Seq("Payment", "Withdrawal", "Transfer")

  def randomTransactions(howMany: Int): Seq[Transaction] = List.fill(howMany)(randomTransaction)

  def randomTransaction: Transaction = Transaction(randomBigDecimal, Transfer, None)

  def randomDeclarations(howMany: Int): Seq[Declaration] = List.fill(howMany)(randomDeclaration)

  def randomPendingDailyStatements(howMany: Int): Seq[Declaration] = List.fill(howMany)(randomDeclaration)

  def randomCashDailyStatements(howMany: Int): Seq[CashDailyStatement] = List.fill(howMany)(randomCashDailyStatement)
}
