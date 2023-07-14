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

import play.api.libs.json.{JsResult, JsString, JsSuccess, JsValue, Reads, Writes}

sealed trait TaxGroupType {
  def onWire: String
}

object TaxGroupType {
  implicit val taxGroupReads: Reads[TaxGroupType] = new Reads[TaxGroupType]{
    override def reads(json: JsValue): JsResult[TaxGroupType] = {
      json.as[String] match {
        case "Import VAT" => JsSuccess(ImportVat)
        case "Excise" => JsSuccess(ExciseDuty)
        case "Customs" => JsSuccess(CustomsDuty)
        case _ => throw new RuntimeException("Unknown Tax Group Type")
      }
    }
  }

  implicit val taxGroupWrites: Writes[TaxGroupType] = new Writes[TaxGroupType]{
    override def writes(o: TaxGroupType): JsString = JsString{
      o match {
        case ImportVat => "Import VAT"
        case ExciseDuty => "Excise"
        case CustomsDuty => "Customs"
      }
    }
  }
}

case object ImportVat extends TaxGroupType {
  override val onWire: String = "Import VAT"
}

case object ExciseDuty extends TaxGroupType {
  val onWire: String = "Excise"
}

case object CustomsDuty extends TaxGroupType {
  val onWire: String = "Customs"
}
