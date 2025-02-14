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
import models.domain.{EORI, MRN, UCR}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.crypto.Crypted

import java.time.LocalDate

case class Declaration(
  movementReferenceNumber: MRN,
  importerEori: Option[String],
  declarantEori: EORI,
  declarantReference: Option[UCR],
  date: LocalDate,
  amount: BigDecimal,
  taxGroups: Seq[TaxGroup],
  secureMovementReferenceNumber: Option[String]
) extends Ordered[Declaration] {
  override def compare(that: Declaration): Int = movementReferenceNumber.compareTo(that.movementReferenceNumber)
}

object Declaration {
  implicit val format: OFormat[Declaration] = Json.format[Declaration]
}

case class EncryptedDeclaration(
  movementReferenceNumber: Either[EncryptedValue, Crypted],
  importerEori: Either[EncryptedValue, Crypted],
  declarantEori: Either[EncryptedValue, Crypted],
  declarantReference: Option[Either[EncryptedValue, Crypted]],
  date: LocalDate,
  amount: BigDecimal,
  taxGroups: Seq[TaxGroup],
  secureMovementReferenceNumber: String
)

object EncryptedDeclaration {
  import crypto.CryptoAdapterFormats.eitherFormat

  implicit val format: OFormat[EncryptedDeclaration] = Json.format[EncryptedDeclaration]
}
