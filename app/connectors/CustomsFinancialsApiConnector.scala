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
import helpers.Constants.*
import models.*
import models.AccountsAndBalancesResponseContainer.accountResponseCommonReads
import models.CashDailyStatement.*
import models.request.{CashAccountStatementRequestDetail, CashDailyStatementRequest, IdentifierRequest}
import org.slf4j.LoggerFactory
import play.api.http.Status.*
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.AnyContent
import repositories.CacheRepository
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import utils.Utils.emptyString

import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
  private val retrieveCashAccountStatementsUrl = s"$baseUrl/accounts/cashaccountstatementrequest/v1"


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

    def addUUIDToCashTransaction(response: CashTransactions): CashTransactions = {
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

            val transactionsWithUUID = addUUIDToCashTransaction(response)

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

  def postCashAccountStatementRequest(eori: String,
                                      can: String,
                                      from: LocalDate,
                                      to: LocalDate)
                                     (implicit hc: HeaderCarrier): Future[Either[ErrorResponse, AccountResponseCommon]] = {

    val request = CashAccountStatementRequestDetail(eori, can, from.toString, to.toString)

    httpClient.post(url"$retrieveCashAccountStatementsUrl")
      .withBody[CashAccountStatementRequestDetail](request)
      .execute[AccountResponseCommon]
      .map(processStatusCode)

  }.recover {
    case UpstreamErrorResponse(_, BAD_REQUEST, _, _) =>
      logger.error("BAD Request for postCashAccountStatements")
      Left(BadRequest)

    case UpstreamErrorResponse(_, INTERNAL_SERVER_ERROR, _, _) =>
      logger.error("No Transactions available for postCashAccountStatements")
      Left(InternalServerErrorErrorResponse)

    case UpstreamErrorResponse(_, SERVICE_UNAVAILABLE, _, _) =>
      logger.error("SERVICE_UNAVAILABLE for postCashAccountStatements")
      Left(ServiceUnavailableErrorResponse)

    case e =>
      logger.error(s"Unknown error for postCashAccountStatements :${e.getMessage}")
      Left(UnknownException)
  }

  private def processStatusCode(accountResponseCommon: AccountResponseCommon): Either[ErrorResponse, AccountResponseCommon] = {

    accountResponseCommon.statusText match {

      case Some(REQUEST_COULD_NOT_BE_PROCESSED) =>
        logger.error(s"REQUEST_COULD_NOT_BE_PROCESSED for the postCashAccountStatementRequest - processStatusCode")
        Left(RequestCouldNotBeProcessed)

      case Some(DUPLICATE_SUBMISSION) =>
        logger.error(s"DUPLICATE_SUBMISSION for the postCashAccountStatementRequest - processStatusCode")
        Left(DuplicateSubmissionAckRef)

      case Some(ACCOUNT_DOES_NOT_EXIST) =>
        logger.error(s"ACCOUNT_DOES_NOT_EXIST for the postCashAccountStatementRequest - processStatusCode")
        Left(AccountDoesNotExist)

      case Some(INVALID_EORI) =>
        logger.error(s"INVALID_EORI for the postCashAccountStatementRequest - processStatusCode")
        Left(InvalidEori)

      case Some(ENTRY_ALREADY_EXISTS) =>
        logger.error(s"ENTRY_ALREADY_EXISTS for the postCashAccountStatementRequest - processStatusCode")
        Left(EntryAlreadyExists)

      case Some(EXCEEDED_MAXIMUM) =>
        logger.error(s"EXCEEDED_MAXIMUM for the postCashAccountStatementRequest - processStatusCode")
        Left(ExceededMaximum)

      case Some(emptyString) => Right(accountResponseCommon)

      case _ => Left(UnknownException)
    }
  }
}

sealed trait ErrorResponse

case object NoTransactionsAvailable extends ErrorResponse

case object InternalServerErrorErrorResponse extends ErrorResponse

case object ServiceUnavailableErrorResponse extends ErrorResponse

case object TooManyTransactionsRequested extends ErrorResponse

case object BadRequest extends ErrorResponse

case object UnknownException extends ErrorResponse

case object RequestCouldNotBeProcessed extends ErrorResponse

case object DuplicateSubmissionAckRef extends ErrorResponse

case object AccountDoesNotExist extends ErrorResponse

case object InvalidEori extends ErrorResponse

case object EntryAlreadyExists extends ErrorResponse

case object ExceededMaximum extends ErrorResponse
