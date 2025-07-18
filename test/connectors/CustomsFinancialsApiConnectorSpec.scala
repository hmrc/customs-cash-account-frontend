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
import models._
import models.request.{CashAccountStatementRequestDetail, CashDailyStatementRequest, IdentifierRequest, SearchType}
import models.response.{CashAccountTransactionSearchResponseDetail, EoriData, EoriDataContainer}
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.{Application, inject}
import repositories.{CacheRepository, CashAccountSearchRepository}
import services.MetricsReporterService
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpReads, HttpResponse, SessionId, UpstreamErrorResponse}
import utils.{EtmpErrorCode, SpecBase}

import java.net.URL
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CustomsFinancialsApiConnectorSpec extends SpecBase {

  "getAccounts" must {

    "return all accounts available to the given EORI from the API service" in new Setup {
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
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[AccountsAndBalancesResponseContainer]], any[ExecutionContext]))
        .thenReturn(Future.successful(traderAccounts))
      when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)

      when[Future[Seq[CashAccount]]](mockMetricsReporterService.withResponseTimeLogging(any)(any)(any))
        .thenReturn(Future.successful(Seq(cashAccount)))

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[MetricsReporterService].toInstance(mockMetricsReporterService)
        )
        .build()

      running(appWithMocks) {
        val result =
          await(connector(appWithMocks).getCashAccount(eori)(implicitly, IdentifierRequest(fakeRequest(), "12345678")))

        result.value mustEqual cashAccount

        verify(mockMetricsReporterService).withResponseTimeLogging(eqTo("customs-financials-api.get.accounts"))(any)(
          any
        )
      }
    }
  }

  "retrieveCashTransactions" must {
    "call the correct URL and pass through the HeaderCarrier and CAN, " +
      "return a list of cash daily statements while adding a UUID, and checking cache state" in new Setup {

        private val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

        when(mockCacheRepository.get(any[String])).thenReturn(Future.successful(None))
        when(mockCacheRepository.set(any[String], any[CashTransactions])).thenReturn(Future.successful(true))
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
          .thenReturn(Future.successful(successResponse))
        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient),
            bind[CacheRepository].toInstance(mockCacheRepository),
            bind[RequestBuilder].toInstance(requestBuilder)
          )
          .build()

        running(appWithMocks) {
          val result = await(connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate))

          result match {
            case Right(actualTransactions) =>
              verify(mockCacheRepository).set("can", actualTransactions)

              actualTransactions.cashDailyStatements.zip(successResponse.cashDailyStatements).foreach {
                case (actualStatement, expectedStatement) =>
                  actualStatement.declarations.zip(expectedStatement.declarations).foreach {
                    case (actualDeclaration, expectedDeclaration) =>
                      actualDeclaration.secureMovementReferenceNumber must not be empty

                      actualDeclaration.copy(secureMovementReferenceNumber = None) mustEqual
                        expectedDeclaration.copy(secureMovementReferenceNumber = None)
                  }
              }

            case Left(error) => fail(s"Expected successful result but got $error")
          }
        }
      }

    "log the error when failed to store data in the session cache call after getting response from the API " +
      "call" in new Setup {

        private val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

        when(mockConfig.customsFinancialsApi).thenReturn("apiEndpointUrl")

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
          .thenReturn(Future.successful(successResponse))
        when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

        when(mockCacheRepository.get("can")).thenReturn(Future.successful(None))
        when(mockCacheRepository.set("can", successResponse)).thenReturn(Future.successful(false))

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient),
            bind[MetricsReporterService].toInstance(mockMetricsReporterService),
            bind[AppConfig].toInstance(mockConfig),
            bind[CacheRepository].toInstance(mockCacheRepository)
          )
          .build()

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

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[AppConfig].toInstance(mockConfig),
            bind[CacheRepository].toInstance(mockCacheRepository)
          )
          .build()

        running(appWithMocks) {
          val result = await(connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate))
          result mustBe Right(successResponse)
        }
      }

    "propagate exceptions when the backend POST fails" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(new HttpException("It's broken", INTERNAL_SERVER_ERROR)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      when(mockCacheRepository.get("can")).thenReturn(Future.successful(None))

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[CacheRepository].toInstance(mockCacheRepository)
        )
        .build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate))
        result mustBe Left(UnknownException)
      }
    }

    "return ErrorResponse when the backend POST fails with REQUEST_ENTITY_TOO_LARGE" in new Setup {
      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", REQUEST_ENTITY_TOO_LARGE)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[CacheRepository].toInstance(mockCacheRepository)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate).map {
          _ mustBe Left(TooManyTransactionsRequested)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with NOT_FOUND" in new Setup {
      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[CacheRepository].toInstance(mockCacheRepository)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactions("can", fromDate, toDate).map {
          _ mustBe Left(NoTransactionsAvailable)
        }
      }
    }
  }

  "retrieveCashTransactionsBySearch" must {

    "return valid response from api for the given request" when {

      "cache repository returns None for search id" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(mockCashAccountSearchRepository.get(any[String])).thenReturn(Future.successful(None))

        when(mockCashAccountSearchRepository.set(any[String], any[CashAccountTransactionSearchResponseDetail]))
          .thenReturn(Future.successful(true))

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(Future.successful(HttpResponse(OK, Json.toJson(transactionSearchResponseDetail).toString)))

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient),
            bind[RequestBuilder].toInstance(requestBuilder),
            bind[CashAccountSearchRepository].toInstance(mockCashAccountSearchRepository)
          )
          .build()

        running(appWithMocks) {
          val result = await(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          )

          result mustBe Right(transactionSearchResponseDetail)
          verify(mockCashAccountSearchRepository, times(1)).get(any[String])
          verify(mockCashAccountSearchRepository, times(1))
            .set(any[String], any[CashAccountTransactionSearchResponseDetail])
        }
      }
    }

    "return valid response from cache for the given request" when {

      "cache repository returns valid data for search id" in new Setup {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(mockCashAccountSearchRepository.get(any[String]))
          .thenReturn(Future.successful(Some(transactionSearchResponseDetail)))

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient),
            bind[RequestBuilder].toInstance(requestBuilder),
            bind[CashAccountSearchRepository].toInstance(mockCashAccountSearchRepository)
          )
          .build()

        running(appWithMocks) {
          val result = await(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          )

          result mustBe Right(transactionSearchResponseDetail)
          verify(mockHttpClient, times(0)).post(any[URL])(any())
          verify(mockCashAccountSearchRepository, times(1)).get(any[String])
          verify(mockCashAccountSearchRepository, times(0))
            .set(any[String], any[CashAccountTransactionSearchResponseDetail])
        }
      }
    }

    "return BadRequest error when backend responds with BAD_REQUEST" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[CashAccountTransactionSearchResponseDetail]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Bad request", BAD_REQUEST)))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        val result = await(
          connector(appWithMocks)
            .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
        )

        result mustBe Left(BadRequest)
      }
    }

    "return InternalServerErrorErrorResponse when backend responds with INTERNAL_SERVER_ERROR" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[CashAccountTransactionSearchResponseDetail]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Internal server error", INTERNAL_SERVER_ERROR)))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        val result = await(
          connector(appWithMocks)
            .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
        )

        result mustBe Left(InternalServerErrorErrorResponse)
      }
    }

    "return ServiceUnavailableErrorResponse when backend responds with SERVICE_UNAVAILABLE" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[CashAccountTransactionSearchResponseDetail]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Service unavailable", SERVICE_UNAVAILABLE)))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        val result = await(
          connector(appWithMocks)
            .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
        )

        result mustBe Left(ServiceUnavailableErrorResponse)
      }
    }

    "return NoAssociatedDataFound when backend responds with NOT_FOUND" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[CashAccountTransactionSearchResponseDetail]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      when(mockCashAccountSearchRepository.get(any[String])).thenReturn(Future.successful(None))

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[CashAccountSearchRepository].toInstance(mockCashAccountSearchRepository)
        )
        .build()

      running(appWithMocks) {
        val result = await(
          connector(appWithMocks)
            .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
        )

        result mustBe Left(NoAssociatedDataFound)
      }
    }

    "return UnknownException when an unexpected error occurs" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[CashAccountTransactionSearchResponseDetail]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Unexpected error")))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        val result = await(
          connector(appWithMocks)
            .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
        )

        result mustBe Left(UnknownException)
      }
    }

    "return correct ErrorResponse object for the ETMP business errors" when {

      "error code is 001 (Invalid Cash Account)" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(CREATED, Json.toJson(errorDetailObject.copy(errorCode = EtmpErrorCode.code001)).toString)
            )
          )

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient)
          )
          .build()

        running(appWithMocks) {
          whenReady(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          ) { response =>
            response.left.getOrElse(UnknownException) mustBe InvalidCashAccount
          }
        }
      }

      "error code is 002 (Invalid Declaration Reference)" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(CREATED, Json.toJson(errorDetailObject.copy(errorCode = EtmpErrorCode.code002)).toString)
            )
          )

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient)
          )
          .build()

        running(appWithMocks) {
          whenReady(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          ) { response =>
            response.left.getOrElse(UnknownException) mustBe InvalidDeclarationReference
          }
        }
      }

      "error code is 003 (Duplicate Acknowledgement Reference)" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(CREATED, Json.toJson(errorDetailObject.copy(errorCode = EtmpErrorCode.code003)).toString)
            )
          )

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient)
          )
          .build()

        running(appWithMocks) {
          whenReady(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          ) { response =>
            response.left.getOrElse(UnknownException) mustBe DuplicateAckRef
          }
        }
      }

      "error code is 004 (No associated data found)" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(CREATED, Json.toJson(errorDetailObject.copy(errorCode = EtmpErrorCode.code004)).toString)
            )
          )

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient)
          )
          .build()

        running(appWithMocks) {
          whenReady(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          ) { response =>
            response.left.getOrElse(UnknownException) mustBe NoAssociatedDataFound
          }
        }
      }

      "error code is 005 (Owner EORI not belongs to the Cash Account)" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(CREATED, Json.toJson(errorDetailObject.copy(errorCode = EtmpErrorCode.code005)).toString)
            )
          )

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient)
          )
          .build()

        running(appWithMocks) {
          whenReady(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          ) { response =>
            response.left.getOrElse(UnknownException) mustBe InvalidEori
          }
        }
      }

      "BAD_REQUEST is returned from ETMP" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(BAD_REQUEST, Json.toJson(errorDetailObject.copy(errorCode = EtmpErrorCode.code400)).toString)
            )
          )

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient)
          )
          .build()

        running(appWithMocks) {
          whenReady(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          ) { response =>
            response.left.getOrElse(UnknownException) mustBe BadRequest
          }
        }
      }

      "INTERNAL_SERVER_ERROR is returned from ETMP" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(
                INTERNAL_SERVER_ERROR,
                Json.toJson(errorDetailObject.copy(errorCode = EtmpErrorCode.code500)).toString
              )
            )
          )

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient)
          )
          .build()

        running(appWithMocks) {
          whenReady(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          ) { response =>
            response.left.getOrElse(UnknownException) mustBe InternalServerErrorErrorResponse
          }
        }
      }

      "SERVICE_UNAVAILABLE is returned from ETMP" in new Setup {
        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(
                SERVICE_UNAVAILABLE,
                Json.toJson(errorDetailObject.copy(errorCode = EtmpErrorCode.code503)).toString
              )
            )
          )

        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient)
          )
          .build()

        running(appWithMocks) {
          whenReady(
            connector(appWithMocks)
              .retrieveCashTransactionsBySearch("testCAN", "GB123456789012", SearchType.D, searchValue, None, None)
          ) { response =>
            response.left.getOrElse(UnknownException) mustBe ServiceUnavailableErrorResponse
          }
        }
      }
    }
  }

  "retrieveCashTransactionsDetail" must {
    "call the correct URL and pass through the HeaderCarrier and CAN," +
      " and return a list of cash daily statements" in new Setup {

        private val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
          .thenReturn(Future.successful(successResponse))
        when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

        val appWithMocks: Application = applicationBuilder
          .overrides(
            bind[HttpClientV2].toInstance(mockHttpClient),
            bind[RequestBuilder].toInstance(requestBuilder)
          )
          .build()

        running(appWithMocks) {
          val result = await(connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate))
          result mustBe Right(successResponse)
        }
      }

    "propagate exceptions when the backend POST fails" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(new HttpException("It's broken", INTERNAL_SERVER_ERROR)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(UnknownException)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with REQUEST_ENTITY_TOO_LARGE" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", REQUEST_ENTITY_TOO_LARGE)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(TooManyTransactionsRequested)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with NOT_FOUND" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(NoTransactionsAvailable)
        }
      }
    }
  }

  "postCashAccountStatements" must {
    "return success when calling the correct URL" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
        .thenReturn(Future.successful(accResponse))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).postCashAccountStatementRequest("eori", "can", fromDate, toDate))

        result mustBe Right(accResponse)
      }
    }

    "return failure when backend post fails" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(new HttpException("It's broken", INTERNAL_SERVER_ERROR)))

      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(bind[HttpClientV2].toInstance(mockHttpClient))
        .build()

      running(appWithMocks) {
        connector(appWithMocks).postCashAccountStatementRequest("eori", "can", fromDate, toDate).map {
          _ mustBe Left(UnknownException)
        }
      }
    }

    "return RequestCouldNotBeProcessed when Request cant be processed error is populated" in new Setup {
      val requestCouldNotBeProcessed: AccountResponseCommon =
        AccountResponseCommon(emptyString, Some("123"), emptyString, None)

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
        .thenReturn(Future.successful(requestCouldNotBeProcessed))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).postCashAccountStatementRequest("eori", "can", fromDate, toDate).map {
          _ mustBe Left(UnknownException)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with BAD_REQUEST" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", BAD_REQUEST)))

      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(BadRequest)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with INTERNAL_SERVER_ERROR" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", INTERNAL_SERVER_ERROR)))

      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(InternalServerErrorErrorResponse)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with SERVICE_UNAVAILABLE" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", SERVICE_UNAVAILABLE)))

      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).retrieveCashTransactionsDetail("can", fromDate, toDate).map {
          _ mustBe Left(ServiceUnavailableErrorResponse)
        }
      }
    }

    "return ErrorResponse when the backend POST fails with NOT_FOUND" in new Setup {
      val requestCouldNotBeProcessed: AccountResponseCommon =
        AccountResponseCommon(emptyString, Some("123"), emptyString, None)

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("Error occurred", NOT_FOUND)))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .build()

      running(appWithMocks) {
        connector(appWithMocks).postCashAccountStatementRequest("eori", "can", fromDate, toDate).map {
          _ mustBe Left(NoAssociatedDataFound)
        }
      }
    }
  }

  "retrieveHistoricCashTransactions" must {
    "return a list of requested cash daily statements" in new Setup {
      private val successResponse = CashTransactions(listOfPendingTransactions, listOfCashDailyStatements)

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[CashTransactions]], any[ExecutionContext]))
        .thenReturn(Future.successful(successResponse))

      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)

      val appWithMocks: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .build()

      running(appWithMocks) {
        val result = await(connector(appWithMocks).retrieveHistoricCashTransactions("can", fromDate, toDate))
        result mustBe Right(successResponse)
      }
    }

    "propagate exceptions when the backend POST fails" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Seq[CashDailyStatement]]], any[ExecutionContext]))
        .thenReturn(Future.failed(new HttpException("It's broken", INTERNAL_SERVER_ERROR)))
      when(mockHttpClient.post(any())(any())).thenReturn(requestBuilder)

      running(appWithHttpClient) {
        val result = await(connector().retrieveHistoricCashTransactions("can", fromDate, toDate))
        result mustBe Left(UnknownException)
      }
    }

    "return ErrorResponse when the backend POST fails with REQUEST_ENTITY_TOO_LARGE" in new Setup {

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

  trait Setup {
    private val traderEori        = "12345678"
    private val cashAccountNumber = "987654"
    private val sMRN              = "ic62zbad-75fa-445f-962b-cc92311686b8e"

    val accResponse: AccountResponseCommon = AccountResponseCommon(emptyString, None, emptyString, None)

    val sessionId: SessionId       = SessionId("session_1234")
    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))

    val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val mockMetricsReporterService: MetricsReporterService           = mock[MetricsReporterService]
    val mockConfig: AppConfig                                        = mock[AppConfig]
    val mockCacheRepository: CacheRepository                         = mock[CacheRepository]
    val mockCashAccountSearchRepository: CashAccountSearchRepository = mock[CashAccountSearchRepository]

    val cdsCashAccount: CdsCashAccount = CdsCashAccount(
      Account(cashAccountNumber, emptyString, traderEori, Some(AccountStatusOpen), false, Some(false)),
      Some("999.99")
    )

    val cashAccount: CashAccount = cdsCashAccount.toDomain

    val fromDate: LocalDate = LocalDate.parse("2019-10-08")
    val toDate: LocalDate   = LocalDate.parse("2020-04-08")
    val eori                = "123456789"

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

    val cashDailyStatementRequest: CashDailyStatementRequest = CashDailyStatementRequest("can", fromDate, toDate)

    val cashAccountStatementRequestDetail: CashAccountStatementRequestDetail =
      CashAccountStatementRequestDetail(eori, "someCan", fromDate.toString, toDate.toString)

    private val otherTransactions =
      Seq(Transaction(123.45, Payment, None), Transaction(-432.87, Withdrawal, Some("77665544")))

    val listOfCashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(
        LocalDate.parse("2020-07-18"),
        500.0,
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
        otherTransactions
      ),
      CashDailyStatement(
        LocalDate.parse("2020-07-20"),
        600.0,
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
        Nil
      )
    )

    val transactionSearchResponseDetail: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        can = "testCAN",
        eoriDetails = Seq(EoriDataContainer(EoriData(eoriNumber = "GB123456789012", name = "Test Importer"))),
        declarations = Some(Seq.empty)
      )

    val timestamp           = "2019-08-1618:15:41"
    val correlationId       = "3jh1f6b3-f8b1-4f3c-973a-05b4720e-4567899"
    val errorCode           = "400"
    val errorMessage        = "Bad request received"
    val source              = "CDS Financials"
    val searchValue: String = "test"

    val sourceFaultDetail: SourceFaultDetail =
      SourceFaultDetail(Seq("Invalid value supplied for field statementRequestId: 32"))

    val errorDetailObject: ErrorDetail =
      ErrorDetail(timestamp, correlationId, errorCode, errorMessage, source, sourceFaultDetail)

    val appWithHttpClient: Application = applicationBuilder
      .overrides(
        bind[HttpClientV2].toInstance(mockHttpClient),
        bind[RequestBuilder].toInstance(requestBuilder)
      )
      .build()

    def connector(app: Application = appWithHttpClient): CustomsFinancialsApiConnector =
      app.injector.instanceOf[CustomsFinancialsApiConnector]
  }
}
