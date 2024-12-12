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

import models.{CashDailyStatement, CashTransactions, Declaration, Payment, Transaction, Transfer, Withdrawal}
import helpers.Formatters
import org.scalatest.Assertion
import play.api.i18n.Messages
import utils.SpecBase
import utils.TestData.*
import utils.Utils.{LinkComponentValues, emptyPComponent, h2Component, linkComponent, pComponent}

import java.time.LocalDate

class CashAccountDailyStatementsViewModelSpec extends SpecBase {

  "apply" should {

    "return correct contents for the model" when {

      "cash transactions are available" in new Setup {
        val dailyStatementsViewModel: CashAccountDailyStatementsViewModel =
          CashAccountDailyStatementsViewModel(cashTransactions, None)

        dailyStatementsViewModel.dailyStatements.size must be > 0
        shouldContainCorrectContentsForDailyStatements(dailyStatementsViewModel.dailyStatements)
        shouldContainTransactionForLastSixMonthsHeading(dailyStatementsViewModel)
        dailyStatementsViewModel.hasTransactions mustBe true
      }

      "cash transactions are available, max records per page is 30 and page no is one" in new Setup {
        val dailyStatementsViewModel: CashAccountDailyStatementsViewModel =
          CashAccountDailyStatementsViewModel(cashTransactions, Some(1))

        dailyStatementsViewModel.dailyStatements.size must be > 0
        shouldContainCorrectContentsForDailyStatements(dailyStatementsViewModel.dailyStatements)
        shouldContainTransactionForLastSixMonthsHeading(dailyStatementsViewModel)
        dailyStatementsViewModel.hasTransactions mustBe true
      }

      "cash transactions are available, max records per page is 30 and page no is other than one" in new Setup {
        val dailyStatementsViewModel: CashAccountDailyStatementsViewModel =
          CashAccountDailyStatementsViewModel(cashTransactions, Some(2))

        dailyStatementsViewModel.dailyStatements.size must be(0)
        shouldContainTransactionForLastSixMonthsHeading(dailyStatementsViewModel)
        dailyStatementsViewModel.hasTransactions mustBe true
      }

      "cash transactions are not present" in new Setup {
        val cashAccountDailyStatementsViewModelWithNoTransactions: CashAccountDailyStatementsViewModel =
          CashAccountDailyStatementsViewModel(
            cashTransactions.copy(pendingTransactions = Seq(), cashDailyStatements = Seq()),
            None
          )

        cashAccountDailyStatementsViewModelWithNoTransactions.dailyStatements mustBe empty
        cashAccountDailyStatementsViewModelWithNoTransactions.hasTransactions mustBe false
        shouldContainTransactionForLastSixMonthsHeading(cashAccountDailyStatementsViewModelWithNoTransactions)
        shouldContainNoTransactionFromLastSixMonthsText(cashAccountDailyStatementsViewModelWithNoTransactions)
      }
    }

  }

  "DailyStatementViewModel.compare" should {

    "sort the data in the correct order" in new Setup {
      val expectedListAfterSort: List[DailyStatementViewModel] =
        List(
          DailyStatementViewModel(date = dateAug17, balance = None),
          DailyStatementViewModel(date = dateAug16, balance = None),
          DailyStatementViewModel(date = dateAug15, balance = None),
          DailyStatementViewModel(date = dateAug14, balance = None),
          DailyStatementViewModel(date = dateAug13, balance = None),
          DailyStatementViewModel(date = dateAug12, balance = None),
          DailyStatementViewModel(date = dateAug11, balance = None)
        )

      List(
        DailyStatementViewModel(date = dateAug14, balance = None),
        DailyStatementViewModel(date = dateAug12, balance = None),
        DailyStatementViewModel(date = dateAug11, balance = None),
        DailyStatementViewModel(date = dateAug17, balance = None),
        DailyStatementViewModel(date = dateAug16, balance = None),
        DailyStatementViewModel(date = dateAug15, balance = None),
        DailyStatementViewModel(date = dateAug13, balance = None)
      ).sorted mustBe expectedListAfterSort
    }
  }

  private def shouldContainCorrectContentsForDailyStatements(
    actualStatements: Seq[DailyStatementViewModel]
  )(implicit msgs: Messages): Assertion = {

    val expectedDailyStatements: Seq[DailyStatementViewModel] = populateDailyStatViewModelFromDailyCashTransactions()

    actualStatements.size mustBe expectedDailyStatements.size
  }

  private def shouldContainTransactionForLastSixMonthsHeading(
    dailyStatementsViewModel: CashAccountDailyStatementsViewModel
  )(implicit msgs: Messages): Assertion = {
    val headingComponent = h2Component(
      msgKey = "cf.cash-account.transactions.transactions-for-last-six-months.heading",
      id = Some("transactions-for-last-six-months-heading")
    )

    dailyStatementsViewModel.transForLastSixMonthsHeading mustBe headingComponent
  }

  private def shouldContainNoTransactionFromLastSixMonthsText(
    dailyStatementsViewModel: CashAccountDailyStatementsViewModel
  )(implicit msgs: Messages): Assertion = {
    val paragraphComponent = pComponent(
      messageKey = "cf.cash-account.transactions.no-transactions-for-last-six-months",
      id = Some("no-transactions-for-last-six-months-text")
    )

    dailyStatementsViewModel.noTransFromLastSixMonthsText.getOrElse(emptyPComponent) mustBe paragraphComponent
  }

  private def populateDailyStatViewModelFromDailyCashTransactions()(implicit msgs: Messages) =
    Seq(
      dailyStatementViewModel1,
      dailyStatementViewModel3,
      dailyStatementViewModel5,
      dailyStatementViewModel6,
      dailyStatementViewModel2,
      dailyStatementViewModel4,
      dailyStatementViewModel7,
      dailyStatementViewModel8,
      dailyStatementViewModel9,
      dailyStatementViewModel10
    )

  trait Setup {

    val declaration1: Declaration =
      Declaration(
        MOVEMENT_REF_NUMBER,
        Some(EORI_NUMBER),
        EORI_NUMBER,
        Some(DECLARANT_REF),
        DATE,
        AMOUNT,
        Seq(TAX_GROUP),
        Some(SECURE_MOVEMENT_REF_NUMBER)
      )

    val declaration2: Declaration =
      Declaration(
        MOVEMENT_REF_NUMBER,
        Some(EORI_NUMBER),
        EORI_NUMBER,
        Some(DECLARANT_REF),
        DATE_1,
        AMOUNT,
        Seq(TAX_GROUP),
        Some(SECURE_MOVEMENT_REF_NUMBER)
      )

    val pendingTransactions: Seq[Declaration] = Seq(declaration1, declaration2)

    val date1: LocalDate     = LocalDate.parse("2020-07-18")
    val date2: LocalDate     = LocalDate.parse("2020-07-20")
    val dateAug11: LocalDate = LocalDate.of(YEAR_2021, MONTH_8, DAY_11)
    val dateAug12: LocalDate = LocalDate.of(YEAR_2021, MONTH_8, DAY_12)
    val dateAug13: LocalDate = LocalDate.of(YEAR_2021, MONTH_8, DAY_13)
    val dateAug14: LocalDate = LocalDate.of(YEAR_2021, MONTH_8, DAY_14)
    val dateAug15: LocalDate = LocalDate.of(YEAR_2021, MONTH_8, DAY_15)
    val dateAug16: LocalDate = LocalDate.of(YEAR_2021, MONTH_8, DAY_16)
    val dateAug17: LocalDate = LocalDate.of(YEAR_2021, MONTH_8, DAY_17)

    val declaration3: Declaration = declaration1.copy(date = date1)
    val declaration4: Declaration = declaration2.copy(date = date1)
    val declaration5: Declaration = declaration1.copy(date = date2)
    val declaration6: Declaration = declaration2.copy(date = date2)

    private val otherTransactions =
      Seq(Transaction(123.45, Payment, None), Transaction(-432.87, Withdrawal, Some("77665544")))

    private val otherTransactions1 =
      Seq(Transaction(123.45, Payment, None), Transaction(-432.87, Transfer, Some("77665544")))

    val dailyStatement1: CashDailyStatement =
      CashDailyStatement(date1, 500.0, 1000.00, Seq(declaration3, declaration4), otherTransactions)

    val dailyStatement2: CashDailyStatement =
      CashDailyStatement(date2, 600.0, 1200.00, Seq(declaration5, declaration6), otherTransactions1)

    val dailyStatements: Seq[CashDailyStatement] = Seq(dailyStatement1, dailyStatement2)

    val cashTransactions: CashTransactions = CashTransactions(pendingTransactions, dailyStatements)
  }

  val date1: LocalDate = LocalDate.parse("2020-07-18")
  val date2: LocalDate = LocalDate.parse("2020-07-20")

  val dailyStatementViewModel1: DailyStatementViewModel =
    DailyStatementViewModel(
      date = date2,
      transactionType = Some(
        PaymentType(mrnLink =
          Some(
            linkComponent(
              LinkComponentValues(
                "cf-cash-account.tbd",
                controllers.routes.DeclarationDetailController.displayDetails(SECURE_MOVEMENT_REF_NUMBER, None).url
              )
            )
          )
        )
      ),
      credit = None,
      debit = Some(Formatters.formatCurrencyAmount(AMOUNT)),
      balance = Some(Formatters.formatCurrencyAmount(AMOUNT))
    )

  val dailyStatementViewModel2: DailyStatementViewModel =
    DailyStatementViewModel(
      date = date2,
      transactionType = Some(
        PaymentType(
          mrnLink = Some(
            linkComponent(
              LinkComponentValues(
                "cf-cash-account.tbd",
                controllers.routes.DeclarationDetailController.displayDetails(SECURE_MOVEMENT_REF_NUMBER, None).url
              )
            )
          )
        )
      ),
      credit = None,
      debit = Some(Formatters.formatCurrencyAmount(AMOUNT)),
      balance = None
    )

  val dailyStatementViewModel3: DailyStatementViewModel =
    DailyStatementViewModel(
      date = date1,
      transactionType = Some(
        PaymentType(
          mrnLink = Some(
            linkComponent(
              LinkComponentValues(
                "cf-cash-account.tbd",
                controllers.routes.DeclarationDetailController.displayDetails(SECURE_MOVEMENT_REF_NUMBER, None).url
              )
            )
          )
        )
      ),
      credit = None,
      debit = Some(Formatters.formatCurrencyAmount(AMOUNT)),
      balance = None
    )

  val dailyStatementViewModel4: DailyStatementViewModel =
    DailyStatementViewModel(
      date = date1,
      transactionType = Some(
        PaymentType(
          mrnLink = Some(
            linkComponent(
              LinkComponentValues(
                "cf-cash-account.tbd",
                controllers.routes.DeclarationDetailController.displayDetails(SECURE_MOVEMENT_REF_NUMBER, None).url
              )
            )
          )
        )
      ),
      credit = None,
      debit = Some(Formatters.formatCurrencyAmount(AMOUNT)),
      balance = None
    )

  def dailyStatementViewModel5(implicit msgs: Messages): DailyStatementViewModel =
    DailyStatementViewModel(
      date = date2,
      transactionType = Some(PaymentType(textString = Some(msgs("cf.cash-account.detail.top-up.v2")))),
      credit = Some(Formatters.formatCurrencyAmount(123.45)),
      debit = None,
      balance = None
    )

  def dailyStatementViewModel6(implicit msgs: Messages): DailyStatementViewModel =
    DailyStatementViewModel(
      date = date2,
      transactionType =
        Some(PaymentType(textString = Some(msgs("cf.cash-account.detail.transfer-out.v2", "77665544")))),
      credit = None,
      debit = Some(Formatters.formatCurrencyAmount(-432.87)),
      balance = None
    )

  def dailyStatementViewModel7(implicit msgs: Messages): DailyStatementViewModel =
    DailyStatementViewModel(
      date = date1,
      transactionType = Some(PaymentType(textString = Some(msgs("cf.cash-account.detail.top-up.v2")))),
      credit = Some(Formatters.formatCurrencyAmount(123.45)),
      debit = None,
      balance = None
    )

  val dailyStatementViewModel9: DailyStatementViewModel =
    DailyStatementViewModel(date = date1, balance = Some("123"))

  val dailyStatementViewModel10: DailyStatementViewModel =
    DailyStatementViewModel(date = date2, balance = Some("123456"))

  def dailyStatementViewModel8(implicit msgs: Messages): DailyStatementViewModel =
    DailyStatementViewModel(
      date = date1,
      transactionType = Some(PaymentType(textString = Some(msgs("cf.cash-account.detail.withdrawal")))),
      credit = None,
      debit = Some(Formatters.formatCurrencyAmount(-432.87)),
      balance = None
    )
}
