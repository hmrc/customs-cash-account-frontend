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

import play.api.libs.json.{JsError, JsObject, JsResult, JsResultException, JsValue, Json}
import utils.SpecBase

class TaxGroupSpec extends SpecBase {

  "TaxType" must {

    "serialize and deserialize TaxType correctly" in new Setup {

      val expectedTaxType: TaxType = TaxType(Some("Reason"), "VAT", hundred)
      val serialized: JsValue      = Json.toJson(expectedTaxType)
      val deserialized: TaxType    = serialized.as[TaxType]

      deserialized mustBe expectedTaxType
    }

    "handle invalid JSON" in new Setup {

      val invalidJson: JsValue = Json.obj("reason" -> "Reason", "type" -> "VAT")

      intercept[JsResultException] {
        invalidJson.as[TaxType]
      }
    }

    "fail to deserialize TaxType with incorrect field types" in new Setup {
      val invalidJson: JsObject     = Json.obj("reason" -> 123, "type" -> true, "amount" -> "not-a-number")
      val result: JsResult[TaxType] = Json.fromJson[TaxType](invalidJson)
      result mustBe a[JsError]
    }

  }

  "TaxGroup" must {

    "serialize and deserialize TaxGroup correctly" in new Setup {

      val expectedTaxGroup: TaxGroup = TaxGroup(
        taxGroupDescription = CustomsDuty,
        amount = fiveHundred,
        taxTypes = Seq(taxType)
      )

      val serialized: JsValue    = Json.toJson(expectedTaxGroup)
      val deserialized: TaxGroup = serialized.as[TaxGroup]

      deserialized mustBe expectedTaxGroup
    }

    "handle empty taxTypes sequence in TaxGroup" in new Setup {

      val taxGroupWithEmptyTaxTypes: TaxGroup = TaxGroup(
        taxGroupDescription = CustomsDuty,
        amount = fiveHundred,
        taxTypes = Seq.empty
      )

      val serialized: JsValue    = Json.toJson(taxGroupWithEmptyTaxTypes)
      val deserialized: TaxGroup = serialized.as[TaxGroup]

      deserialized mustBe taxGroupWithEmptyTaxTypes
    }

    "handle invalid JSON" in new Setup {

      val invalidJson: JsValue = Json.obj("taxGroupDescription" -> "CustomsDuty", "amount" -> hundred)

      intercept[RuntimeException] {
        invalidJson.as[TaxGroup]
      }
    }

    "fail to deserialize TaxGroup with incorrect types" in new Setup {
      val invalidJson: JsValue      = Json.obj("type" -> 123)
      val result: JsResult[TaxType] = Json.fromJson[TaxType](invalidJson)
      result mustBe a[JsError]
    }
  }

  trait Setup {

    val hundred: BigDecimal     = BigDecimal(100.50)
    val twoHundred: BigDecimal  = BigDecimal(200.50)
    val fiveHundred: BigDecimal = BigDecimal(500.00)

    val taxType: TaxType = TaxType(Some("Reason"), "VAT", hundred)

    val taxGroup: TaxGroup = TaxGroup(
      taxGroupDescription = CustomsDuty,
      amount = fiveHundred,
      taxTypes = Seq(taxType)
    )
  }
}
