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

import utils.SpecBase

import play.api.libs.json._
import java.time.LocalDate
import crypto.EncryptedValue

class DeclarationSpec extends SpecBase {

  "Declaration format" should {

    "serialize and deserialize a Declaration with all fields populated" in new Setup {
      val json: JsValue       = Declaration.format.writes(declarations)
      val result: Declaration = Declaration.format.reads(json).get
      result mustBe declarations
    }

    "handle missing optional fields in Declaration" in new Setup {
      val declarationWithFieldsMissing: Declaration = declarations.copy(
        importerEori = None,
        declarantReference = None,
        secureMovementReferenceNumber = None
      )

      val json: JsValue       = Declaration.format.writes(declarationWithFieldsMissing)
      val result: Declaration = Declaration.format.reads(json).get
      result mustBe declarationWithFieldsMissing
    }
  }

  "EncryptedDeclaration format" should {

    "serialize and deserialize an EncryptedDeclaration with all fields populated" in new Setup {
      val json: JsValue                = EncryptedDeclaration.format.writes(encryptedDeclaration)
      val result: EncryptedDeclaration = EncryptedDeclaration.format.reads(json).get
      result mustBe encryptedDeclaration
    }
  }

  trait Setup {

    val year  = 2024
    val month = 5
    val day   = 5

    val date: LocalDate         = LocalDate.of(year, month, day)
    val thousand: BigDecimal    = BigDecimal(1000.00)
    val twoThousand: BigDecimal = BigDecimal(2000.00)

    val movementReferenceNumber       = "MRN1234567890"
    val nonce                         = "someNone"
    val importerEori                  = "GB123456789000"
    val declarantEori                 = "GB987654321000"
    val declarantReference            = "UCR12345"
    val secureMovementReferenceNumber = "5a71a767-5c1c-4df8-8eef-2b83769b8fda"

    val taxTypes: Seq[TaxType] = Seq(TaxType(reasonForSecurity = Some("Reason"), taxTypeID = "50", amount = thousand))

    val declarations: Declaration = Declaration(
      movementReferenceNumber = movementReferenceNumber,
      importerEori = Some(importerEori),
      declarantEori = declarantEori,
      declarantReference = Some(declarantReference),
      date = date,
      amount = twoThousand,
      taxGroups = Seq(
        TaxGroup(CustomsDuty, twoThousand, taxTypes),
        TaxGroup(ImportVat, thousand, taxTypes),
        TaxGroup(ExciseDuty, twoThousand, taxTypes)
      ),
      secureMovementReferenceNumber = Some(secureMovementReferenceNumber)
    )

    val encryptedDeclaration: EncryptedDeclaration = EncryptedDeclaration(
      movementReferenceNumber = EncryptedValue(movementReferenceNumber, nonce),
      importerEori = EncryptedValue(importerEori, nonce),
      declarantEori = EncryptedValue(declarantEori, nonce),
      declarantReference = Some(EncryptedValue(declarantReference, nonce)),
      date = date,
      amount = thousand,
      taxGroups = Seq(
        TaxGroup(CustomsDuty, twoThousand, taxTypes),
        TaxGroup(ImportVat, thousand, taxTypes),
        TaxGroup(ExciseDuty, twoThousand, taxTypes)
      ),
      secureMovementReferenceNumber = secureMovementReferenceNumber
    )
  }
}
