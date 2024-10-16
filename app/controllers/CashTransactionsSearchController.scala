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
import forms.SearchTransactionsFormProvider
import helpers.CashAccountUtils
import models.request.*
import models.response.{CashAccountTransactionSearchResponseDetail, PaymentsWithdrawalsAndTransfer}
import models.{CashAccount, CashTransactions}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.CashAccountSearchCacheRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.RegexPatterns.{mrnRegex, paymentRegex}
import utils.Utils.formCacheId
import viewmodels.{DeclarationDetailSearchViewModel, DeclarationDetailViewModel, PaymentSearchResultsViewModel, ResultsPageSummary}
import views.html.{cash_account_declaration_details, cash_account_declaration_details_search, cash_account_payment_search, cash_transactions_no_result}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CashTransactionsSearchController @Inject()(authenticate: IdentifierAction,
                                                 verifyEmail: EmailAction,
                                                 apiConnector: CustomsFinancialsApiConnector,
                                                 errorHandler: ErrorHandler,
                                                 mcc: MessagesControllerComponents,
                                                 view: cash_account_declaration_details,
                                                 searchView: cash_account_declaration_details_search,
                                                 paymentSearchView: cash_account_payment_search,
                                                 cashAccountUtils: CashAccountUtils,
                                                 noTransactionsView: cash_transactions_no_result,
                                                 formProvider: SearchTransactionsFormProvider,
                                                 searchCacheRepository: CashAccountSearchCacheRepository,
                                                 eh: ErrorHandler
                                                )(implicit executionContext: ExecutionContext,
                                                  appConfig: AppConfig
                                                ) extends FrontendController(mcc) with I18nSupport with Logging {


  def search(searchValue: String,
             page: Option[Int]): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>

    apiConnector.getCashAccount(request.eori).flatMap {
      case Some(account) =>

        val uuid = formCacheId(account.number,searchValue)

        searchCacheRepository.get(uuid).flatMap {
          case Some(paymentTransfersList) =>
            paymentTransfersList.paymentsWithdrawalsAndTransfers match {
              case Some(seqOfPaymentsWithdrawalsAndTransfers) =>
                val paymentTransfersList = seqOfPaymentsWithdrawalsAndTransfers.map(_.paymentsWithdrawalsAndTransfer)
                Future.successful(Ok(paymentSearchView(PaymentSearchResultsViewModel(searchValue, account, paymentTransfersList, page))))

              case None => Future.successful(NotFound(errorHandler.notFoundTemplate))
            }
          case None => Future.successful(NotFound(errorHandler.notFoundTemplate))
        }
      case None => Future.successful(NotFound(errorHandler.notFoundTemplate))
    }

  }

}
