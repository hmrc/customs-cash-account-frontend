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

  val PAGE_1 = 1
  val PAGE_2 = 2
  val PAGE_3 = 2
  val PAGE_4 = 4
  val PAGE_5 = 5
  val PAGE_6 = 6
  val PAGE_7 = 7
  val PAGE_8 = 8
  val PAGE_10 = 10
  val PAGE_11 = 11
  val PAGE_20 = 20
  val PAGE_21 = 21
  val PAGE_30 = 30
  val PAGE_40 = 40
  val PAGE_50 = 50
  val PAGE_51 = 51
  val PAGE_100 = 100
  val PAGE_101 = 101
  val PAGE_150 = 150
  val PAGE_201 = 201
  val PAGE_400 = 400
}
