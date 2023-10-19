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
import models.TaxGroupType
import play.api.libs.json.{JsValue, JsString, JsSuccess}

class TaxGroupTypeSpec extends SpecBase {
  "taxGroupReads" must {
    "ImportVat must read correctly as jsSuccess" in new Setup {
      val res = TaxGroupType.taxGroupReads.reads(jsImport)
      res mustBe JsSuccess(ImportVat)
    }

    "ExciseDuty must read correctly as jsSuccess" in new Setup {
      val res = TaxGroupType.taxGroupReads.reads(jsExcise)
      res mustBe JsSuccess(ExciseDuty)
    }

    "CustomsDuty must read correctly as jsSuccess" in new Setup {
      val res = TaxGroupType.taxGroupReads.reads(jsCustoms)
      res mustBe JsSuccess(CustomsDuty)
    }

    "Unknown must read correctly as jsSuccess" in new Setup {
      val res = intercept[RuntimeException](TaxGroupType.taxGroupReads.reads(JsString("")))
      res.getMessage mustBe ("Unknown Tax Group Type")
    }
  }

  "taxGroupWrites" must {
    "ImportVat JsString must write correctly" in new Setup {
      val res: JsValue = TaxGroupType.taxGroupWrites.writes(ImportVat)
      res mustBe jsImport
    }

    "ExciseDuty JsString must write correctly" in new Setup {
      val res: JsValue = TaxGroupType.taxGroupWrites.writes(ExciseDuty)
      res mustBe jsExcise
    }

    "CustomsDuty JsString must write correctly" in new Setup {
      val res: JsValue = TaxGroupType.taxGroupWrites.writes(CustomsDuty)
      res mustBe jsCustoms
    }
  }

  trait Setup {
    val jsImport: JsString = JsString("Import VAT")
    val jsExcise: JsString = JsString("Excise")
    val jsCustoms: JsString = JsString("Customs")
  }
}
