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

import config.AppConfig
import connectors.*
import models.response.*
import models.*
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.when
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.CashAccountSearchRepository
import utils.SpecBase
import utils.TestData.{PAYMENT_SEARCH_VALUE, SEQ_PAYMENT_DETAILS_CONTAINER_01}

import scala.concurrent.Future

class CashAccountPaymentSearchControllerSpec extends SpecBase {

  "search" must {

    "return OK with transaction details when search response is available in cache repository" in new Setup {

      val cashAccountTransactionSearchResponseDetail: CashAccountTransactionSearchResponseDetail =
        CashAccountTransactionSearchResponseDetail(can = cashAccountNumber, eoriDetails = Seq.empty,
          declarations = None, paymentsWithdrawalsAndTransfers = Some(SEQ_PAYMENT_DETAILS_CONTAINER_01))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCashAccountSearchRepo.get(any[String]))
        .thenReturn(Future.successful(Some(cashAccountTransactionSearchResponseDetail)))

      running(app) {
        val request =
          FakeRequest(GET, routes.CashAccountPaymentSearchController.search(PAYMENT_SEARCH_VALUE, Some(1)).url)
            .withSession("eori" -> eori)

        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }

    "return OK with no-result page when search response is unavailable in cache repository" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCashAccountSearchRepo.get(any[String]))
        .thenReturn(Future.successful(None))

      running(app) {
        val request =
          FakeRequest(GET, routes.CashAccountPaymentSearchController.search(PAYMENT_SEARCH_VALUE, Some(1)).url)
            .withSession("eori" -> eori)

        val result = route(app, request).value
        val htmlContent = contentAsString(result)

        status(result) mustEqual OK
        htmlContent.contains(noResultsReturnedMessage) mustBe true
      }
    }

    "return OK with no-result page when no transactions are available in response detail" in new Setup {

      val cashAccountTransactionSearchResponseDetail: CashAccountTransactionSearchResponseDetail =
        CashAccountTransactionSearchResponseDetail(can = cashAccountNumber, eoriDetails = Seq.empty,
          declarations = None, paymentsWithdrawalsAndTransfers = None)

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCashAccountSearchRepo.get(any[String]))
        .thenReturn(Future.successful(Some(cashAccountTransactionSearchResponseDetail)))

      running(app) {
        val request =
          FakeRequest(GET, routes.CashAccountPaymentSearchController.search(PAYMENT_SEARCH_VALUE, Some(1)).url)
            .withSession("eori" -> eori)

        val result = route(app, request).value
        val htmlContent = contentAsString(result)

        status(result) mustEqual OK
        htmlContent.contains(noResultsReturnedMessage) mustBe true
      }
    }
  }

  trait Setup {

    val cashAccountNumber: String = "1234567"
    val eori: String = "exampleEori"

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockCashAccountSearchRepo: CashAccountSearchRepository = mock[CashAccountSearchRepository]

    val cashAccount: CashAccount = CashAccount(
      cashAccountNumber,
      eori,
      AccountStatusOpen,
      CDSCashBalance(Some(BigDecimal(123456.78))))

    val app: Application = applicationBuilder
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[CashAccountSearchRepository].toInstance(mockCashAccountSearchRepo)
      ).build()

    implicit val msgs: Messages = messages(app)

    val noResultsReturnedMessage: String = msgs(
      "cf.cash-account.detail.declaration.search-no-results-guidance-not-returned-any-results", PAYMENT_SEARCH_VALUE)
  }
}
