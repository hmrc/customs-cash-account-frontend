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

import models.domain.{CAN, EORI}
import play.api.libs.json.{JsValue, Json, Writes}

import java.time.LocalDate

case class AuditModel(auditType: String, transactionName: String, detail: JsValue)

case class CashCsvAuditData(eori: EORI,
                            cashAccountNumber: CAN,
                            asOfDateTime: String,
                            fileFormat: String,
                            from: LocalDate,
                            to:LocalDate)

object CashCsvAuditData {
  implicit val cashCsvAuditDataWrites: Writes[CashCsvAuditData] = Json.writes[CashCsvAuditData]
}

