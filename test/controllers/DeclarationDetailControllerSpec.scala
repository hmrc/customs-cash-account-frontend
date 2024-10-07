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

package controllers

import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, UnknownException}
import controllers.actions.{EmailAction, IdentifierAction}
import helpers.CashAccountUtils
import models.request.{CashAccountPaymentDetails, DeclarationDetailsSearch, SearchType}
import models.response.{
  CashAccountTransactionSearchResponseDetail,
  DeclarationSearch,
  DeclarationWrapper,
  TaxGroupSearch,
  TaxGroupWrapper,
  TaxTypeWithSecurity,
  TaxTypeWithSecurityContainer
}
import models.{
  AccountStatusOpen,
  CDSCashBalance,
  CashAccount,
  CashDailyStatement,
  CashTransactions,
  CustomsDuty,
  Declaration,
  ImportVat,
  Payment,
  TaxGroup,
  TaxType,
  Transaction,
  Withdrawal
}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase
import views.html.{cash_account_declaration_details, cash_account_declaration_details_search, cash_transactions_no_result}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class DeclarationDetailControllerSpec extends SpecBase {

  "Cash Account Declaration Transaction Details" must {
    "return an OK view when a transaction is found" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DeclarationDetailController.displayDetails(sMRN, Some(1)).url)
          .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }

    "return an OK view with no transactions when an error occurs during retrieval" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(new Exception("API error"))))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DeclarationDetailController.displayDetails(sMRN, Some(1)).url)
          .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return a NOT_FOUND when the transaction details not found in the retrieved data" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(CashTransactions(Seq.empty, Seq.empty))))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request =
          FakeRequest(GET, routes.DeclarationDetailController.displayDetails("sMRN not found", Some(1)).url)
            .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustBe NOT_FOUND
      }
    }

    "return a NOT_FOUND when the transaction details not retrieved" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DeclarationDetailController.displayDetails(sMRN, Some(1)).url)
          .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustBe NOT_FOUND
      }
    }
  }

  "Cash Account Declaration Transaction Search Details" must {

    "return a NOT_FOUND when declarationsOpt is None" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
        eqTo(cashAccountNumber),
        eqTo(eori),
        any[SearchType.Value],
        any[Option[DeclarationDetailsSearch]],
        any[Option[CashAccountPaymentDetails]]
      )(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(CashAccountTransactionSearchResponseDetail(
          can = cashAccountNumber,
          eoriDetails = Seq.empty,
          declarations = None,
          paymentsWithdrawalsAndTransfers = None
        ))))

      when(mockErrorHandler.notFoundTemplate(any[Request[_]]))
        .thenReturn(Html("Not Found"))

      val app: Application = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[ErrorHandler].toInstance(mockErrorHandler)
        )
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
          .withSession("eori" -> eori)

        val result = route(app, request).value

        status(result) mustEqual NOT_FOUND
        contentAsString(result) mustEqual "Not Found"
      }
    }

    "return an OK view when a transaction is found" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      val mockCashAccountTransactionSearchResponseDetail: CashAccountTransactionSearchResponseDetail = CashAccountTransactionSearchResponseDetail(
        can = cashAccountNumber,
        eoriDetails = Seq.empty,
        declarations = Some(Seq(declarationWrapper)),
        paymentsWithdrawalsAndTransfers = None)

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
        eqTo(cashAccountNumber),
        eqTo(eori),
        any[SearchType.Value],
        any[Option[DeclarationDetailsSearch]],
        any[Option[CashAccountPaymentDetails]]
      )(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(mockCashAccountTransactionSearchResponseDetail)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
          .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }

    "return an NOT_FOUND when an error occurs during retrieval" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
        eqTo(cashAccountNumber),
        eqTo(eori),
        any[SearchType.Value],
        any[Option[DeclarationDetailsSearch]],
        any[Option[CashAccountPaymentDetails]]
      )(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(UnknownException)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
          .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustEqual NOT_FOUND
      }
    }

    "return a NOT_FOUND when the transaction details not retrieved" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
          .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustEqual NOT_FOUND
      }
    }
  }

  trait Setup {

    val year2024 = 2024
    val month4 = 4
    val day4 = 29
    val date: LocalDate = LocalDate.of(year2024, month4, day4)

    val fiveHundred: BigDecimal = BigDecimal(500.00)
    val fourHundred: BigDecimal = BigDecimal(400.00)
    val hundred: BigDecimal = BigDecimal(100.00)

    val movementReferenceNumber = "MRN1234567890"
    val importerEori: Option[String] = Some("GB123456789000")
    val declarantEori = "GB987654321000"
    val declarantReference: Option[String] = Some("UCR12345")
    val secureMovementReferenceNumber: Option[String] = Some("5a71a767-5c1c-4df8-8eef-2b83769b8fda")

    val cashAccountNumber = "1234567"
    val eori = "exampleEori"
    val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"
    val searchInput = "21GB399145852YAD23"
    val invalidSearchInput = "-2?1G!B399.145852YAD23"
    val page: Option[Int] = Some(1)
    val notFoundText = "Not Found"
    val noTransactionsText = "No Transactions"

    val fromDate: LocalDate = LocalDate.parse("2019-10-08")
    val toDate: LocalDate = LocalDate.parse("2020-04-08")

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockErrorHandler: ErrorHandler = mock[ErrorHandler]
    val mockCashAccountUtils: CashAccountUtils = mock[CashAccountUtils]
    val mockIdentifierAction: IdentifierAction = mock[IdentifierAction]
    val mockEmailAction: EmailAction = mock[EmailAction]
    val mockAppConfig: AppConfig = mock[AppConfig]
    val mockNoTransactionsView: cash_transactions_no_result = mock[cash_transactions_no_result]

    val controller = new DeclarationDetailController(
      mockIdentifierAction,
      mockEmailAction,
      mockCustomsFinancialsApiConnector,
      mockErrorHandler,
      stubMessagesControllerComponents(),
      mock[cash_account_declaration_details],
      mock[cash_account_declaration_details_search],
      mockCashAccountUtils,
      mockNoTransactionsView
    )(ExecutionContext.global, mockAppConfig)

    val taxTypes: Seq[TaxType] = Seq(TaxType(reasonForSecurity = Some("Reason"), taxTypeID = "50", amount = hundred))

    val declaration: Declaration = Declaration(
      movementReferenceNumber = movementReferenceNumber,
      importerEori = importerEori,
      declarantEori = declarantEori,
      declarantReference = declarantReference,
      date = date,
      amount = fiveHundred,
      taxGroups = Seq(
        TaxGroup(CustomsDuty, fourHundred, taxTypes),
        TaxGroup(ImportVat, hundred, taxTypes)
      ), secureMovementReferenceNumber = secureMovementReferenceNumber)

    val cashAccount: CashAccount = CashAccount(
      cashAccountNumber,
      eori,
      AccountStatusOpen,
      CDSCashBalance(Some(BigDecimal(123456.78))))

    val listOfPendingTransactions: Seq[Declaration] = Seq(
      Declaration("pendingDeclarationID", Some("pendingImporterEORI"), "pendingDeclarantEORINumber",
        Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil, Some(sMRN)))

    val cashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 0.0, 1000.00,
        Seq(Declaration("mrn1", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
          LocalDate.parse("2020-07-18"), -84.00, Nil, Some(sMRN)),
          Declaration("mrn2", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
            LocalDate.parse("2020-07-18"), -65.00, Nil, Some(sMRN))),

        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))),

      CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 1200.00,
        Seq(Declaration("mrn3", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
          LocalDate.parse("2020-07-20"), -90.00, Nil, Some(sMRN)),
          Declaration("mrn4", Some("Importer EORI"), "Declarant EORI", Some("Declarant Reference"),
            LocalDate.parse("2020-07-20"), -30.00, Nil, Some(sMRN))),
        Seq(Transaction(67.89, Payment, None))))

    val cashTransactionResponse: CashTransactions = CashTransactions(listOfPendingTransactions, cashDailyStatements)

    val declarationSearch: DeclarationSearch = DeclarationSearch(
      declarationID = "MRN1234567890",
      declarantEORINumber = "GB987654321000",
      declarantRef = Some("UCR12345"),
      c18OrOverpaymentReference = Some("C18Reference"),
      importersEORINumber = "GB123456789000",
      postingDate = "2024-04-29",
      acceptanceDate = "2024-04-28",
      amount = 500.00,
      taxGroups = Seq(
        TaxGroupWrapper(
          TaxGroupSearch(
            taxGroupDescription = "Import VAT",
            amount = 100.00,
            taxTypes = Seq(
              TaxTypeWithSecurityContainer(
                TaxTypeWithSecurity(
                  reasonForSecurity = Some("Security Reason"),
                  taxTypeID = "50",
                  amount = 100.00
                )
              )
            )
          )
        )
      )
    )

    val declarationWrapper: DeclarationWrapper = DeclarationWrapper(declarationSearch)
  }
}
