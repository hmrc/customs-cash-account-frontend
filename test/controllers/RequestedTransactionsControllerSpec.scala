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

import config.{AppConfig, ErrorHandler}
import connectors.{
  CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException
}
import controllers.actions.FakeIdentifierAction
import models.*
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, SystemMaterializer}
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import play.api.i18n.{Messages, MessagesApi}
import play.twirl.api.Html
import viewmodels.ResultsPageSummary
import views.html.{
  cash_account_transactions_not_available, cash_transactions_no_result, cash_transactions_result_page,
  cash_transactions_too_many_results
}

class RequestedTransactionsControllerSpec extends SpecBase {

  "redirect to request page if no requested data found in cache" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(None))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.RequestTransactionsController.onPageLoad().url
    }
  }

  "return status Ok when valid data has been submitted" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(CashTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(cashAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
      .thenReturn(Future.successful(Right(cashTransactionResponse)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK
    }
  }

  "return No Transactions view when no data is returned for the search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(CashTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(cashAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
      .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK
      contentAsString(result) must include regex "No cash account transactions"
    }
  }

  "return Exceeded Threshold view when too many results returned for the search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(CashTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(cashAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
      .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
    }
  }

  "return transaction unavailable for internal server error during search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(CashTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(cashAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
      .thenReturn(Future.successful(Left(UnknownException)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK
    }
  }

  "when too many results returned for the search" in new Setup {
    val day   = 30
    val month = 3
    val year  = 2023

    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(
        Future.successful(Some(CashTransactionDates(LocalDate.of(year, month, day), LocalDate.of(year, month, day))))
      )

    when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(cashAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
      .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual
        routes.RequestedTransactionsController.tooManyTransactionsRequested(RequestedDateRange(fromDate, toDate)).url
    }
  }

  "redirect to account unavailable page when exception is thrown" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(CashTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(cashAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
      .thenThrow(new RuntimeException())

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
    }
  }

  "tooManyTransactionsSelected" must {

    "return OK when called with a valid date range" in new Setup {

      val dateRange: RequestedDateRange = RequestedDateRange(fromDate, toDate)

      val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(GET, "/some-path")

      implicit val messages: Messages =
        app.injector.instanceOf[MessagesApi].preferred(request)

      when(mockTooManyResultsView.apply(any[ResultsPageSummary], any[String])(any[Request[_]], any[Messages], any))
        .thenReturn(Html("Too many transactions"))

      val result: Future[Result] = controller.tooManyTransactionsRequested(dateRange)(request)

      status(result) mustBe OK
      contentAsString(result) must include("Too many transactions")
    }
  }

  trait Setup {
    val cashAccountNumber = "1234567"
    val eori              = "exampleEori"
    val sMRN              = "ic62zbad-75fa-445f-962b-cc92311686b8e"

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockRequestedTransactionsCache: RequestedTransactionsCache       = mock[RequestedTransactionsCache]
    val mockTooManyResultsView: cash_transactions_too_many_results       = mock[cash_transactions_too_many_results]

    val mockErrorHandler: ErrorHandler    = mock[ErrorHandler]
    val mcc: MessagesControllerComponents = stubMessagesControllerComponents()

    implicit val actorSystem: ActorSystem   = ActorSystem("test")
    implicit val materializer: Materializer = SystemMaterializer(actorSystem).materializer
    val fakeIdentify                        = new FakeIdentifierAction(stubPlayBodyParsers())

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    val controller = new RequestedTransactionsController(
      resultView = mock[cash_transactions_result_page],
      apiConnector = mockCustomsFinancialsApiConnector,
      transactionsUnavailable = mock[cash_account_transactions_not_available],
      tooManyResults = mockTooManyResultsView,
      noResults = mock[cash_transactions_no_result],
      identify = fakeIdentify,
      eh = mockErrorHandler,
      cache = mockRequestedTransactionsCache,
      mcc = mcc
    )

    val cashAccount: CashAccount =
      CashAccount(cashAccountNumber, eori, AccountStatusOpen, CDSCashBalance(Some(BigDecimal(123456.78))))

    val listOfPendingTransactions: Seq[Declaration] =
      Seq(
        Declaration(
          "pendingDeclarationID",
          Some("pendingImporterEORI"),
          "pendingDeclarantEORINumber",
          Some("pendingDeclarantReference"),
          LocalDate.parse("2020-07-21"),
          -100.00,
          Nil,
          Some(sMRN)
        )
      )

    val fromDate: LocalDate = LocalDate.parse("2023-03-30")
    val toDate: LocalDate   = LocalDate.parse("2023-03-30")

    val cashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(
        LocalDate.parse("2020-07-18"),
        0.0,
        1000.00,
        Seq(
          Declaration(
            "mrn1",
            Some("Importer EORI"),
            "Declarant EORI",
            Some("Declarant Reference"),
            LocalDate.parse("2020-07-18"),
            -84.00,
            Nil,
            Some(sMRN)
          ),
          Declaration(
            "mrn2",
            Some("Importer EORI"),
            "Declarant EORI",
            Some("Declarant Reference"),
            LocalDate.parse("2020-07-18"),
            -65.00,
            Nil,
            Some(sMRN)
          )
        ),
        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))
      ),
      CashDailyStatement(
        LocalDate.parse("2020-07-20"),
        0.0,
        1200.00,
        Seq(
          Declaration(
            "mrn3",
            Some("Importer EORI"),
            "Declarant EORI",
            Some("Declarant Reference"),
            LocalDate.parse("2020-07-20"),
            -90.00,
            Nil,
            Some(sMRN)
          ),
          Declaration(
            "mrn4",
            Some("Importer EORI"),
            "Declarant EORI",
            Some("Declarant Reference"),
            LocalDate.parse("2020-07-20"),
            -30.00,
            Nil,
            Some(sMRN)
          )
        ),
        Seq(Transaction(67.89, Payment, None))
      )
    )

    val nonFatalResponse: UpstreamErrorResponse =
      UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)

    val cashTransactionResponse: CashTransactions =
      CashTransactions(listOfPendingTransactions, cashDailyStatements)

    val app: Application = applicationBuilder
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[RequestedTransactionsCache].toInstance(mockRequestedTransactionsCache)
      )
      .configure("features.fixed-systemdate-for-tests" -> "true")
      .build()

    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
