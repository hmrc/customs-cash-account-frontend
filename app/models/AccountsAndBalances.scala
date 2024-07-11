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

package models

import play.api.libs.json._
import play.api.{Logger, LoggerLike}
import play.api.libs.ws.BodyWritable

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.util.Random



case class AccountsAndBalancesResponseContainer(accountsAndBalancesResponse: AccountsAndBalancesResponse) {
  def toCashAccounts: Seq[CashAccount] = {
    List(
      accountsAndBalancesResponse.responseDetail.cdsCashAccount.map(_.map(_.toDomain))
    ).flatten.flatten
  }
}

case class AccountsAndBalancesRequestContainer(accountsAndBalancesRequest: AccountsAndBalancesRequest)

case class AccountsRequestCommon(receiptDate: String, acknowledgementReference: String, regime: String)

object AccountsRequestCommon {
  private val MDG_ACK_REF_LENGTH = 32

  def generate: AccountsRequestCommon = {
    val isoLocalDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS))
    val acknowledgmentRef = generateStringOfRandomDigits(MDG_ACK_REF_LENGTH)
    val regime = "CDS"

    AccountsRequestCommon(isoLocalDateTime, acknowledgmentRef, regime)
  }

  private def generateStringOfRandomDigits(length: Int): String = {
    val maxRandomLength = 10

    (1 to length).map(_ => Random.nextInt(maxRandomLength)).mkString
  }
}

case class AccountsRequestDetail(EORINo: String,
                                 accountType: Option[String],
                                 accountNumber: Option[String],
                                 referenceDate: Option[String])

case class AccountsAndBalancesRequest(requestCommon: AccountsRequestCommon, requestDetail: AccountsRequestDetail)

case class AccountsAndBalancesResponse(responseCommon: Option[AccountResponseCommon],
                                       responseDetail: AccountResponseDetail)

case class AccountResponseCommon(status: String,
                                 statusText: Option[String],
                                 processingDate: String,
                                 returnParameters: Option[Seq[ReturnParameters]])

case class ReturnParameters(paramName: String, paramValue: String)

case class AccountResponseDetail(EORINo: Option[String],
                                 referenceDate: Option[String],
                                 cdsCashAccount: Option[Seq[CdsCashAccount]])

case class CdsCashAccount(account: Account, availableAccountBalance: Option[String]) {
  def toDomain: CashAccount = {
    val balance = CDSCashBalance(availableAccountBalance.map(BigDecimal(_)))
    CashAccount(account.number, account.owner, account.accountStatus.getOrElse(AccountStatusOpen), balance)
  }
}

sealed trait CDSAccountStatus

case object AccountStatusOpen extends CDSAccountStatus

case object AccountStatusClosed extends CDSAccountStatus

case object AccountStatusSuspended extends CDSAccountStatus

object CDSAccountStatus {

  val logger: LoggerLike = Logger(this.getClass)

  implicit val CDSAccountStatusReads: Reads[CDSAccountStatus] = (json: JsValue) => {
    json.as[String] match {
      case status if status.equalsIgnoreCase("Open") => JsSuccess(AccountStatusOpen)
      case status if status.equalsIgnoreCase("Suspended") => JsSuccess(AccountStatusSuspended)
      case status if status.equalsIgnoreCase("Closed") => JsSuccess(AccountStatusClosed)
      case unknown => logger.warn(s"Invalid account status: $unknown"); JsSuccess(AccountStatusOpen)
    }
  }
}

case class Account(number: String,
                   `type`: String,
                   owner: String,
                   accountStatus: Option[CDSAccountStatus],
                   viewBalanceIsGranted: Boolean,
                   isleOfManFlag: Option[Boolean])

case class Limits(periodGuaranteeLimit: String, periodAccountLimit: String)

case class DefermentBalances(periodAvailableGuaranteeBalance: String, periodAvailableAccountBalance: String)

object AccountsAndBalancesResponseContainer {

  implicit val returnParametersReads: Reads[ReturnParameters] = Json.reads[ReturnParameters]

  implicit val accountReads: Reads[Account] = Json.reads[Account]
  implicit val limitsReads: Reads[Limits] = Json.reads[Limits]
  implicit val balancesReads: Reads[DefermentBalances] = Json.reads[DefermentBalances]
  implicit val cashAccountReads: Reads[CdsCashAccount] = Json.reads[CdsCashAccount]

  implicit val accountResponseDetailReads: Reads[AccountResponseDetail] = Json.reads[AccountResponseDetail]
  implicit val accountResponseCommonReads: Reads[AccountResponseCommon] = Json.reads[AccountResponseCommon]

  implicit val accountsAndBalancesResponseReads: Reads[AccountsAndBalancesResponse] =
    Json.reads[AccountsAndBalancesResponse]

  implicit val accountsAndBalancesResponseContainerReads: Reads[AccountsAndBalancesResponseContainer] =
    Json.reads[AccountsAndBalancesResponseContainer]

}

object AccountsAndBalancesRequestContainer {

  implicit val accountsRequestCommonFormat: OFormat[AccountsRequestCommon] = Json.format[AccountsRequestCommon]
  implicit val accountsRequestDetailFormat: OFormat[AccountsRequestDetail] = Json.format[AccountsRequestDetail]

  implicit val accountsAndBalancesRequestFormat: OFormat[AccountsAndBalancesRequest] =
    Json.format[AccountsAndBalancesRequest]

  implicit val accountsAndBalancesRequestContainerFormat: OFormat[AccountsAndBalancesRequestContainer] =
    Json.format[AccountsAndBalancesRequestContainer]


  implicit def jsonBodyWritable[T](implicit
                                   writes: Writes[T],
                                   jsValueBodyWritable: BodyWritable[JsValue]
                                  ): BodyWritable[T] = jsValueBodyWritable.map(writes.writes)
}
