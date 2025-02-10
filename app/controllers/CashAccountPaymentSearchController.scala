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
import models.response.CashAccountTransactionSearchResponseDetail
import models.CashAccount
import models.request.IdentifierRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CashAccountSearchRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Utils.{buildCacheId, extractNumericValue}
import viewmodels.PaymentSearchResultsViewModel
import views.html.{cash_account_declaration_details_search_no_result, cash_account_payment_search}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CashAccountPaymentSearchController @Inject() (
  authenticate: IdentifierAction,
  verifyEmail: EmailAction,
  apiConnector: CustomsFinancialsApiConnector,
  eh: ErrorHandler,
  mcc: MessagesControllerComponents,
  paymentSearchView: cash_account_payment_search,
  searchRepository: CashAccountSearchRepository,
  noSearchResultView: cash_account_declaration_details_search_no_result
)(implicit executionContext: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  def search(searchValue: String, page: Option[Int]): Action[AnyContent] = (authenticate andThen verifyEmail).async {
    implicit request =>
      apiConnector.getCashAccount(request.eori).flatMap {
        case Some(account) => processCashAccountDetails(searchValue, account, page)
        case None          => eh.notFoundTemplate.map(NotFound(_))
      }
  }

  private def processCashAccountDetails(searchValue: String, account: CashAccount, page: Option[Int])(implicit
    request: IdentifierRequest[AnyContent]
  ) =
    searchRepository.get(buildCacheId(account.number, extractNumericValue(searchValue))).flatMap {
      case Some(paymentTransfers) => processPaymentTransfersAndDisplayView(searchValue, account, paymentTransfers, page)
      case None                   => Future.successful(Ok(noSearchResultView(page, account.number, searchValue)))
    }

  private def processPaymentTransfersAndDisplayView(
    searchValue: String,
    account: CashAccount,
    paymentTransfers: CashAccountTransactionSearchResponseDetail,
    page: Option[Int]
  )(implicit request: IdentifierRequest[AnyContent]) =
    paymentTransfers.paymentsWithdrawalsAndTransfers match {
      case Some(paymentsWithdrawalsAndTransfers) =>
        val paymentTransfersList = paymentsWithdrawalsAndTransfers.map(_.paymentsWithdrawalsAndTransfer)

        Future.successful(
          Ok(paymentSearchView(PaymentSearchResultsViewModel(searchValue, account, paymentTransfersList, page)))
        )

      case None => Future.successful(Ok(noSearchResultView(page, account.number, searchValue)))
    }
}
