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

import connectors.*
import models.*
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

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
        .thenReturn(Future.successful(Right(CashTransactions(Seq.empty, Seq.empty, None))))

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

  trait Setup {

    val cashAccountNumber = "1234567"
    val eori = "exampleEori"
    val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"

    val fromDate: LocalDate = LocalDate.parse("2019-10-08")
    val toDate: LocalDate = LocalDate.parse("2020-04-08")

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]

    val cashAccount: CashAccount = CashAccount(
      cashAccountNumber,
      eori,
      AccountStatusOpen,
      CDSCashBalance(Some(BigDecimal(123456.78)))
    )

    val listOfPendingTransactions: Seq[Declaration] = Seq(
      Declaration("pendingDeclarationID", Some("pendingImporterEORI"), "pendingDeclarantEORINumber",
        Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil, Some(sMRN))
    )

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

    val cashTransactionResponse: CashTransactions = CashTransactions(
      listOfPendingTransactions, cashDailyStatements, None)
  }
}
