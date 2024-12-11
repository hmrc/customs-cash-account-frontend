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

import play.api.libs.json.{JsResult, JsResultException, JsSuccess, JsValue, Json}
import utils.SpecBase

class AuditEoriSpec extends SpecBase {

  "AuditEori" should {

    "create an instance correctly" in {
      val eori       = "GB123456789002"
      val isHistoric = true
      val auditEori  = AuditEori(eori, isHistoric)

      auditEori.eori mustBe eori
      auditEori.isHistoric mustBe isHistoric
    }
  }

  "AuditEori serialization and deserialization" should {

    "serialize correctly" in {
      val auditEori = AuditEori("GB123456789000", isHistoric = false)
      val json      = Json.toJson(auditEori)

      val expectedJson = Json.obj("eori" -> "GB123456789000", "isHistoric" -> false)

      json mustBe expectedJson
    }

    "deserialize correctly" in {
      val json = Json.obj("eori" -> "GB123456789000", "isHistoric" -> false)

      val expectedAuditEori = AuditEori("GB123456789000", isHistoric = false)
      val result            = json.validate[AuditEori]

      result mustBe JsSuccess(expectedAuditEori)
    }

    "fail to deserialize when invalid" in {
      val invalidJson = Json.obj("eori" -> 12345, "isHistoric" -> "notABool")

      assertThrows[JsResultException] {
        invalidJson.as[AuditEori]
      }
    }
  }
}
