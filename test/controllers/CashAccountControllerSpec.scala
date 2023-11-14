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
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException}
import models.{AccountStatusOpen, CashTransactions, _}
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditingService
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase
import views.html.{cash_account_no_transactions, cash_account_no_transactions_with_balance, cash_account_transactions_not_available}
import java.time.LocalDate
import scala.concurrent.Future
import scala.util.Random

class CashAccountControllerSpec extends SpecBase {

  "show account details" must {
    "return OK" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {

        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }

    "check view cash account details" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {

        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }

    "display transactions unavailable if the call to ACC31 fails" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view = app.injector.instanceOf[cash_account_transactions_not_available]
      val appConfig = app.injector.instanceOf[AppConfig]
      val messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest("", ""))

      running(app) {

        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CashAccountViewModel(eori, cashAccount), appConfig.transactionsTimeoutFlag)(request, messages, appConfig).toString()

      }
    }

    "display no transactions if the call to ACC31 returns no transactions with balance if balance is greater than 0" in new Setup {

      val newCashAccount = cashAccount.copy(balances = CDSCashBalance(Some(100)))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(newCashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))


      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view = app.injector.instanceOf[cash_account_no_transactions_with_balance]
      val appConfig = app.injector.instanceOf[AppConfig]
      val messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest("", ""))

      running(app) {

        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CashAccountViewModel(eori, newCashAccount))(request, messages, appConfig).toString()
        contentAsString(result) must include regex "search and download previous transactions as CSV."
      }
    }

    "display no transactions if the call to ACC31 returns no cashDailyStatements and balance is empty" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount.copy(balances = CDSCashBalance(None)))))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))


      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view = app.injector.instanceOf[cash_account_no_transactions]
      val appConfig = app.injector.instanceOf[AppConfig]
      val messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest("", ""))

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CashAccountViewModel(eori, cashAccount))(request, messages, appConfig).toString()
        contentAsString(result) must include regex "search and download previous transactions as CSV."
      }
    }

    "display no transactions if the call to ACC31 returns no cashDailyStatements and balance is 0" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount.copy(balances = CDSCashBalance(Some(0))))))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))


      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view = app.injector.instanceOf[cash_account_no_transactions]
      val appConfig = app.injector.instanceOf[AppConfig]
      val messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest("", ""))

      running(app) {

        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CashAccountViewModel(eori, cashAccount))(request, messages, appConfig).toString()
        contentAsString(result) must include regex "search and download previous transactions as CSV."
      }
    }

    "display balance and no transactions to ACC31 returns no cashDailyStatements or pending transactions" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(CashTransactions(Seq.empty, Seq.empty))))


      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      val view = app.injector.instanceOf[cash_account_no_transactions_with_balance]
      val appConfig = app.injector.instanceOf[AppConfig]
      val messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest("", ""))

      running(app) {

        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CashAccountViewModel(eori, cashAccount))(request, messages, appConfig).toString()
        contentAsString(result) must include regex "search and download previous transactions as CSV."
      }
    }

    "display too many results page if the call to ACC31 returns 091-the query has exceeded threshold error" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {

        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CashAccountController.tooManyTransactions().url
      }
    }

    "return Page Not Found if account does not exist" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        status(result) mustEqual NOT_FOUND
      }
    }
    "include a link to the cash transactions request page when feature switch is set to true" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
        val result = route(app, request).value
        contentAsString(result) must include regex "search and download previous transactions as CSV."
      }
    }

    "redirect to the cash account unavailable page" when {
      "ACC27 fails to fetch the user's account details" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any)).thenReturn(
          Future.failed(nonFatalResponse)
        )

        val app = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(Some(1)).url)
          val result = route(app, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.CashAccountController.showAccountUnavailable.url
        }
      }
    }
  }

  "showAccountUnavailable" must {
    "return OK" in new Setup {
      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.CashAccountController.showAccountUnavailable.url)
        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }
  }

  "showUnableToDownloadCSV" must {
    "return OK" in new Setup {
      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.showUnableToDownloadCSV().url)
        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }
  }

  trait Setup {

    val cashAccountNumber = "1234567"
    val eori = "exampleEori"
    val someCan = "1234567"

    val mockCustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockAuditingservice = mock[AuditingService]

    val cashAccount = CashAccount(cashAccountNumber, eori, AccountStatusOpen, CDSCashBalance(Some(BigDecimal(123456.78))))

    val listOfPendingTransactions =
      Seq(Declaration("pendingDeclarationID", Some("pendingImporterEORI"), "pendingDeclarantEORINumber",
        Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil))

    val fromDate = LocalDate.parse("2019-10-08")
    val toDate = LocalDate.parse("2020-04-08")

    val cashDailyStatements = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 0.0, 1000.00,
        Seq(Declaration("mrn1", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil),
          Declaration("mrn2", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -65.00, Nil)),
        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))),
      CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 1200.00,
        Seq(Declaration("mrn3", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil),
          Declaration("mrn4", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil)),
        Seq(Transaction(67.89, Payment, None))))

    val nonFatalResponse = UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)
    val cashTransactionResponse = CashTransactions(listOfPendingTransactions, cashDailyStatements)

  }

  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString
  def randomFloat: Float = Random.nextFloat()
  def randomLong: Long = Random.nextLong()
  def randomBigDecimal: BigDecimal = BigDecimal(randomFloat.toString)
  def randomLocalDate: LocalDate = LocalDate.now().minusMonths(Random.nextInt(36))

  def randomCashTransaction(howMany: Int): CashTransactions =
    CashTransactions(randomPendingDailyStatements(20), randomCashDailyStatements(howMany))

  def randomDeclaration: Declaration =
    Declaration(randomString(10),
      Some(randomString(10)),
      randomString(10),
      Some(randomString(10)),
      randomLocalDate,
      randomBigDecimal, Nil)

  def randomCashDailyStatement: CashDailyStatement =
    CashDailyStatement(randomLocalDate,
      randomBigDecimal,
      randomBigDecimal,
      randomDeclarations(5),
      randomTransactions(7))

  val types = Seq("Payment", "Withdrawal", "Transfer")

  def randomTransactions(howMany: Int): Seq[Transaction] = List.fill(howMany)(randomTransaction)
  def randomTransaction: Transaction = Transaction(randomBigDecimal, Transfer, None)
  def randomDeclarations(howMany: Int): Seq[Declaration] = List.fill(howMany)(randomDeclaration)
  def randomPendingDailyStatements(howMany: Int): Seq[Declaration] = List.fill(howMany)(randomDeclaration)
  def randomCashDailyStatements(howMany: Int): Seq[CashDailyStatement] = List.fill(howMany)(randomCashDailyStatement)
}
