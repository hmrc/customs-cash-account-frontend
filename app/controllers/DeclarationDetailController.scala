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
import models.CashAccount
import models.request.IdentifierRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{cash_account_declaration_details, cash_transactions_no_result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging
import viewmodels.{DeclarationDetailViewModel, ResultsPageSummary}

import java.time.LocalDate

class DeclarationDetailController @Inject()(authenticate: IdentifierAction,
                                            verifyEmail: EmailAction,
                                            apiConnector: CustomsFinancialsApiConnector,
                                            errorHandler: ErrorHandler,
                                            mcc: MessagesControllerComponents,
                                            view: cash_account_declaration_details,
                                            cashAccountUtils: CashAccountUtils,
                                            noTransactions: cash_transactions_no_result
                                           )(implicit executionContext: ExecutionContext,
                                             val appConfig: AppConfig
                                           ) extends FrontendController(mcc) with I18nSupport with Logging {

  def displayDetails(ref: String, page: Option[Int]): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>

      apiConnector.getCashAccount(request.eori).flatMap {

        case Some(account) =>
          val (from, to) = cashAccountUtils.transactionDateRange()
          retrieveCashAccountTransactionAndDisplay(account, from, to, ref, page)

        case None => Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
    }

  private def retrieveCashAccountTransactionAndDisplay(account: CashAccount,
                                                       from: LocalDate,
                                                       to: LocalDate,
                                                       ref: String,
                                                       page: Option[Int])
                                                      (implicit request: IdentifierRequest[_]): Future[Result] = {

    apiConnector.retrieveCashTransactions(account.number, from, to).map {

      case Right(transactions) =>
        transactions.cashDailyStatements
          .flatMap(_.declarations)
          .find(_.secureMovementReferenceNumber.contains(ref))
          .map(declaration => Ok(view(DeclarationDetailViewModel(request.eori, account), declaration, page)))
          .getOrElse(NotFound(errorHandler.notFoundTemplate))

      case Left(_) => Ok(noTransactions(new ResultsPageSummary(from, to)))
    }
  }
}
