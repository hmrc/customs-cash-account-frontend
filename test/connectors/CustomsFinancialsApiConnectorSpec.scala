/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import java.time.LocalDate
import config.AppConfig
import models.{CashAccount, _}
import models.request.{CashDailyStatementRequest, IdentifierRequest}
import org.mockito.ArgumentMatchers.anyString
import play.api.inject.bind
import play.api.test.Helpers._
import repositories.CacheRepository
import services.MetricsReporterService
import uk.gov.hmrc.http.{SessionId, HeaderCarrier, HttpClient, HttpException}
import utils.SpecBase

import scala.concurrent.Future

class CustomsFinancialsApiConnectorSpec extends SpecBase {


  "getAccounts" must {

    "return all accounts available to the given EORI from the API service" in new Setup {
      when[Future[AccountsAndBalancesResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(traderAccounts))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.getCashAccount(eori)(implicitly, IdentifierRequest(fakeRequest(), "12345678")))
        result.value mustEqual cashAccount
      }
    }

    "log response time metric" in new Setup {
      when[Future[AccountsAndBalancesResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(traderAccounts))

      val mockMetricsReporterService = mock[MetricsReporterService]
      when[Future[Seq[CashAccount]]](mockMetricsReporterService.withResponseTimeLogging(any)(any)(any))
        .thenReturn(Future.successful(Seq(cashAccount)))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient),
          bind[MetricsReporterService].toInstance(mockMetricsReporterService)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.getCashAccount(eori)(implicitly, IdentifierRequest(fakeRequest(), "12345678")))
        result.value mustEqual cashAccount
        verify(mockMetricsReporterService).withResponseTimeLogging(eqTo("customs-financials-api.get.accounts"))(any)(any)
      }
    }
  }

  "retrieveCashTransactions" must {
    "call the correct URL and pass through the HeaderCarrier and CAN, and return a list of cash daily statements" in new Setup {
      val expectedUrl = "apiEndpointUrl/account/cash/transactions"
      val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      val mockConfig = mock[AppConfig]
      val mockCacheRepository = mock[CacheRepository]

      when(mockConfig.customsFinancialsApi).thenReturn("apiEndpointUrl")

      when[Future[CashTransactions]](mockHttpClient.POST(eqTo(expectedUrl), eqTo(cashDailyStatementRequest), any)(any, any, eqTo(hc), any))
        .thenReturn(Future.successful(successResponse))
      when(mockCacheRepository.get("can")).thenReturn(Future.successful(None))
      when(mockCacheRepository.set("can", successResponse)).thenReturn(Future.successful(true))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient),
          bind[AppConfig].toInstance(mockConfig),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveCashTransactions("can", fromDate, toDate))
        result mustEqual Right(successResponse)
      }
    }

    "call the correct URL and pass through the HeaderCarrier and CAN, and return a list of cash daily statements from the cacheRepository" in new Setup {
      val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      val mockConfig = mock[AppConfig]
      val mockCacheRepository = mock[CacheRepository]

      when(mockConfig.customsFinancialsApi).thenReturn("apiEndpointUrl")

      when(mockCacheRepository.get(anyString)).thenReturn(Future.successful(Some(successResponse)))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient),
          bind[AppConfig].toInstance(mockConfig),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveCashTransactions("can", fromDate, toDate))
        result mustEqual Right(successResponse)
      }
    }

    "propagate exceptions when the backend POST fails" in new Setup {
      val mockCacheRepository = mock[CacheRepository]
      when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new HttpException("It's broken", 500)))
      when(mockCacheRepository.get("can")).thenReturn(Future.successful(None))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveCashTransactions("can", fromDate, toDate))
        result mustEqual Left(UnknownException)
      }
    }
  }

  "retrieveCashTransactionsDetail" must {
    "call the correct URL and pass through the HeaderCarrier and CAN, and return a list of cash daily statements" in new Setup {
      val expectedUrl = "apiEndpointUrl/account/cash/transactions-detail"
      val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      val mockConfig = mock[AppConfig]

      when(mockConfig.customsFinancialsApi).thenReturn("apiEndpointUrl")

      when[Future[CashTransactions]](mockHttpClient.POST(eqTo(expectedUrl), eqTo(cashDailyStatementRequest), any)(any, any, eqTo(hc), any))
        .thenReturn(Future.successful(successResponse))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient),
          bind[AppConfig].toInstance(mockConfig)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveCashTransactionsDetail("can", fromDate, toDate))
        result mustEqual Right(successResponse)
      }
    }

    "propagate exceptions when the backend POST fails" in new Setup {
      when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new HttpException("It's broken", 500)))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveCashTransactions("can", fromDate, toDate))
        result mustEqual Left(UnknownException)
      }
    }
  }

  "retrieveHistoricCashTransactions" must {
    "return a list of requested cash daily statements" in new Setup {
      val expectedUrl = "apiEndpointUrl/account/cash/transactions"
      val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      val mockConfig = mock[AppConfig]

      when(mockConfig.customsFinancialsApi).thenReturn("apiEndpointUrl")

      when[Future[CashTransactions]](mockHttpClient.POST(eqTo(expectedUrl), eqTo(cashDailyStatementRequest), any)(any, any, eqTo(hc), any))
        .thenReturn(Future.successful(successResponse))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient),
          bind[AppConfig].toInstance(mockConfig)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveHistoricCashTransactions("can", fromDate, toDate))
        result mustEqual Right(successResponse)
      }
    }

    "propagate exceptions when the backend POST fails" in new Setup {
      when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new HttpException("It's broken", 500)))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveHistoricCashTransactions("can", fromDate, toDate))
        result mustEqual Left(UnknownException)
      }
    }
  }

  trait Setup {
    private val traderEori = "12345678"
    private val cashAccountNumber = "987654"
    val sessionId = SessionId("session_1234")
    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))
    val mockHttpClient = mock[HttpClient]

    val cdsCashAccount = CdsCashAccount(Account(cashAccountNumber, "", traderEori, Some(AccountStatusOpen), false, Some(false)), Some("999.99"))
    val cashAccount = cdsCashAccount.toDomain()

    val fromDate = LocalDate.parse("2019-10-08")
    val toDate = LocalDate.parse("2020-04-08")

    val eori = "123456789"
    val traderAccounts = AccountsAndBalancesResponseContainer(
      AccountsAndBalancesResponse(
        Some(AccountResponseCommon("", Some(""), "", None)),
        AccountResponseDetail(
          Some("987654"),
          None,
          Some(Seq(cdsCashAccount))
        )
      )
    )

    val listOfPendingTransactions =
      Seq(Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil))

    val cashDailyStatementRequest = CashDailyStatementRequest("can", fromDate, toDate)
    val listOfCashDailyStatements = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 500.0, 1000.00,
        Seq(Declaration("mrn1", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil),
          Declaration("mrn2", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -65.00, Nil)),
        Seq(Transaction(123.45, Payment, None),
          Transaction(-432.87, Withdrawal, Some("77665544")))),
      CashDailyStatement(LocalDate.parse("2020-07-20"), 600.0, 1200.00,
        Seq(Declaration("mrn3", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil),
          Declaration("mrn4", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil)),
        Nil)
    )
  }
}
