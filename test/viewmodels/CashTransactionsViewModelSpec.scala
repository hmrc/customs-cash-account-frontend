/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig

import java.time.LocalDate
import models._
import utils.SpecBase
import CashTransactionsViewModel._

class CashTransactionsViewModelSpec extends SpecBase {

  "CashAccountDailyStatementsViewModel" should {

    "sort cash daily statements in date descending order" in new Setup {

      val cashDailyStatementsSortedByDate = Seq(
        CashDailyStatement(LocalDate.parse("2020-07-20"), 600.0, 1200.00,
          Seq(Declaration("mrn4", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil),
            Declaration("mrn3", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil)), Nil),
        CashDailyStatement(LocalDate.parse("2020-07-18"), 500.0, 1000.00,
          Seq(Declaration("mrn2", "Declarant EORI", None, LocalDate.parse("2020-07-18"), -65.00, Nil),
            Declaration("mrn1", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil)), Nil))

      model.cashTransactions.cashDailyStatements.sorted mustBe cashDailyStatementsSortedByDate
    }
  }

  "sort declarations within Cash Daily statements in ascending mrn order" in new Setup {

    val declarationsSortedByMRN = Seq(
      Declaration("mrn1", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.0, Nil),
      Declaration("mrn2", "Declarant EORI", None, LocalDate.parse("2020-07-18"), -65.0, Nil))

    model.cashTransactions.cashDailyStatements.head.declarations.sorted mustBe declarationsSortedByMRN
  }

  "group pending transactions by date, sorts by date descending" in new Setup {
    model.pendingTransactionsGroupedByDate.head mustBe group1
  }

  "include CSV download url" in new Setup {
    model.downloadUrl must endWith("/download-csv")
  }

  "display pending transaction when no daily statements" in new Setup {
    modelWithNoDailyStatement.pendingTransactionsGroupedByDate.head mustBe group1
  }


  "CashDailyStatementViewModel" should {

    "calculates the overall size of the collection" in {
      val someTransactions = Seq(Transaction(123.45, Payment, None),
        Transaction(223.45, Payment, None),
        Transaction(-54.66, Withdrawal, Some("77665544")),
        Transaction(300.00, Transfer, None),
        Transaction(-300.00, Transfer, None))

      val someDeclarations = Seq(Declaration("mrn1", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil))

      val dailyStatement = CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 0.00, someDeclarations, someTransactions)

      dailyStatement.size mustBe 8
    }
  }

  trait Setup {
    val mockAppConfig = mock[AppConfig]
    when(mockAppConfig.numberOfDaysToShow).thenReturn(5)
    val cashDailyStatements = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 500.0, 1000.00,
        Seq(Declaration("mrn2", "Declarant EORI", None, LocalDate.parse("2020-07-18"), -65.00, Nil),
          Declaration("mrn1", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil)), Nil),
      CashDailyStatement(LocalDate.parse("2020-07-20"), 600.0, 1200.00,
        Seq(Declaration("mrn4", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil),
          Declaration("mrn3", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil)), Nil))

    val listOfPendingTransactions = Seq(
      Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-08-05"), -300.00, Nil),
      Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil),
      Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", None, LocalDate.parse("2020-07-21"), -50.00, Nil),
      Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-08-05"), -200.00, Nil)
    )
    val group1 = PaginatedPendingDailyStatement(LocalDate.parse("2020-08-05"),
      Seq(Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-08-05"), -300.00, Nil),
        Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-08-05"), -200.00, Nil)))
    val group2 =
      Seq(Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil),
        Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", None, LocalDate.parse("2020-07-21"), -50.00, Nil))

    val cashTransactions = CashTransactions(listOfPendingTransactions, cashDailyStatements)

    val cashTransactionsWithNoDailyStatement = CashTransactions(listOfPendingTransactions, Seq.empty)

    val model = CashTransactionsViewModel(cashTransactions, Some(1))(mockAppConfig)

    val modelWithNoDailyStatement = CashTransactionsViewModel(cashTransactionsWithNoDailyStatement, None)(mockAppConfig)

  }
}
