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

import cats.data.EitherT.*
import cats.instances.future.*
import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, ErrorResponse, MaxTransactionsExceeded, NoTransactionsAvailable, TooManyTransactionsRequested}
import controllers.actions.{EmailAction, IdentifierAction}
import helpers.CashAccountUtils
import models.*
import models.request.IdentifierRequest
import org.slf4j.LoggerFactory
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.{CashAccountV2ViewModel, CashTransactionsViewModel}
import views.html.*

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import forms.SearchTransactionsFormProvider
import play.api.data.Form

class CashAccountV2Controller @Inject()(authenticate: IdentifierAction,
                                        verifyEmail: EmailAction,
                                        apiConnector: CustomsFinancialsApiConnector,
                                        accountsView: cash_account_v2,
                                        unavailable: cash_account_not_available,
                                        transactionsUnavailable: cash_account_transactions_not_available,
                                        noTransactions: cash_account_no_transactions_v2,
                                        showAccountsExceededThreshold: cash_account_exceeded_threshold,
                                        noTransactionsWithBalance: cash_account_no_transactions_with_balance,
                                        cashAccountUtils: CashAccountUtils,
                                        formProvider: SearchTransactionsFormProvider)
                                       (implicit mcc: MessagesControllerComponents,
                                        ec: ExecutionContext,
                                        eh: ErrorHandler,
                                        appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  val form: Form[String] = formProvider()

  def showAccountDetails(page: Option[Int]): Action[AnyContent] = (authenticate andThen verifyEmail).async {
    implicit request =>

      val eventualMaybeCashAccount = apiConnector.getCashAccount(request.eori)

      val result = for {
        cashAccount <- fromOptionF[Future, Result, CashAccount](eventualMaybeCashAccount, NotFound(eh.notFoundTemplate))
        (from, to) = cashAccountUtils.transactionDateRange()
        page <- liftF[Future, Result, Result](showAccountWithTransactionDetails(cashAccount, from, to, page))
      } yield page

      result.merge.recover {
        case exception =>
          logger.error(s"Unable to retrieve account details: ${exception.getMessage}")
          Redirect(routes.CashAccountV2Controller.showAccountUnavailable)
      }
  }

  def onSubmit(page: Option[Int]): Action[AnyContent] = Action.async {
    Future.successful(NotImplemented("Needs to be implemented"))
  }

  private def showAccountWithTransactionDetails(account: CashAccount,
                                                from: LocalDate,
                                                to: LocalDate,
                                                page: Option[Int])
                                               (implicit req: IdentifierRequest[AnyContent],
                                                appConfig: AppConfig): Future[Result] = {
    apiConnector.retrieveCashTransactions(account.number, from, to).map {

      case Left(errorResponse) => processErrorResponse(account, errorResponse)
      case Right(cashTransactions) => Ok(accountsView(form, CashAccountV2ViewModel(req.eori, account, cashTransactions)))
    }
  }

  private def processErrorResponse(account: CashAccount, errorResponse: ErrorResponse)
                                  (implicit request: IdentifierRequest[AnyContent], appConfig: AppConfig) = {
    errorResponse match {
      case NoTransactionsAvailable => checkBalanceAndDisplayNoTransactionsView(account)

      case TooManyTransactionsRequested => Redirect(routes.CashAccountV2Controller.tooManyTransactions())

      case MaxTransactionsExceeded =>
        Ok(accountsView(form, CashAccountV2ViewModel(request.eori, account, CashTransactions(Seq(), Seq()))))

      case _ => Ok(transactionsUnavailable(CashAccountViewModel(request.eori, account), appConfig.transactionsTimeoutFlag))
    }
  }

  private def checkBalanceAndDisplayNoTransactionsView(account: CashAccount)
                                                      (implicit request: IdentifierRequest[AnyContent]) = {
    val isBrandNewCashAccount = (balance: BigDecimal) => balance == 0

    account.balances.AvailableAccountBalance match {
      case Some(v) if isBrandNewCashAccount(v) => Ok(noTransactions(CashAccountViewModel(request.eori, account)))
      case Some(_) => Ok(noTransactionsWithBalance(CashAccountViewModel(request.eori, account)))
      case None => Ok(noTransactions(CashAccountViewModel(request.eori, account)))
    }
  }

  def tooManyTransactions(): Action[AnyContent] = authenticate.async { implicit request =>
    apiConnector.getCashAccount(request.eori) flatMap {
      case None => Future.successful(NotFound(eh.notFoundTemplate))
      case Some(account) =>
        Future.successful(
          Ok(
            showAccountsExceededThreshold(
              CashAccountViewModel(
                request.eori,
                CashAccount(account.number, account.owner, account.status, account.balances)))
          ))
    }
  }

  def showAccountUnavailable: Action[AnyContent] = authenticate { implicit req =>
    Ok(unavailable())
  }
}
