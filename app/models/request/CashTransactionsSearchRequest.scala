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

package models.request

import play.api.libs.json.*

case class CashAccountTransactionSearchRequestDetails(can: String,
                                                      ownerEORI: String,
                                                      searchType: SearchType.Value,
                                                      declarationDetails: Option[DeclarationDetailsSearch] = None,
                                                      cashAccountPaymentDetails: Option[CashAccountPaymentDetails] = None)

case class DeclarationDetailsSearch(paramName: ParamName.Value, paramValue: String)

case class CashAccountPaymentDetails(amount: Double,
                                     dateFrom: Option[String] = None,
                                     dateTo: Option[String] = None)

object SearchType extends Enumeration {
  type SearchType = Value

  val P, D = Value

  implicit val searchTypeReads: Reads[SearchType.Value] = JsPath.read[String].map(strVal => SearchType.withName(strVal))
  implicit val searchTypeWrites: Writes[SearchType.Value] = Writes { value => JsString(value.toString) }

  implicit val searchTypeFormat: Format[SearchType.Value] = Format(searchTypeReads, searchTypeWrites)
}

object ParamName extends Enumeration {
  type ParamName = Value

  val MRN, UCR = Value

  implicit val paramNameReads: Reads[ParamName.Value] = JsPath.read[String].map(strVal => ParamName.withName(strVal))
  implicit val paramNameWrites: Writes[ParamName.Value] = Writes { value => JsString(value.toString) }

  implicit val paramNameFormat: Format[ParamName.Value] = Format(paramNameReads, paramNameWrites)
}

object DeclarationDetailsSearch {
  implicit val format: OFormat[DeclarationDetailsSearch] = Json.format[DeclarationDetailsSearch]
}

object CashAccountPaymentDetails {
  implicit val format: OFormat[CashAccountPaymentDetails] = Json.format[CashAccountPaymentDetails]
}

object CashAccountTransactionSearchRequestDetails {
  implicit val format: OFormat[CashAccountTransactionSearchRequestDetails] =
    Json.format[CashAccountTransactionSearchRequestDetails]
}
