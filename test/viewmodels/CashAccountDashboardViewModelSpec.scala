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

package viewmodels

import models.domain.MRN
import models.{CashDailyStatement, CashTransactions, Declaration, Payment, Transaction, Withdrawal}
import utils.SpecBase
import utils.TestData.*

import java.time.LocalDate

class CashAccountDashboardViewModelSpec extends SpecBase {

  "apply" should {

    "return correct pendingTransactions in descending order of date" in new Setup {
      val dashBoardViewModel: CashAccountDashboardViewModel = CashAccountDashboardViewModel(cashTransactions)

      dashBoardViewModel.pendingTransactions mustBe Seq(declaration2, declaration1)
      dashBoardViewModel.hasTransactions mustBe true
    }

    "return correct dailyStatements in descending order of date" in new Setup {
      val dashBoardViewModel: CashAccountDashboardViewModel = CashAccountDashboardViewModel(cashTransactions)

      dashBoardViewModel.cashDailyStatements mustBe Seq(dailyStatement2, dailyStatement1)
      dashBoardViewModel.hasTransactions mustBe true
    }

    "return correct output " in new Setup {
      val cashAccDashboardVieModelWithNoTransaction: CashAccountDashboardViewModel =
        CashAccountDashboardViewModel(cashTransactions.copy(pendingTransactions = Seq(), cashDailyStatements = Seq()))

      cashAccDashboardVieModelWithNoTransaction.hasTransactions mustBe false
    }
  }

  trait Setup {

    val declaration1: Declaration =
      Declaration(MOVEMENT_REF_NUMBER, Some(EORI_NUMBER), EORI_NUMBER, Some(DECLARANT_REF), DATE, AMOUNT,
        Seq(TAX_GROUP), Some(SECURE_MOVEMENT_REF_NUMBER))

    val declaration2: Declaration =
      Declaration(MOVEMENT_REF_NUMBER, Some(EORI_NUMBER), EORI_NUMBER, Some(DECLARANT_REF), DATE_1, AMOUNT,
        Seq(TAX_GROUP), Some(SECURE_MOVEMENT_REF_NUMBER))


    val pendingTransactions: Seq[Declaration] = Seq(declaration1, declaration2)

    val date1: LocalDate = LocalDate.parse("2020-07-18")
    val date2: LocalDate = LocalDate.parse("2020-07-20")

    val declaration3: Declaration = declaration1.copy(date = date1)
    val declaration4: Declaration = declaration2.copy(date = date2)

    private val otherTransactions =
      Seq(Transaction(123.45, Payment, None), Transaction(-432.87, Withdrawal, Some("77665544")))

    val dailyStatement1: CashDailyStatement =
      CashDailyStatement(date1, 500.0, 1000.00, Seq(declaration3, declaration4), otherTransactions)

    val dailyStatement2: CashDailyStatement =
      CashDailyStatement(date2, 600.0, 1200.00, Seq(declaration3, declaration4), otherTransactions)

    val dailyStatements: Seq[CashDailyStatement] = Seq(dailyStatement1, dailyStatement2)

    val cashTransactions: CashTransactions = CashTransactions(pendingTransactions, dailyStatements)
  }

}
