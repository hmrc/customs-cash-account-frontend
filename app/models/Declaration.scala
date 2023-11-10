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

import crypto.EncryptedValue
import models.domain.{EORI, MRN}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Declaration(movementReferenceNumber: MRN,
                       importerEori: Option[String],
                       declarantEori: EORI,
                       declarantReference: Option[String],
                       date: LocalDate,
                       amount: BigDecimal,
                       taxGroups: Seq[TaxGroup])
  extends Ordered[Declaration] {
  override def compare(that: Declaration): Int = movementReferenceNumber.compareTo(that.movementReferenceNumber)
}

object Declaration {
  implicit val format: OFormat[Declaration] = Json.format[Declaration]
}

case class EncryptedDeclaration(movementReferenceNumber: EncryptedValue,
                                importerEori: EncryptedValue,
                                declarantEori: EncryptedValue,
                                declarantReference: Option[EncryptedValue],
                                date: LocalDate,
                                amount: BigDecimal,
                                taxGroups: Seq[TaxGroup])

object EncryptedDeclaration {
  implicit val format: OFormat[EncryptedDeclaration] = Json.format[EncryptedDeclaration]
}
