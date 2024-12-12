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
import models.request.{
  CashAccountPaymentDetails, CashAccountStatementRequestDetail, CashAccountTransactionSearchRequestDetails,
  CashDailyStatementRequest, DeclarationDetailsSearch, IdentifierRequest, SearchType
}
import org.slf4j.LoggerFactory
import play.api.http.Status.{
  BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, REQUEST_ENTITY_TOO_LARGE, SERVICE_UNAVAILABLE
}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.AnyContent
import repositories.{CacheRepository, CashAccountSearchRepository}
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import models.request.CashAccountStatementRequestDetail.jsonBodyWritable
import models.response.CashAccountTransactionSearchResponseDetail
import play.api.libs.json.Json
import utils.EtmpErrorCode
import utils.Utils.buildCacheId

class CustomsFinancialsApiConnector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  metricsReporter: MetricsReporterService,
  cacheRepository: CacheRepository,
  searchRepository: CashAccountSearchRepository
)(implicit executionContext: ExecutionContext) {

  private val logger                                = LoggerFactory.getLogger("application." + getClass.getCanonicalName)
  private val baseUrl                               = appConfig.customsFinancialsApi
  private val accountsUrl                           = s"$baseUrl/eori/accounts"
  private val retrieveCashTransactionsUrl           = s"$baseUrl/account/cash/transactions"
  private val retrieveCashTransactionsDetailUrl     = s"$baseUrl/account/cash/transactions-detail"
  private val retrieveCashAccountStatementsUrl      = s"$baseUrl/accounts/cashaccountstatementrequest/v1"
  private val retrieveCashAccountStatementSearchUrl = s"$baseUrl/account/cash/transaction-search"

  def getCashAccount(
    eori: String
  )(implicit hc: HeaderCarrier, request: IdentifierRequest[AnyContent]): Future[Option[CashAccount]] = {
    val requestDetail              = AccountsRequestDetail(eori, None, None, None)
    val accountsAndBalancesRequest = AccountsAndBalancesRequestContainer(
      AccountsAndBalancesRequest(AccountsRequestCommon.generate, requestDetail)
    )

    metricsReporter
      .withResponseTimeLogging("customs-financials-api.get.accounts") {
        httpClient
          .post(url"$accountsUrl")
          .withBody[AccountsAndBalancesRequestContainer](accountsAndBalancesRequest)
          .execute[AccountsAndBalancesResponseContainer]
          .map(_.toCashAccounts)
      }
      .map(_.find(_.owner == request.eori))
  }

  def retrieveHistoricCashTransactions(can: String, from: LocalDate, to: LocalDate)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, CashTransactions]] = {
    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)

    httpClient
      .post(url"$retrieveCashTransactionsUrl")
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
      logger.error(s"Unable to retrieve cash transactions :${e.getMessage}")
      Left(UnknownException)
  }

  def retrieveCashTransactions(can: String, from: LocalDate, to: LocalDate)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, CashTransactions]] = {
    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)

    cacheRepository
      .get(can)
      .flatMap {
        case Some(value) => Future.successful(Right(value))

        case None =>
          httpClient
            .post(url"$retrieveCashTransactionsUrl")
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

      }
      .recover {
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

  def retrieveCashTransactionsBySearch(
    can: String,
    ownerEORI: String,
    searchType: SearchType.Value,
    searchInput: String,
    declarationDetails: Option[DeclarationDetailsSearch] = None,
    cashAccountPaymentDetails: Option[CashAccountPaymentDetails] = None
  )(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, CashAccountTransactionSearchResponseDetail]] = {

    val request = CashAccountTransactionSearchRequestDetails(
      can,
      ownerEORI,
      searchType,
      declarationDetails,
      cashAccountPaymentDetails
    )

    val cacheId = buildCacheId(can, searchInput)

    searchRepository.get(cacheId).flatMap {
      case Some(value) => Future.successful(Right(value))

      case None =>
        httpClient
          .post(url"$retrieveCashAccountStatementSearchUrl")
          .withBody[CashAccountTransactionSearchRequestDetails](request)
          .execute[HttpResponse]
          .map(jsonResponse => processResponseForTransactionsBySearch(cacheId, jsonResponse))
    }
  }.recover {
    case UpstreamErrorResponse(_, BAD_REQUEST, _, _) =>
      logger.error("BAD Request for retrieveCashTransactionsBySearch")
      Left(BadRequest)

    case UpstreamErrorResponse(_, INTERNAL_SERVER_ERROR, _, _) =>
      logger.error("No Transactions available for retrieveCashTransactionsBySearch")
      Left(InternalServerErrorErrorResponse)

    case UpstreamErrorResponse(_, SERVICE_UNAVAILABLE, _, _) =>
      logger.error("SERVICE_UNAVAILABLE for retrieveCashTransactionsBySearch")
      Left(ServiceUnavailableErrorResponse)

    case e =>
      logger.error(s"Unknown error for retrieveCashTransactionsBySearch :${e.getMessage}")
      Left(UnknownException)
  }

  def retrieveCashTransactionsDetail(can: String, from: LocalDate, to: LocalDate)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, CashTransactions]] = {

    val cashDailyStatementRequest = CashDailyStatementRequest(can, from, to)

    httpClient
      .post(url"$retrieveCashTransactionsDetailUrl")
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

  def postCashAccountStatementRequest(eori: String, can: String, from: LocalDate, to: LocalDate)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, AccountResponseCommon]] = {

    val request = CashAccountStatementRequestDetail(eori, can, from.toString, to.toString)

    httpClient
      .post(url"$retrieveCashAccountStatementsUrl")
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

  private def processStatusCode(
    accountResponseCommon: AccountResponseCommon
  ): Either[ErrorResponse, AccountResponseCommon] =
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

      case Some(_) =>
        logger.error("unidentified error code")
        Left(UnknownException)

      case _ =>
        Right(accountResponseCommon)
    }

  private def processResponseForTransactionsBySearch(cacheId: String, response: HttpResponse) =
    response.status match {
      case OK => processOKResponse(cacheId, response)

      case CREATED => processETMPErrors(response)

      case BAD_REQUEST =>
        logger.error("Bad request error while calling ETMP")
        Left(BadRequest)

      case INTERNAL_SERVER_ERROR =>
        logger.error("Internal Server error while calling ETMP")
        Left(InternalServerErrorErrorResponse)

      case _ =>
        logger.error("Service Unavailable error while calling ETMP")
        Left(ServiceUnavailableErrorResponse)
    }

  private def processOKResponse(cacheId: String, response: HttpResponse) = {
    val responseDetail = Json.fromJson[CashAccountTransactionSearchResponseDetail](response.json)

    searchRepository.set(cacheId, responseDetail.get).map { successfulWrite =>
      if (!successfulWrite) {
        logger.error("Failed to store data in the session cache defaulting to the api response")
      }
    }

    responseDetail.asOpt.fold(Left(UnknownException))(Right(_))
  }

  private def processETMPErrors(
    res: HttpResponse
  ): Either[ErrorResponse, CashAccountTransactionSearchResponseDetail] = {
    val errorDetail: Option[ErrorDetail] = Json.fromJson[ErrorDetail](res.json).asOpt

    errorDetail match {
      case Some(errorDetail) => checkErrorCodeAndReturnErrorResponse(errorDetail)
      case _                 => Left(UnknownException)
    }
  }

  private def checkErrorCodeAndReturnErrorResponse(errorDetail: ErrorDetail) =
    errorDetail.errorCode match {
      case EtmpErrorCode.code001 =>
        logger.warn("Invalid Cash Account error")
        Left(InvalidCashAccount)

      case EtmpErrorCode.code002 =>
        logger.warn("Invalid Declaration Reference error")
        Left(InvalidDeclarationReference)

      case EtmpErrorCode.code003 =>
        logger.warn("Duplicate Acknowledge Reference error")
        Left(DuplicateAckRef)

      case EtmpErrorCode.code004 =>
        logger.warn("No Associated Data Found error")
        Left(NoAssociatedDataFound)

      case EtmpErrorCode.code005 =>
        logger.warn("Owner EORI not belongs to the Cash Account error")
        Left(InvalidEori)

      case _ => Left(UnknownException)
    }

  private def addUUIDToCashTransaction(response: CashTransactions): CashTransactions =
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

case object InvalidCashAccount extends ErrorResponse

case object InvalidDeclarationReference extends ErrorResponse

case object DuplicateAckRef extends ErrorResponse

case object NoAssociatedDataFound extends ErrorResponse
