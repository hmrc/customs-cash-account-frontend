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

package models.response

import models.*
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*

case class CashAccountTransactionSearchResponseContainer(cashAccountTransactionSearchResponse: CashAccountTransactionSearchResponse)

case class CashAccountTransactionSearchResponse(responseCommon: CashTransactionsResponseCommon,
                                                responseDetail: Option[CashAccountTransactionSearchResponseDetail] = None)

case class CashTransactionsResponseCommon(status: String,
                                          statusText: Option[String],
                                          processingDate: String,
                                          returnParameters: Option[Array[ReturnParameter]] = None)

case class ReturnParameter(paramName: String, paramValue: String)

case class CashAccountTransactionSearchResponseDetail(can: String,
                                                      eoriDetails: Seq[EoriDataContainer],
                                                      declarations: Option[Seq[DeclarationWrapper]],
                                                      paymentsWithdrawalsAndTransfers: Option[Seq[PaymentsWithdrawalsAndTransferContainer]] = None)

case class EoriDataContainer(eoriData: EoriData)

case class EoriData(eoriNumber: String, name: String)

case class DeclarationWrapper(declaration: DeclarationSearch)

case class DeclarationSearch(declarationID: String,
                             declarantEORINumber: String,
                             declarantRef: Option[String] = None,
                             c18OrOverpaymentReference: Option[String] = None,
                             importersEORINumber: String,
                             postingDate: String,
                             acceptanceDate: String,
                             amount: Double,
                             taxGroups: Seq[TaxGroupWrapper])

case class TaxGroupWrapper(taxGroup: TaxGroupSearch)

case class TaxGroupSearch(taxGroupDescription: String, amount: Double, taxTypes: Seq[TaxTypeWithSecurityContainer])

case class TaxTypeWithSecurityContainer(taxType: TaxTypeWithSecurity)

case class TaxTypeWithSecurity(reasonForSecurity: Option[String] = None,
                               taxTypeID: String,
                               amount: Double)

case class PaymentsWithdrawalsAndTransferContainer(paymentsWithdrawalsAndTransfer: PaymentsWithdrawalsAndTransfer)

case class PaymentsWithdrawalsAndTransfer(valueDate: String,
                                          postingDate: String,
                                          paymentReference: String,
                                          amount: Double,
                                          `type`: PaymentType.Value,
                                          bankAccount: Option[String] = None,
                                          sortCode: Option[String] = None)

object PaymentType extends Enumeration {
  type PaymentType = Value

  val Payment, Withdrawal, Transfer = Value

  implicit val format: Format[PaymentType.Value] = Json.formatEnum(PaymentType)
}

object CashAccountTransactionSearchResponseDetail {
  implicit val cashAccTransSearchResponseDetailReads: Reads[CashAccountTransactionSearchResponseDetail] = (
    (JsPath \ "can").read[String] and
      (JsPath \ "eoriDetails").read[Seq[EoriDataContainer]] and
      (JsPath \ "declarations").readNullable[Seq[DeclarationWrapper]].map(identity) and
      (JsPath \ "paymentsWithdrawalsAndTransfers").readNullable[Seq[PaymentsWithdrawalsAndTransferContainer]].map(identity)
    )(CashAccountTransactionSearchResponseDetail.apply _)

  implicit val cashAccTransSearchResponseDetailWrites: Writes[CashAccountTransactionSearchResponseDetail] = (
    (JsPath \ "can").write[String] and
      (JsPath \ "eoriDetails").write[Seq[EoriDataContainer]] and
      (JsPath \ "declarations").writeNullable[Seq[DeclarationWrapper]] and
      (JsPath \ "paymentsWithdrawalsAndTransfers").writeNullable[Seq[PaymentsWithdrawalsAndTransferContainer]]
    )(resDetails =>
    (resDetails.can, resDetails.eoriDetails, resDetails.declarations, resDetails.paymentsWithdrawalsAndTransfers))

  implicit val format: Format[CashAccountTransactionSearchResponseDetail] =
    Format(cashAccTransSearchResponseDetailReads, cashAccTransSearchResponseDetailWrites)
}

object PaymentsWithdrawalsAndTransfer {
  implicit val paymentWithdrawalsAndTransferReads: Reads[PaymentsWithdrawalsAndTransfer] = (
    (JsPath \ "valueDate").read[String] and
      (JsPath \ "postingDate").read[String] and
      (JsPath \ "paymentReference").read[String] and
      (JsPath \ "amount").read[Double] and
      (JsPath \ "type").read[String].map(strVal => PaymentType.withName(strVal)) and
      (JsPath \ "bankAccount").readNullable[String].map(identity) and
      (JsPath \ "sortCode").readNullable[String].map(identity)
    )(PaymentsWithdrawalsAndTransfer.apply _)

  implicit val paymentWithdrawalsAndTransferWrites: Writes[PaymentsWithdrawalsAndTransfer] =
    (paymentTransfer: PaymentsWithdrawalsAndTransfer) => {
      Json.obj(
        "valueDate" -> paymentTransfer.valueDate,
        "postingDate" -> paymentTransfer.postingDate,
        "paymentReference" -> paymentTransfer.paymentReference,
        "amount" -> paymentTransfer.amount,
        "type" -> paymentTransfer.`type`,
        "bankAccount" -> paymentTransfer.bankAccount.map(identity),
        "sortCode" -> paymentTransfer.sortCode.map(identity)
      )
    }

  implicit val format: Format[PaymentsWithdrawalsAndTransfer] =
    Format(paymentWithdrawalsAndTransferReads, paymentWithdrawalsAndTransferWrites)
}

object ReturnParameter {
  implicit val format: OFormat[ReturnParameter] = Json.format[ReturnParameter]
}

object EoriData {
  implicit val format: OFormat[EoriData] = Json.format[EoriData]
}

object EoriDataContainer {
  implicit val format: OFormat[EoriDataContainer] = Json.format[EoriDataContainer]
}

object TaxTypeWithSecurity {
  implicit val format: OFormat[TaxTypeWithSecurity] = Json.format[TaxTypeWithSecurity]
}

object TaxTypeWithSecurityContainer {
  implicit val format: OFormat[TaxTypeWithSecurityContainer] = Json.format[TaxTypeWithSecurityContainer]
}

object TaxGroupSearch {
  implicit val format: OFormat[TaxGroupSearch] = Json.format[TaxGroupSearch]
}

object TaxGroupWrapper {
  implicit val format: OFormat[TaxGroupWrapper] = Json.format[TaxGroupWrapper]
}

object DeclarationSearch {
  implicit val format: OFormat[DeclarationSearch] = Json.format[DeclarationSearch]
}

object DeclarationWrapper {
  implicit val format: OFormat[DeclarationWrapper] = Json.format[DeclarationWrapper]
}

object PaymentsWithdrawalsAndTransferContainer {
  implicit val format: OFormat[PaymentsWithdrawalsAndTransferContainer] =
    Json.format[PaymentsWithdrawalsAndTransferContainer]
}

object CashAccountTransactionSearchResponse {
  implicit val responseCommonFormat: OFormat[CashTransactionsResponseCommon] =
    Json.format[CashTransactionsResponseCommon]

  implicit val format: OFormat[CashAccountTransactionSearchResponse] =
    Json.format[CashAccountTransactionSearchResponse]
}

object CashAccountTransactionSearchResponseContainer {
  implicit val format: OFormat[CashAccountTransactionSearchResponseContainer] =
    Json.format[CashAccountTransactionSearchResponseContainer]
}
