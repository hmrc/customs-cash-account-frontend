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
import connectors.CustomsFinancialsApiConnector
import controllers.actions.{EmailAction, IdentifierAction}
import helpers.CashAccountUtils
import models.request.IdentifierRequest
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
import org.mockito.Mockito.{verify, when}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import utils.SpecBase
import viewmodels.ResultsPageSummary
import views.html.{cash_account_declaration_details, cash_transactions_no_result}

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
    "return an OK view when a transaction is found" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))
      when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request =
          FakeRequest(GET, routes.DeclarationDetailController
            .displaySearchDetails(sMRN, Some(1), searchInput).url)
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
        val request =
          FakeRequest(GET, routes.DeclarationDetailController
            .displaySearchDetails(sMRN, Some(1), searchInput).url)
            .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return a NOT_FOUND when the transaction details not retrieved" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request =
          FakeRequest(GET, routes.DeclarationDetailController
            .displaySearchDetails(sMRN, Some(1), emptyString).url)
            .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustBe NOT_FOUND
      }
    }

    "return a NOT_FOUND when a matching declaration has an empty secureMovementReferenceNumber" in new Setup {
      val matchingDeclaration: Declaration = declaration.copy(secureMovementReferenceNumber = Some(emptyString))

      val cashTransactions: CashTransactions = CashTransactions(
        pendingTransactions = Seq.empty,
        cashDailyStatements = Seq(
          CashDailyStatement(
            date = fromDate,
            openingBalance = 0.0,
            closingBalance = 1000.00,
            declarations = Seq(matchingDeclaration),
            otherTransactions = Seq.empty)))

      when(mockCashAccountUtils.transactionDateRange()).thenReturn((fromDate, toDate))
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))
      when(mockCustomsFinancialsApiConnector
        .retrieveCashTransactions(eqTo(cashAccountNumber), eqTo(fromDate), eqTo(toDate))(any))
        .thenReturn(Future.successful(Right(cashTransactions)))
      when(mockErrorHandler.notFoundTemplate(any())).thenReturn(Html(notFoundText))

      val matchDeclaration: Declaration => Boolean = { declaration =>
        declaration.secureMovementReferenceNumber.exists(_.isEmpty)
      }

      implicit val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(), eori)
      val result: Future[Result] = controller.searchDeclarations(page, searchInput, matchDeclaration)

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe notFoundText
      verify(mockErrorHandler).notFoundTemplate(any())
    }
  }

  ".handleSearchRequest method" should {
    "return NOT_FOUND when given an invalid search input" in new Setup {
      when(mockErrorHandler.notFoundTemplate(any())).thenReturn(Html(notFoundText))

      implicit val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(), eori)
      val result: Future[Result] = controller.handleSearchRequest(page, invalidSearchInput)

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe notFoundText
      verify(mockErrorHandler).notFoundTemplate(any())
    }
  }

  ".isValidMRNUCR method" should {
    "return true for a valid input" in new Setup {
      val validMRN = "MRN1234567890"
      controller.isValidMRNUCR(validMRN) mustBe true
    }

    "return false for an invalid input" in new Setup {
      val invalidInput = "INVALID123"
      controller.isValidMRNUCR(invalidInput) mustBe false
    }
  }

  ".isValidPayment method" should {
    "return true for a valid payment input" in new Setup {
      val validPayment = "£100.00"
      controller.isValidPayment(validPayment) mustBe true
    }

    "return false for an invalid payment input" in new Setup {
      val invalidPayment = "100 dollars"
      controller.isValidPayment(invalidPayment) mustBe false
    }
  }

  ".parsePaymentAmount method" should {
    "correctly parse a valid payment" in new Setup {
      val paymentString = "£123.45"
      val expectedAmount: BigDecimal = BigDecimal(123.45)

      controller.parsePaymentAmount(paymentString) mustBe expectedAmount
    }

    "handle correctly input with additional spaces" in new Setup {
      val paymentString = " £  678.90 "
      val expectedAmount: BigDecimal = BigDecimal(678.90)

      controller.parsePaymentAmount(paymentString) mustBe expectedAmount
    }

    "throw an exception for invalid payment formats" in new Setup {
      val invalidPaymentString = "123.45USD"
      a[NumberFormatException] should be thrownBy controller.parsePaymentAmount(invalidPaymentString)
    }
  }

  ".determineSearchType method" should {
    "return a predicate that matches MRN when input is a valid" in new Setup {
      val validMRN = "MRN1234567890"

      val predicate: Declaration => Boolean = controller.determineSearchType(validMRN)

      val declarationWithMatchingMRN: Declaration = declaration.copy(movementReferenceNumber = validMRN)
      val declarationWithNonMatchingMRN: Declaration = declaration.copy(movementReferenceNumber = "21GB343TUG2")
      val declarationWithNonMatchingUCR: Declaration =
        declaration.copy(movementReferenceNumber = "21GB3ER1JB32", declarantReference = Some("22IH483-ANVR123"))

      predicate(declarationWithMatchingMRN) mustBe true
      predicate(declarationWithNonMatchingMRN) mustBe false
      predicate(declarationWithNonMatchingUCR) mustBe false
    }

    "return a predicate that matches payment amounts when input is a valid" in new Setup {
      val validPayment = "£100.00"

      val predicate: Declaration => Boolean = controller.determineSearchType(validPayment)
      val declaration1: Declaration = declaration.copy(amount = hundred)
      val declaration2: Declaration = declaration.copy(amount = fiveHundred)
      val declaration3: Declaration = declaration.copy(amount = fourHundred)

      predicate(declaration1) mustBe true
      predicate(declaration2) mustBe false
      predicate(declaration3) mustBe false
    }
  }

  ".searchDeclarations method" should {
    "return an OK view with no transactions when no cash account is found" in new Setup {
      when(mockCashAccountUtils.transactionDateRange()).thenReturn((fromDate, toDate))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      implicit val appConfig: AppConfig = mock[AppConfig]

      when(mockNoTransactionsView.apply(any[ResultsPageSummary])(any, any, any))
        .thenReturn(Html(noTransactionsText))

      implicit val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(), eori)
      val result: Future[Result] = controller.searchDeclarations(page, searchInput, _ => false)

      status(result) mustBe OK
      contentAsString(result) must include(noTransactionsText)
    }

    "return an OK view no transaction page when the API returns a Left result" in new Setup {
      when(mockCashAccountUtils.transactionDateRange()).thenReturn((fromDate, toDate))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector
        .retrieveCashTransactions(eqTo(cashAccountNumber), eqTo(fromDate), eqTo(toDate))(any))
        .thenReturn(Future.successful(Left(new Exception(noTransactionsText))))

      when(mockNoTransactionsView.apply(any[ResultsPageSummary])(any, any, any))
        .thenReturn(Html(noTransactionsText))

      implicit val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(), eori)
      val result: Future[Result] = controller.searchDeclarations(page, searchInput, _ => false)

      status(result) mustBe OK
      contentAsString(result) must include(noTransactionsText)
    }

    "redirect to displaySearchDetails when a matching declaration with a valid UUID is found" in new Setup {
      val matchingDeclaration: Declaration = declaration.copy(secureMovementReferenceNumber = Some(sMRN))

      val cashTransactions: CashTransactions = CashTransactions(
        pendingTransactions = Seq.empty,
        cashDailyStatements = Seq(
          CashDailyStatement(
            date = fromDate,
            openingBalance = 0.0,
            closingBalance = 1000.00,
            declarations = Seq(matchingDeclaration),
            otherTransactions = Seq.empty)))

      when(mockCashAccountUtils.transactionDateRange()).thenReturn((fromDate, toDate))
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))
      when(mockCustomsFinancialsApiConnector
        .retrieveCashTransactions(eqTo(cashAccountNumber), eqTo(fromDate), eqTo(toDate))(any))
        .thenReturn(Future.successful(Right(cashTransactions)))

      val matchDeclaration: Declaration => Boolean = _.secureMovementReferenceNumber.contains(sMRN)

      implicit val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(), eori)
      val result: Future[Result] = controller.searchDeclarations(page, searchInput, matchDeclaration)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.DeclarationDetailController
        .displaySearchDetails(sMRN, page, searchInput).url)
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
  }
}
