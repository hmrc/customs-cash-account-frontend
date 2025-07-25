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

import crypto.Crypto
import utils.SpecBase
import play.api.libs.json.*
import play.api.Configuration

import java.time.LocalDate

class DeclarationSpec extends SpecBase {

  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val config    = Configuration("mongodb.encryptionKey" -> secretKey)
  private val crypto    = new Crypto(config)

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

    "fail to deserialize invalid Declaration JSON (e.g., missing required field)" in new Setup {
      val invalidJson: JsValue = Json.parse(
        s"""{
           |  "importerEori": "$importerEori",
           |  "declarantReference": "$declarantReference",
           |  "date": "$date",
           |  "amount": $twoThousand
           |}""".stripMargin
      )

      Declaration.format.reads(invalidJson) mustBe a[JsError]
    }

    "fail to deserialize Declaration with wrong data type" in new Setup {
      val invalidJson: JsValue = Json.parse(
        s"""{
           |  "movementReferenceNumber": 123,
           |  "importerEori": "$importerEori",
           |  "declarantEori": "$declarantEori",
           |  "date": "$date",
           |  "amount": "$twoThousand",
           |  "taxGroups": []
           |}""".stripMargin
      )

      Declaration.format.reads(invalidJson) mustBe a[JsError]
    }

  }

  "EncryptedDeclaration format" should {

    "serialize and deserialize an EncryptedDeclaration with all fields populated" in new Setup {
      val json: JsValue                = EncryptedDeclaration.format.writes(encryptedDeclaration)
      val result: EncryptedDeclaration = EncryptedDeclaration.format.reads(json).get
      result mustBe encryptedDeclaration
    }

    "fail to deserialize invalid EncryptedDeclaration JSON" in new Setup {
      val invalidJson: JsValue = Json.parse(
        s"""{
           |  "importerEori": "${crypto.encrypt(importerEori)}",
           |  "declarantEori": "${crypto.encrypt(declarantEori)}",
           |  "amount": $thousand,
           |  "taxGroups": []
           |}""".stripMargin
      )

      EncryptedDeclaration.format.reads(invalidJson) mustBe a[JsError]
    }

    "fail to deserialize EncryptedDeclaration with wrong data type" in new Setup {
      val invalidJson: JsValue = Json.parse(
        s"""{
           |  "movementReferenceNumber": 123,
           |  "importerEori": true,
           |  "declarantEori": {},
           |  "date": "not-a-date",
           |  "amount": "1000",
           |  "taxGroups": "not-an-array"
           |}""".stripMargin
      )

      EncryptedDeclaration.format.reads(invalidJson) mustBe a[JsError]
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
      movementReferenceNumber = crypto.encrypt(movementReferenceNumber),
      importerEori = crypto.encrypt(importerEori),
      declarantEori = crypto.encrypt(declarantEori),
      declarantReference = Some(crypto.encrypt(declarantReference)),
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
