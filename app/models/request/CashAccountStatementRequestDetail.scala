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

package models.request

import java.time.LocalDate
import play.api.libs.json.{Format, JsValue, Json, OWrites, Writes}
import play.api.libs.ws.BodyWritable

case class CashAccountStatementRequestDetail(eori: String, can: String, dateFrom: String, dateTo: String)

object CashAccountStatementRequestDetail {

  implicit val format: Format[CashAccountStatementRequestDetail] = Json.format[CashAccountStatementRequestDetail]
  implicit val writes: Writes[CashAccountStatementRequestDetail] = Json.writes[CashAccountStatementRequestDetail]

  implicit def jsonBodyWritable[T](implicit
    writes: Writes[T],
    jsValueBodyWritable: BodyWritable[JsValue]
  ): BodyWritable[T] = jsValueBodyWritable.map(writes.writes)
}
