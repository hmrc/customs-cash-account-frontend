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

import cats.instances.future.*
import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested}
import controllers.actions.{EmailAction, IdentifierAction}
import helpers.CashAccountUtils
import models.*
import models.request.IdentifierRequest
import org.slf4j.LoggerFactory
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.CashTransactionsViewModel
import views.html.*

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CashAccountController @Inject() (
  authenticate: IdentifierAction,
  verifyEmail: EmailAction,
  apiConnector: CustomsFinancialsApiConnector,
  showAccountsView: cash_account,
  unavailable: cash_account_not_available,
  transactionsUnavailable: cash_account_transactions_not_available,
  noTransactions: cash_account_no_transactions,
  showAccountsExceededThreshold: cash_account_exceeded_threshold,
  noTransactionsWithBalance: cash_account_no_transactions_with_balance,
  cashAccountUtils: CashAccountUtils,
  cache: RequestedTransactionsCache
)(implicit mcc: MessagesControllerComponents, ec: ExecutionContext, eh: ErrorHandler, appConfig: AppConfig)
    extends FrontendController(mcc)
    with I18nSupport {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def showAccountDetails(page: Option[Int]): Action[AnyContent] = (authenticate andThen verifyEmail).async {
    implicit request =>
      if (appConfig.isCashAccountV2FeatureFlagEnabled) {
        Future.successful(Redirect(routes.CashAccountV2Controller.showAccountDetails(page)))
      } else {

        cache.clear(request.eori).recover { case e =>
          logger.error(s"Cache clear failed: ${e.getMessage}")
        }

        val eventualMaybeCashAccount = apiConnector.getCashAccount(request.eori)

        eventualMaybeCashAccount
          .flatMap {
            case Some(cashAccount) =>
              val (from, to) = cashAccountUtils.transactionDateRange()
              showAccountWithTransactionDetails(cashAccount, from, to, page)
            case None              =>
              eh.notFoundTemplate.map(NotFound(_))
          }
          .recover { case e =>
            logger.error(s"Unable to retrieve account details: ${e.getMessage}")
            Redirect(routes.CashAccountController.showAccountUnavailable)
          }
      }
  }

  private def showAccountWithTransactionDetails(
    account: CashAccount,
    from: LocalDate,
    to: LocalDate,
    page: Option[Int]
  )(implicit req: IdentifierRequest[AnyContent], appConfig: AppConfig): Future[Result] =
    apiConnector.retrieveCashTransactions(account.number, from, to).map {
      case Left(errorResponse) =>
        errorResponse match {
          case NoTransactionsAvailable =>
            account.balances.AvailableAccountBalance match {
              case Some(v) if v == 0 => Ok(noTransactions(CashAccountViewModel(req.eori, account)))
              case Some(_)           => Ok(noTransactionsWithBalance(CashAccountViewModel(req.eori, account)))
              case None              => Ok(noTransactions(CashAccountViewModel(req.eori, account)))
            }

          case TooManyTransactionsRequested => Redirect(routes.CashAccountController.tooManyTransactions())

          case _ =>
            Ok(transactionsUnavailable(CashAccountViewModel(req.eori, account), appConfig.transactionsTimeoutFlag))
        }

      case Right(cashTransactions) =>
        if (cashTransactions.availableTransactions) {
          Ok(
            showAccountsView(CashAccountViewModel(req.eori, account), CashTransactionsViewModel(cashTransactions, page))
          )
        } else {
          Ok(noTransactionsWithBalance(CashAccountViewModel(req.eori, account)))
        }
    }

  def tooManyTransactions(): Action[AnyContent] = authenticate.async { implicit request =>
    apiConnector.getCashAccount(request.eori) flatMap {
      case None          => eh.notFoundTemplate.map(NotFound(_))
      case Some(account) =>
        Future.successful(
          Ok(
            showAccountsExceededThreshold(
              CashAccountViewModel(
                request.eori,
                CashAccount(account.number, account.owner, account.status, account.balances)
              )
            )
          )
        )
    }
  }

  def showAccountUnavailable: Action[AnyContent] = authenticate { implicit req =>
    Ok(unavailable())
  }
}
