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
import models.CDSAccountStatus
import play.api.libs.json.{JsValue, Json, JsString, JsSuccess}

class CDSAccountStatusSpec extends SpecBase {

  "CDSAccountStatus" must {

    "Reads" must {
      "AccountStatusOpen must read correctly as jsSuccess AccountStatusOpen" in new Setup {
        val res = CDSAccountStatus.CDSAccountStatusReads.reads(jsOpen)
        res mustBe JsSuccess(AccountStatusOpen)
      }

      "AccountStatusSuspended must read correctly as jsSuccess AccountStatusSuspended" in new Setup {
        val res = CDSAccountStatus.CDSAccountStatusReads.reads(jsSuspended)
        res mustBe JsSuccess(AccountStatusSuspended)
      }

      "AccountStatusClosed must read correctly as jsSuccess AccountStatusClosed" in new Setup {
        val res = CDSAccountStatus.CDSAccountStatusReads.reads(jsClosed)
        res mustBe JsSuccess(AccountStatusClosed)
      }

      "Unknown must read correctly as jsSuccess AccountStatusOpen" in new Setup {
        val res = CDSAccountStatus.CDSAccountStatusReads.reads(jsFailure)
        res mustBe JsSuccess(AccountStatusOpen)
      }
    }
  }

  trait Setup {
    val jsOpen: JsString = JsString("Open")
    val jsSuspended: JsString = JsString("Suspended")
    val jsClosed: JsString = JsString("Closed")
    val jsFailure: JsString = JsString("123")
  }
}
