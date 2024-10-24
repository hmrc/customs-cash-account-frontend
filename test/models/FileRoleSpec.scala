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

import models.FileRole.*
import play.api.libs.json.{JsResult, JsString, JsSuccess, JsValue, Json}
import utils.SpecBase

class FileRoleSpec extends SpecBase {

  "FileRole" should {

    "return CashStatement" in {
      val fileRole = FileRole("CDSCashAccount")
      fileRole mustBe FileRole.CDSCashAccount
    }

    "throw an exception for invalid role" in {
      assertThrows[Exception] {
        FileRole("UnknownRole")
      }
    }
  }

  "FileRole serialization and deserialization" should {

    "serialize CashStatement correctly" in {
      val fileRole: FileRole = FileRole.CDSCashAccount
      val json: JsValue = Json.toJson(fileRole)

      json mustBe JsString("CDSCashAccount")
    }

    "deserialize CashStatement correctly" in {
      val json: JsValue = JsString("CDSCashAccount")
      val result: JsResult[FileRole] = Json.fromJson[FileRole](json)

      result mustBe JsSuccess(FileRole.CDSCashAccount)
    }
  }
}
