/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.inject.bind
import play.api.test.Helpers._
import services.AuditingService
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class CashTransactionsRequestPageControllerSpec extends SpecBase {

  "onPageLoad" should {
    "return OK " in new Setup {
      val request = fakeRequest(GET, routes.CashTransactionsRequestPageController.onPageLoad().url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

  }

  "onSubmit" should {
    "return status Ok when valid data has been submitted" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return No Transactions view when no data is returned for the search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) must include regex "No cash account transactions"
      }
    }

    "return Exceeded Threshold view when too many results returned for the search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) must include regex "Your search returned too many results"
      }
    }

    "return transaction unavailable for internal server error during search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) must include regex "We are unable to show your payments at the moment. Please try again later."
      }
    }

    "redirect to account unavailable page when exception is thrown" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveHistoricCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenThrow(new RuntimeException())

      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
      }
    }

    "return BAD_REQUEST when the start date is earlier than system start date" in new Setup {
      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "9", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is earlier than system start date" in new Setup {
      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "9", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is future date" in new Setup {
      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2021", "end.month" -> "9", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is future date" in new Setup {
      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2021")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is after the end date" in new Setup {
      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "11", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the requested data exceeds 6 years in the past" in new Setup {
      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2000", "end.month" -> "10", "end.year" -> "2000")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when invalid data submitted" in new Setup {
      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.invalid" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when start date and end date are empty" in new Setup {
      val request = fakeRequest(POST, routes.CashTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "", "start.year" -> "2019", "end.month" -> "", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  trait Setup {
    val mockAuditingservice = mock[AuditingService]
    val cashAccountNumber = "1234567"
    val eori = "exampleEori"
    val someCan = "1234567"
    val mockCustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val cashAccount = CashAccount(cashAccountNumber, eori, AccountStatusOpen, CDSCashBalance(Some(BigDecimal(123456.78))))
    val listOfPendingTransactions =
      Seq(Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil))

    val fromDate = LocalDate.parse("2019-10-08")
    val toDate = LocalDate.parse("2020-04-08")

    val cashDailyStatements = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 0.0, 1000.00,
        Seq(Declaration("mrn1", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil),
          Declaration("mrn2", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -65.00, Nil)),
        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))),
      CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 1200.00,
        Seq(Declaration("mrn3", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil),
          Declaration("mrn4", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil)),
        Seq(Transaction(67.89, Payment, None))))

    val nonFatalResponse = UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)
    val cashTransactionResponse = CashTransactions(listOfPendingTransactions, cashDailyStatements)

    val app = application
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[AuditingService].toInstance(mockAuditingservice)
      )
      .configure("features.cash-account-details" -> "true",
        "features.cash-download-transactions" -> "true",
        "features.fixed-systemdate-for-tests" -> "true")
      .build()

    val appConfig = app.injector.instanceOf[AppConfig]
  }
}


