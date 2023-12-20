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

package models.email

import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase

class EmailUnverifiedResponseSpec extends SpecBase {

  "Reads" should {
    "generate correct EmailUnverifiedResponse object" in new Setup {

      import EmailUnverifiedResponse.format

      Json.fromJson(Json.parse(emailUnverifiedResponse)) mustBe JsSuccess(emailUnverifiedResponseOb)
    }
  }

  "Writes" should {
    "generate correct JsValue for EmailUnverifiedResponse object" in new Setup {
      Json.toJson(emailUnverifiedResponseOb) mustBe Json.parse(emailUnverifiedResponse)
    }
  }

  trait Setup {
    val emailAddress = "someemail@mail.com"

    val emailUnverifiedResponse: String = """{"unVerifiedEmail":"someemail@mail.com"}""".stripMargin

    val emailUnverifiedResponseOb: EmailUnverifiedResponse =
      EmailUnverifiedResponse(unVerifiedEmail = Some(emailAddress))
  }
}
