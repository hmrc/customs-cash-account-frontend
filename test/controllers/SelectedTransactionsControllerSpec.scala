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
import connectors.*
import models.*
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class SelectedTransactionsControllerSpec extends SpecBase {

  "onPageLoad" must {

    "redirect to request page if no requested data found in cache" in new Setup {

      when(mockRequestedTransactionsCache.get(any))
        .thenReturn(Future.successful(None))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectedTransactionsController.onPageLoad().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SelectTransactionsController.onPageLoad().url
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
        fakeRequest(GET, routes.SelectedTransactionsController.onPageLoad().url)

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
        fakeRequest(GET, routes.SelectedTransactionsController.onPageLoad().url)

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
        fakeRequest(GET, routes.SelectedTransactionsController.onPageLoad().url)

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
        fakeRequest(GET, routes.SelectedTransactionsController.onPageLoad().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "when too many results returned for the search" in new Setup {

      val day = 30
      val month = 3
      val year = 2023

      when(mockRequestedTransactionsCache.get(any))
        .thenReturn(Future.successful(Some(CashTransactionDates(
          LocalDate.of(year, month, day), LocalDate.of(year, month, day)))))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectedTransactionsController.onPageLoad().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
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
        fakeRequest(GET, routes.SelectedTransactionsController.onPageLoad().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
      }
    }
  }

  "OnSubmit" must {

    "redirect to selected-confirmation page when cash account statement is submitted successfully" in new Setup {

      when(mockRequestedTransactionsCache.get(any))
        .thenReturn(Future.successful(Some(CashTransactionDates(LocalDate.now(), LocalDate.now()))))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.postCashAccountStatementRequest(any, any, any, any)(any))
        .thenReturn(Future.successful(Right(accountResCommon01)))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(POST, routes.SelectedTransactionsController.onSubmit().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ConfirmationPageController.onPageLoad().url)
      }
    }

    "redirect to requested too many transactions page " when {

      "cash account statement request responds with EXCEEDED_MAXIMUM in statusText" in new Setup {

        when(mockRequestedTransactionsCache.get(any))
          .thenReturn(Future.successful(Some(CashTransactionDates(LocalDate.now(), LocalDate.now()))))

        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.postCashAccountStatementRequest(any, any, any, any)(any))
          .thenReturn(Future.successful(Left(ExceededMaximum)))

        val request: FakeRequest[AnyContentAsEmpty.type] =
          fakeRequest(POST, routes.SelectedTransactionsController.onSubmit().url)

        running(app) {
          val result = route(app, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.SelectedTransactionsController.requestedTooManyTransactions().url)
        }
      }
    }

  }

  "requestedTooManyTransactions" must {

    "return too many transactions requested page if dates are available in cache" in new Setup {

      when(mockRequestedTransactionsCache.get(any))
        .thenReturn(Future.successful(Some(CashTransactionDates(fromDate, toDate))))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectedTransactionsController.requestedTooManyTransactions().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "redirect to showAccountDetails page if dates are unavailable in cache" in new Setup {

      when(mockRequestedTransactionsCache.get(any)).thenReturn(Future.successful(None))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectedTransactionsController.requestedTooManyTransactions().url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CashAccountController.showAccountDetails(None).url)
      }
    }
  }

  "duplicateDates" must {

    "return duplicate dates requested page if duplicate dates exist" in new Setup {

      when(mockRequestedTransactionsCache.get(any))
        .thenReturn(Future.successful(Some(CashTransactionDates(fromDate, toDate))))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.SelectedTransactionsController.duplicateDates(
          "someMsg","someDate","someDate").url)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SelectedTransactionsController.duplicateDates(
          "someMsg","someDate","someDate").url)
      }
    }
  }

  trait Setup {
    val sMRN: Option[String] = Some("ic62zbad-75fa-445f-962b-cc92311686b8e")
    val cashAccountNumber = "1234567"
    val eori = "exampleEori"
    val someCan = "1234567"
    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockRequestedTransactionsCache: RequestedTransactionsCache = mock[RequestedTransactionsCache]

    val cashAccount: CashAccount =
      CashAccount(cashAccountNumber, eori, AccountStatusOpen, CDSCashBalance(Some(BigDecimal(123456.78))))

    val listOfPendingTransactions: Seq[Declaration] =
      Seq(Declaration("pendingDeclarationID", Some("pendingImporterEORI"),
        "pendingDeclarantEORINumber", Some("pendingDeclarantReference"),
        LocalDate.parse("2020-07-21"), -100.00, Nil, sMRN))

    val fromDate: LocalDate = LocalDate.parse("2023-03-30")
    val toDate: LocalDate = LocalDate.parse("2023-03-30")

    val cashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 0.0, 1000.00,
        Seq(Declaration("mrn1", Some("Importer EORI"), "Declarant EORI",
          Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil, sMRN),
          Declaration("mrn2", Some("Importer EORI"), "Declarant EORI",
            Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -65.00, Nil, sMRN)),
        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))),

      CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 1200.00,
        Seq(Declaration("mrn3", Some("Importer EORI"), "Declarant EORI",
          Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil, sMRN),
          Declaration("mrn4", Some("Importer EORI"), "Declarant EORI",
            Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil, sMRN)),
        Seq(Transaction(67.89, Payment, None)))
    )

    val nonFatalResponse: UpstreamErrorResponse =
      UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)

    val cashTransactionResponse: CashTransactions =
      CashTransactions(listOfPendingTransactions, cashDailyStatements)

    val accountResCommon01: AccountResponseCommon = AccountResponseCommon(
      "OK", None, "2021-12-17T09:30:47Z", None)

    val accountResCommon02: AccountResponseCommon = AccountResponseCommon(
      "OK", Some("602-Exceeded maximum threshold of transactions"), "2021-12-17T09:30:47Z", None)

    val app: Application = application
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[RequestedTransactionsCache].toInstance(mockRequestedTransactionsCache)
      )
      .configure("features.fixed-systemdate-for-tests" -> "true")
      .build()

    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
