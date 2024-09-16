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

package utils

import java.time.LocalDate
import models.{TaxGroup, ImportVat, TaxType}

object TestData {
  val MOVEMENT_REF_NUMBER = "GHRT122317AM33E6D6"
  val EORI_NUMBER = "GB1234567890"
  val DECLARANT_REF = "GB1234567890 5569-4213-889 936T"
  val SECURE_MOVEMENT_REF_NUMBER = "5569-4213-889 936T"
  val AMOUNT: BigDecimal = BigDecimal(400.00)

  val DATE: LocalDate = LocalDate.parse("2020-07-21")
  val DATE_1: LocalDate = LocalDate.parse("2020-08-21")

  val TAX_TYPE: TaxType = TaxType(Some("a"), "a", AMOUNT)
  val TAX_GROUP: TaxGroup = TaxGroup(ImportVat, AMOUNT, Seq(TAX_TYPE))
}
