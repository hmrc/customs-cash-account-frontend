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
import connectors.{
  BadRequest, CustomsFinancialsApiConnector, DuplicateAckRef, InternalServerErrorErrorResponse,
  InvalidCashAccount, InvalidDeclarationReference, InvalidEori, NoAssociatedDataFound, ServiceUnavailableErrorResponse,
  UnknownException
}
import models.request.{CashAccountPaymentDetails, DeclarationDetailsSearch, SearchType}
import models.response.{
  CashAccountTransactionSearchResponseDetail, DeclarationSearch, DeclarationWrapper,
  TaxGroupSearch, TaxGroupWrapper, TaxTypeWithSecurity, TaxTypeWithSecurityContainer
}
import models.{
  AccountStatusOpen, CDSCashBalance, CashAccount, CashDailyStatement, CashTransactions,
  Declaration, Payment, Transaction, Withdrawal
}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.when
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future
import views.html.cash_account_declaration_details_search_no_result

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

      val cashAccountTransactionSearchResponseDetail: CashAccountTransactionSearchResponseDetail =
        CashAccountTransactionSearchResponseDetail(
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
        .thenReturn(Future.successful(Right(cashAccountTransactionSearchResponseDetail)))

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

    "return NOT_FOUND when an BAD_REQUEST occurs during retrieval" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
        eqTo(cashAccountNumber),
        eqTo(eori),
        any[SearchType.Value],
        any[Option[DeclarationDetailsSearch]],
        any[Option[CashAccountPaymentDetails]]
      )(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(BadRequest)))

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

    "return NOT_FOUND when an INTERNAL_SERVER_ERROR occurs during retrieval" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
        eqTo(cashAccountNumber),
        eqTo(eori),
        any[SearchType.Value],
        any[Option[DeclarationDetailsSearch]],
        any[Option[CashAccountPaymentDetails]]
      )(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(InternalServerErrorErrorResponse)))

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

    "return NOT_FOUND when an SERVICE_UNAVAILABLE occurs during retrieval" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
        eqTo(cashAccountNumber),
        eqTo(eori),
        any[SearchType.Value],
        any[Option[DeclarationDetailsSearch]],
        any[Option[CashAccountPaymentDetails]]
      )(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(ServiceUnavailableErrorResponse)))

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

    "return an OK and display Declaration search no result page when declarationOpt is None" in new Setup {
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

      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector]
          .toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
            .withSession("eori" -> eori)

        implicit val msgs: Messages = messages(app)
        implicit val config: AppConfig = appConfig(app)

        val expectedView = app.injector.instanceOf[cash_account_declaration_details_search_no_result].apply(
          Some(1), cashAccountNumber, searchInput).body

        val result = route(app, request).value
        status(result) mustEqual OK

        contentAsString(result) mustEqual expectedView
      }
    }

    "return OK and display Declaration search no result page" when {
      "ETMP returns Invalid Cash Account Error" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
          eqTo(cashAccountNumber),
          eqTo(eori),
          any[SearchType.Value],
          any[Option[DeclarationDetailsSearch]],
          any[Option[CashAccountPaymentDetails]]
        )(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(InvalidCashAccount)))

        val app: Application = application
          .overrides(bind[CustomsFinancialsApiConnector]
            .toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
              .withSession("eori" -> eori)

          implicit val msgs: Messages = messages(app)
          implicit val config: AppConfig = appConfig(app)

          val expectedView = app.injector.instanceOf[cash_account_declaration_details_search_no_result].apply(
            Some(1), cashAccountNumber, searchInput).body

          val result = route(app, request).value
          status(result) mustEqual OK

          contentAsString(result) mustBe expectedView
        }
      }

      "ETMP returns Invalid Declaration Reference error" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
          eqTo(cashAccountNumber),
          eqTo(eori),
          any[SearchType.Value],
          any[Option[DeclarationDetailsSearch]],
          any[Option[CashAccountPaymentDetails]]
        )(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(InvalidDeclarationReference)))

        val app: Application = application
          .overrides(bind[CustomsFinancialsApiConnector]
            .toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
              .withSession("eori" -> eori)

          implicit val msgs: Messages = messages(app)
          implicit val config: AppConfig = appConfig(app)

          val expectedView = app.injector.instanceOf[cash_account_declaration_details_search_no_result].apply(
            Some(1), cashAccountNumber, searchInput).body

          val result = route(app, request).value
          status(result) mustEqual OK

          contentAsString(result) mustBe expectedView
        }
      }

      "ETMP returns Duplicate Acknowledge Reference error" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
          eqTo(cashAccountNumber),
          eqTo(eori),
          any[SearchType.Value],
          any[Option[DeclarationDetailsSearch]],
          any[Option[CashAccountPaymentDetails]]
        )(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(DuplicateAckRef)))

        val app: Application = application
          .overrides(bind[CustomsFinancialsApiConnector]
            .toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
              .withSession("eori" -> eori)

          implicit val msgs: Messages = messages(app)
          implicit val config: AppConfig = appConfig(app)

          val expectedView = app.injector.instanceOf[cash_account_declaration_details_search_no_result]
            .apply(Some(1), cashAccountNumber, searchInput).body

          val result = route(app, request).value
          status(result) mustEqual OK

          contentAsString(result) mustBe expectedView
        }
      }

      "ETMP returns No Associated Data Found error" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
          eqTo(cashAccountNumber),
          eqTo(eori),
          any[SearchType.Value],
          any[Option[DeclarationDetailsSearch]],
          any[Option[CashAccountPaymentDetails]]
        )(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(NoAssociatedDataFound)))

        val app: Application = application
          .overrides(bind[CustomsFinancialsApiConnector]
            .toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
              .withSession("eori" -> eori)

          implicit val msgs: Messages = messages(app)
          implicit val config: AppConfig = appConfig(app)

          val expectedView = app.injector.instanceOf[cash_account_declaration_details_search_no_result]
            .apply(Some(1), cashAccountNumber, searchInput).body

          val result = route(app, request).value
          status(result) mustEqual OK

          contentAsString(result) mustBe expectedView
        }
      }

      "ETMP returns Owner EORI not belongs to the Cash Account error" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsBySearch(
          eqTo(cashAccountNumber),
          eqTo(eori),
          any[SearchType.Value],
          any[Option[DeclarationDetailsSearch]],
          any[Option[CashAccountPaymentDetails]]
        )(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(InvalidEori)))

        val app: Application = application
          .overrides(bind[CustomsFinancialsApiConnector]
            .toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.DeclarationDetailController.displaySearchDetails(Some(1), searchInput).url)
              .withSession("eori" -> eori)

          implicit val msgs: Messages = messages(app)
          implicit val config: AppConfig = appConfig(app)

          val expectedView = app.injector.instanceOf[cash_account_declaration_details_search_no_result]
            .apply(Some(1), cashAccountNumber, searchInput).body

          val result = route(app, request).value
          status(result) mustEqual OK

          contentAsString(result) mustBe expectedView
        }
      }
    }
  }

  trait Setup {

    val cashAccountNumber = "1234567"
    val eori = "exampleEori"
    val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"
    val searchInput = "21GB399145852YAD23"

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockErrorHandler: ErrorHandler = mock[ErrorHandler]

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
