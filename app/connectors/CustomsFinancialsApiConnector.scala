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
import models.CashDailyStatement._
import models.email.{EmailUnverifiedResponse, EmailVerifiedResponse}
import models.request.{CashDailyStatementRequest, IdentifierRequest}
import models._
import org.slf4j.LoggerFactory
import play.api.http.Status.{NOT_FOUND, REQUEST_ENTITY_TOO_LARGE}
import play.api.mvc.AnyContent
import repositories.CacheRepository
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsFinancialsApiConnector @Inject()(
                                               httpClient: HttpClient,
                                               appConfig: AppConfig,
                                               metricsReporter: MetricsReporterService,
                                               cacheRepository: CacheRepository
                                             )(implicit executionContext: ExecutionContext) {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  private val baseUrl = appConfig.customsFinancialsApi
  private val accountsUrl = s"$baseUrl/eori/accounts"
  private val retrieveCashTransactionsUrl = s"$baseUrl/account/cash/transactions"
  private val retrieveCashTransactionsDetailUrl = s"$baseUrl/account/cash/transactions-detail"

  def getCashAccount(eori: String)(implicit hc: HeaderCarrier, request: IdentifierRequest[AnyContent]): Future[Option[CashAccount]] = {
    val requestDetail = AccountsRequestDetail(eori, None, None, None)
    val accountsAndBalancesRequest = AccountsAndBalancesRequestContainer(
      AccountsAndBalancesRequest(AccountsRequestCommon.generate, requestDetail)
    )

    metricsReporter.withResponseTimeLogging("customs-financials-api.get.accounts") {
      httpClient.POST[AccountsAndBalancesRequestContainer, AccountsAndBalancesResponseContainer](
        accountsUrl, accountsAndBalancesRequest).map(_.toCashAccounts)
    }.map(_.find(_.owner == request.eori))
  }

  def retrieveHistoricCashTransactions(can: String,
                                       from: LocalDate,
                                       to: LocalDate)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashTransactions]] = {
    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)
    httpClient.POST[CashDailyStatementRequest, CashTransactions](
      retrieveCashTransactionsUrl, cashDailyStatementRequest).map(Right(_))
  }.recover {
    case UpstreamErrorResponse(_, REQUEST_ENTITY_TOO_LARGE, _, _) =>
      logger.error(s"Entity too large to download"); Left(TooManyTransactionsRequested)

    case UpstreamErrorResponse(_, NOT_FOUND, _, _) => logger.error(s"No data found")
      Left(NoTransactionsAvailable)

    case e => logger.error(s"Unable to retrieve cash transactions :${e.getMessage}")
      Left(UnknownException)
  }


  def retrieveCashTransactions(can: String,
                               from: LocalDate,
                               to: LocalDate)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashTransactions]] = {
    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)

    cacheRepository.get(can).flatMap {
      case Some(value) => Future.successful(Right(value))

      case None =>
        httpClient.POST[CashDailyStatementRequest, CashTransactions](
          retrieveCashTransactionsUrl, cashDailyStatementRequest).flatMap { response =>

          cacheRepository.set(can, response).map { successfulWrite =>
            if (!successfulWrite) {
              logger.error("Failed to store data in the session cache defaulting to the api response")
            }
            Right(response)
          }
        }
    }.recover {
      case UpstreamErrorResponse(_, REQUEST_ENTITY_TOO_LARGE, _, _) =>
        logger.error(s"Entity too large to download"); Left(TooManyTransactionsRequested)

      case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
        logger.error(s"No data found")
        Left(NoTransactionsAvailable)

      case e =>
        logger.error(s"Unable to retrieve cash transactions :${e.getMessage}")
        Left(UnknownException)
    }
  }

  def retrieveCashTransactionsDetail(can: String,
                                     from: LocalDate,
                                     to: LocalDate)(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashTransactions]] = {
    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)

    httpClient.POST[CashDailyStatementRequest, CashTransactions](
      retrieveCashTransactionsDetailUrl, cashDailyStatementRequest).map(Right(_))
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

  def verifiedEmail(implicit hc: HeaderCarrier): Future[EmailVerifiedResponse] = {
    val emailDisplayApiUrl = s"$baseUrl/subscriptions/email-display"

    httpClient.GET[EmailVerifiedResponse](emailDisplayApiUrl).recover{
      case _ =>
        logger.error(s"Error occurred while calling API $emailDisplayApiUrl")
        EmailVerifiedResponse(None)
    }
  }

  /**
   * Retrieves unverified email from customs-financials-api using below route
   * /customs-financials-api/subscriptions/unverified-email-display
   */
  def retrieveUnverifiedEmail(implicit hc: HeaderCarrier): Future[EmailUnverifiedResponse] = {
    val unverifiedEmailDisplayApiUrl = s"$baseUrl/subscriptions/unverified-email-display"

    httpClient.GET[EmailUnverifiedResponse](unverifiedEmailDisplayApiUrl).recover {
      case _ =>
        logger.error(s"Error occurred while calling API $unverifiedEmailDisplayApiUrl")
        EmailUnverifiedResponse(None)
    }
  }
}

sealed trait ErrorResponse

case object NoTransactionsAvailable extends ErrorResponse

case object TooManyTransactionsRequested extends ErrorResponse

case object UnknownException extends ErrorResponse
