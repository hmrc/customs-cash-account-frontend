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

package connectors

import config.AppConfig
import models.*
import models.email.{EmailUnverifiedResponse, EmailVerifiedResponse}
import models.request.{CashDailyStatementRequest, IdentifierRequest}
import org.mockito.ArgumentMatchers.anyString
import play.api.{Application, inject}
import play.api.http.Status.{NOT_FOUND, REQUEST_ENTITY_TOO_LARGE}
import play.api.inject.bind
import play.api.test.Helpers.*
import repositories.CacheRepository
import services.MetricsReporterService
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpReads, InternalServerException, SessionId, UpstreamErrorResponse}
import utils.SpecBase
import java.net.URL

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.eq as eqTo
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

class CustomsFinancialsApiConnectorSpec extends SpecBase {

  "getAccounts" must {

    "return all accounts available to the given EORI from the API service" in new Setup {

      /*when[Future[AccountsAndBalancesResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(traderAccounts))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[AccountsAndBalancesResponseContainer]], any[ExecutionContext]))
        .thenReturn(Future.successful(traderAccounts))
      when(mockHttpClient.post(any[URL]())(any)).thenReturn(requestBuilder)

      running(appWithHttpClient) {
        val result = await(connector().getCashAccount(eori)(implicitly, IdentifierRequest(fakeRequest(), "12345678")))
        result.value mustEqual cashAccount
      }
    }

    "log response time metric" in new Setup {
      /*when[Future[AccountsAndBalancesResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(traderAccounts))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[AccountsAndBalancesResponseContainer]], any[ExecutionContext]))
        .thenReturn(Future.successful(traderAccounts))
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)

      when[Future[Seq[CashAccount]]](mockMetricsReporterService.withResponseTimeLogging(any)(any)(any))
        .thenReturn(Future.successful(Seq(cashAccount)))

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[MetricsReporterService].toInstance(mockMetricsReporterService)
        ).build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).getCashAccount(eori)(implicitly,
          IdentifierRequest(fakeRequest(), "12345678")))

        result.value mustEqual cashAccount

        verify(mockMetricsReporterService).withResponseTimeLogging(
          eqTo("customs-financials-api.get.accounts"))(any)(any)
      }
    }
  }

  "retrieveCashTransactions" must {
    "call the correct URL and pass through the HeaderCarrier and CAN, " +
      "and return a list of cash daily statements" in new Setup {

      val expectedUrl = "apiEndpointUrl/account/cash/transactions"
      private val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      /*when[Future[CashTransactions]](mockHttpClient.POST(eqTo(expectedUrl),eqTo(cashDailyStatementRequest),any)(any, any, eqTo(hc), any))
        .thenReturn(Future.successful(successResponse))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
        .thenReturn(Future.successful(successResponse))
      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      when(mockCacheRepository.get("can")).thenReturn(Future.successful(None))
      when(mockCacheRepository.set("can", successResponse)).thenReturn(Future.successful(true))

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder),
          bind[MetricsReporterService].toInstance(mockMetricsReporterService),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate))
        result mustBe Right(successResponse)
      }
    }

    "log the error when failed to store data in the session cache call after getting response from the API " +
      "call" in new Setup {

      val expectedUrl = "apiEndpointUrl/account/cash/transactions"
      private val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      when(mockConfig.customsFinancialsApi).thenReturn("apiEndpointUrl")

      /*when[Future[CashTransactions]](mockHttpClient.POST(eqTo(expectedUrl),eqTo(cashDailyStatementRequest),any)(any, any, eqTo(hc), any))
        .thenReturn(Future.successful(successResponse))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
        .thenReturn(Future.successful(successResponse))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      when(mockCacheRepository.get("can")).thenReturn(Future.successful(None))
      when(mockCacheRepository.set("can", successResponse)).thenReturn(Future.successful(false))

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[MetricsReporterService].toInstance(mockMetricsReporterService),
          bind[AppConfig].toInstance(mockConfig),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate).map {
          _ mustBe Right(successResponse)
        }
      }
    }

    "call the correct URL and pass through the HeaderCarrier and CAN, " +
      "and return a list of cash daily statements from the cacheRepository" in new Setup {
      val successResponse: CashTransactions = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      when(mockConfig.customsFinancialsApi).thenReturn("apiEndpointUrl")

      when(mockCacheRepository.get(anyString)).thenReturn(Future.successful(Some(successResponse)))

      val appWithMocks: Application = application
        .overrides(
          bind[AppConfig].toInstance(mockConfig),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate))
        result mustBe Right(successResponse)
      }
    }

    "propagate exceptions when the backend POST fails" in new Setup {

      private val responseCode: Int = 500

      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new HttpException("It's broken", responseCode)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(new HttpException("It's broken", responseCode)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      when(mockCacheRepository.get("can")).thenReturn(Future.successful(None))

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate))
        result mustBe Left(UnknownException)
      }
    }


    "return ErrorResponse when the backend POST fails with REQUEST_ENTITY_TOO_LARGE" in new Setup {
      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))

      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", REQUEST_ENTITY_TOO_LARGE)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", REQUEST_ENTITY_TOO_LARGE)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate).map {
          _ mustBe Left(TooManyTransactionsRequested)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with NOT_FOUND" in new Setup {
      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))

      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[CacheRepository].toInstance(mockCacheRepository)
        ).build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate).map {
          _ mustBe Left(NoTransactionsAvailable)
        }
      }
    }
  }

  "retrieveCashTransactionsDetail" must {
    "call the correct URL and pass through the HeaderCarrier and CAN," +
      " and return a list of cash daily statements" in new Setup {

      val expectedUrl = "apiEndpointUrl/account/cash/transactions-detail"
      private val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      /*when[Future[CashTransactions]](mockHttpClient.POST(
        eqTo(expectedUrl),
        eqTo(cashDailyStatementRequest),
        any)(any, any, eqTo(hc), any))
        .thenReturn(Future.successful(successResponse))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
        .thenReturn(Future.successful(successResponse))
      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        ).build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate))
        result mustBe Right(successResponse)
      }
    }

    "propagate exceptions when the backend POST fails" in new Setup {
      private val responseCode: Int = 500
      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new HttpException("It's broken", responseCode)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(new HttpException("It's broken", responseCode)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        ).build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(UnknownException)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with REQUEST_ENTITY_TOO_LARGE" in new Setup {

      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", REQUEST_ENTITY_TOO_LARGE)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", REQUEST_ENTITY_TOO_LARGE)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        ).build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(TooManyTransactionsRequested)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with NOT_FOUND" in new Setup {

      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        ).build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(NoTransactionsAvailable)
        }
      }
    }
  }

  "retrieveHistoricCashTransactions" must {
    "return a list of requested cash daily statements" in new Setup {
      val expectedUrl = "apiEndpointUrl/account/cash/transactions"
      private val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      /*when[Future[CashTransactions]](mockHttpClient.POST(
        eqTo(expectedUrl),
        eqTo(cashDailyStatementRequest),
        any)(any, any, eqTo(hc), any))
        .thenReturn(Future.successful(successResponse))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
        .thenReturn(Future.successful(successResponse))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = application
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        ).build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).retrieveHistoricCashTransactions("can", fromDate, toDate))
        result mustBe Right(successResponse)
      }
    }

    "propagate exceptions when the backend POST fails" in new Setup {
      private val responseCode: Int = 500
      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new HttpException("It's broken", responseCode)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(new HttpException("It's broken", responseCode)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      running(appWithHttpClient) {
        val result = await(connector().retrieveHistoricCashTransactions("can", fromDate, toDate))
        result mustBe Left(UnknownException)
      }
    }

    "return ErrorResponse when the backend POST fails with REQUEST_ENTITY_TOO_LARGE" in new Setup {

      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", REQUEST_ENTITY_TOO_LARGE)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", REQUEST_ENTITY_TOO_LARGE)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      running(appWithHttpClient) {
        connector().retrieveHistoricCashTransactions("can", fromDate, toDate).map {
          _ mustBe Left(TooManyTransactionsRequested)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with NOT_FOUND" in new Setup {
      /*when[Future[Seq[CashDailyStatement]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))*/

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      running(appWithHttpClient) {
        connector().retrieveHistoricCashTransactions("can", fromDate, toDate).map {
          _ mustBe Left(NoTransactionsAvailable)
        }
      }
    }
  }

  "retrieveUnverifiedEmail" must {
    "return EmailUnverifiedResponse with unverified email value" in new Setup {

      /*when(mockHttpClient.GET[EmailUnverifiedResponse](eqTo(customFinancialsApiUrl), any, any)(any, any, any))
        .thenReturn(Future.successful(emailUnverifiedRes))*/

      when(requestBuilder.execute(any[HttpReads[EmailUnverifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailUnverifiedRes))
      when(mockHttpClient.get(any[URL]())(any())).thenReturn(requestBuilder)

      connector().retrieveUnverifiedEmail.map {
        _ mustBe emailUnverifiedRes
      }
    }

    "return EmailUnverifiedResponse with None for unverified email if there is an error while" +
      " fetching response from api" in new Setup {

      /*when(mockHttpClient.GET[EmailUnverifiedResponse](
        eqTo(customFinancialsApiUrl), any, any)(any, any, any))
        .thenReturn(Future.failed(new RuntimeException("error occurred")))*/

      when(requestBuilder.execute(any[HttpReads[EmailUnverifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("error occurred")))
      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      connector().retrieveUnverifiedEmail.map {
        _.unVerifiedEmail mustBe empty
      }
    }
  }

  "verifiedEmail" must {
    "return verified email when email-display api call is successful" in new Setup {

      /*when(mockHttpClient.GET[EmailVerifiedResponse](any, any, any)(any, any, any))
        .thenReturn(Future.successful(emailVerifiedRes))*/

      when(requestBuilder.execute(any[HttpReads[EmailVerifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailVerifiedRes))
      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      connector().verifiedEmail.map {
        _ mustBe emailVerifiedRes
      }
    }

    "return none for verified email when exception occurs while calling email-display api" in new Setup {

      /*when(mockHttpClient.GET[EmailVerifiedResponse](any, any, any)(any, any, any))
        .thenReturn(Future.failed(new InternalServerException("error occurred")))*/

      when(requestBuilder.execute(any[HttpReads[EmailVerifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new InternalServerException("error occurred")))
      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      connector().verifiedEmail.map {
        _.verifiedEmail mustBe empty
      }
    }
  }

  trait Setup {
    private val traderEori = "12345678"
    private val cashAccountNumber = "987654"

    val sessionId: SessionId = SessionId("session_1234")
    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))

    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockConfig: AppConfig = mock[AppConfig]
    val mockCacheRepository: CacheRepository = mock[CacheRepository]

    val cdsCashAccount: CdsCashAccount = CdsCashAccount(
      Account(cashAccountNumber, emptyString, traderEori, Some(AccountStatusOpen), false, Some(false)),
      Some("999.99"))

    val cashAccount: CashAccount = cdsCashAccount.toDomain

    val fromDate: LocalDate = LocalDate.parse("2019-10-08")
    val toDate: LocalDate = LocalDate.parse("2020-04-08")
    val eori = "123456789"

    val traderAccounts: AccountsAndBalancesResponseContainer = AccountsAndBalancesResponseContainer(
      AccountsAndBalancesResponse(
        Some(AccountResponseCommon(emptyString, Some(emptyString), emptyString, None)),
        AccountResponseDetail(
          Some("987654"),
          None,
          Some(Seq(cdsCashAccount))
        )
      )
    )

    val listOfPendingTransactions: Seq[Declaration] = Seq(
      Declaration("pendingDeclarationID",
        Some("pendingImporterEORI"),
        "pendingDeclarantEORINumber",
        Some("pendingDeclarantReference"),
        LocalDate.parse("2020-07-21"),
        -100.00,
        Nil)
    )

    val cashDailyStatementRequest: CashDailyStatementRequest = CashDailyStatementRequest("can", fromDate, toDate)

    private val otherTransactions =
      Seq(Transaction(123.45, Payment, None), Transaction(-432.87, Withdrawal, Some("77665544")))

    val listOfCashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(
        LocalDate.parse("2020-07-18"),
        500.0,
        1000.00,
        Seq(Declaration("mrn1", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
          LocalDate.parse("2020-07-18"), -84.00, Nil),
          Declaration("mrn2", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
            LocalDate.parse("2020-07-18"), -65.00, Nil)),
        otherTransactions),

      CashDailyStatement(LocalDate.parse("2020-07-20"), 600.0, 1200.00,
        Seq(Declaration("mrn3", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
          LocalDate.parse("2020-07-20"), -90.00, Nil),
          Declaration("mrn4", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
            LocalDate.parse("2020-07-20"), -30.00, Nil)), Nil)
    )

    val emailId = "test@test.com"
    val emailUnverifiedRes: EmailUnverifiedResponse = EmailUnverifiedResponse(Some(emailId))
    val emailVerifiedRes: EmailVerifiedResponse = EmailVerifiedResponse(Some(emailId))

    val customFinancialsApiUrl = "http://localhost:9878/customs-financials-api/subscriptions/unverified-email-display"
    val verifyEmailApiUrl = "http://localhost:9878/customs-financials-api/subscriptions/email-display"

    val appWithHttpClient: Application = application
      .overrides(
        bind[HttpClientV2].toInstance(mockHttpClient),
        bind[RequestBuilder].toInstance(requestBuilder)
      ).build()

    def connector(app: Application = appWithHttpClient): CustomsFinancialsApiConnector =
      app.injector.instanceOf[CustomsFinancialsApiConnector]
  }
}
