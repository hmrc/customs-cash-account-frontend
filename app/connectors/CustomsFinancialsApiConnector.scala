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
import models.CashDailyStatement.*
import models.*
import models.request.{CashDailyStatementRequest, IdentifierRequest}
import org.slf4j.LoggerFactory
import play.api.http.Status.{NOT_FOUND, REQUEST_ENTITY_TOO_LARGE}
import play.api.mvc.AnyContent
import repositories.CacheRepository
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import java.util.UUID

class CustomsFinancialsApiConnector @Inject()(httpClient: HttpClientV2,
                                              appConfig: AppConfig,
                                              metricsReporter: MetricsReporterService,
                                              cacheRepository: CacheRepository)
                                             (implicit executionContext: ExecutionContext) {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)
  private val baseUrl = appConfig.customsFinancialsApi
  private val accountsUrl = s"$baseUrl/eori/accounts"
  private val retrieveCashTransactionsUrl = s"$baseUrl/account/cash/transactions"
  private val retrieveCashTransactionsDetailUrl = s"$baseUrl/account/cash/transactions-detail"

  def getCashAccount(eori: String)(implicit hc: HeaderCarrier,
                                   request: IdentifierRequest[AnyContent]): Future[Option[CashAccount]] = {
    val requestDetail = AccountsRequestDetail(eori, None, None, None)
    val accountsAndBalancesRequest = AccountsAndBalancesRequestContainer(
      AccountsAndBalancesRequest(AccountsRequestCommon.generate, requestDetail)
    )

    metricsReporter.withResponseTimeLogging("customs-financials-api.get.accounts") {
      httpClient.post(url"$accountsUrl")
        .withBody[AccountsAndBalancesRequestContainer](accountsAndBalancesRequest)
        .execute[AccountsAndBalancesResponseContainer]
        .map(_.toCashAccounts)
    }.map(_.find(_.owner == request.eori))
  }

  def retrieveHistoricCashTransactions(can: String,
                                       from: LocalDate,
                                       to: LocalDate)
                                      (implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashTransactions]] = {
    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)

    httpClient.post(url"$retrieveCashTransactionsUrl")
      .withBody[CashDailyStatementRequest](cashDailyStatementRequest)
      .execute[CashTransactions].map(Right(_))
  }.recover {
    case UpstreamErrorResponse(_, REQUEST_ENTITY_TOO_LARGE, _, _) =>
      logger.error(s"Entity too large to download")
      Left(TooManyTransactionsRequested)

    case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
      logger.error(s"No data found")
      Left(NoTransactionsAvailable)

    case e =>
      logger.error(s"Unable to retrieve cash transactions :${e.getMessage}")
      Left(UnknownException)
  }

  def retrieveCashTransactions(can: String,
                               from: LocalDate,
                               to: LocalDate)
                              (implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashTransactions]] = {
    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)

    def addUUID(response: CashTransactions): CashTransactions = {
      response.copy(
        cashDailyStatements = response.cashDailyStatements.map { statement =>
          statement.copy(
            declarations = statement.declarations.map { declaration =>
              declaration.copy(secureMovementReferenceNumber = Some(UUID.randomUUID().toString))
            }
          )
        }
      )
    }

    cacheRepository.get(can).flatMap {
      case Some(value) => Future.successful(Right(value))

      case None =>
        httpClient.post(url"$retrieveCashTransactionsUrl")
          .withBody[CashDailyStatementRequest](cashDailyStatementRequest)
          .execute[CashTransactions]
          .flatMap { response =>
            val transactionsWithUUID = addUUID(response)
            cacheRepository.set(can, transactionsWithUUID).map { successfulWrite =>
              if (!successfulWrite) {
                logger.error("Failed to store data in the session cache defaulting to the api response")
              }
              Right(transactionsWithUUID)
            }
          }
    }.recover {
      case UpstreamErrorResponse(_, REQUEST_ENTITY_TOO_LARGE, _, _) =>
        logger.error(s"Entity too large to download")
        Left(TooManyTransactionsRequested)

      case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
        logger.error(s"No data found")
        Left(NoTransactionsAvailable)

      case e =>
        logger.error(s"Unable to retrieve cash transactions: ${e.getMessage}")
        Left(UnknownException)
    }
  }

  def retrieveCashTransactionsDetail(can: String,
                                     from: LocalDate,
                                     to: LocalDate)
                                    (implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashTransactions]] = {
    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)

    httpClient.post(url"$retrieveCashTransactionsDetailUrl")
      .withBody[CashDailyStatementRequest](cashDailyStatementRequest)
      .execute[CashTransactions]
      .map(Right(_))
  }.recover {
    case UpstreamErrorResponse(_, REQUEST_ENTITY_TOO_LARGE, _, _) =>
      logger.error(s"Entity too large to download")
      Left(TooManyTransactionsRequested)

    case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
      logger.error(s"No data found")
      Left(NoTransactionsAvailable)

    case e =>
      logger.error(s"Unable to download CSV :${e.getMessage}")
      Left(UnknownException)
  }
}

sealed trait ErrorResponse

case object NoTransactionsAvailable extends ErrorResponse

case object TooManyTransactionsRequested extends ErrorResponse

case object UnknownException extends ErrorResponse
