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
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, MaxTransactionsExceeded}
import controllers.actions.{EmailAction, IdentifierAction}
import helpers.CashAccountUtils
import models.*
import models.request.IdentifierRequest
import org.slf4j.LoggerFactory
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.{CashTransactionsViewModel, CashAccountV2ViewModel}
import views.html.*

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import forms.SearchTransactionsFormProvider
import play.api.data.Form

class CashAccountV2Controller @Inject()(authenticate: IdentifierAction,
                                        verifyEmail: EmailAction,
                                        apiConnector: CustomsFinancialsApiConnector,
                                        showAccountsView: cash_account_v2,
                                        unavailable: cash_account_not_available,
                                        transactionsUnavailable: cash_account_transactions_not_available,
                                        noTransactions: cash_account_no_transactions,
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

      case Left(errorResponse) => errorResponse match {
        case NoTransactionsAvailable => account.balances.AvailableAccountBalance match {
          case Some(v) if v == 0 => Ok(noTransactions(CashAccountViewModel(req.eori, account)))
          case Some(_) => Ok(noTransactionsWithBalance(CashAccountViewModel(req.eori, account)))
          case None => Ok(noTransactions(CashAccountViewModel(req.eori, account)))
        }

        case TooManyTransactionsRequested => Redirect(routes.CashAccountV2Controller.tooManyTransactions())

        case MaxTransactionsExceeded =>
          Ok(showAccountsView(form, CashAccountV2ViewModel(req.eori, account, CashTransactions(Seq(), Seq()))))

        case _ => Ok(transactionsUnavailable(CashAccountViewModel(req.eori, account),
          appConfig.transactionsTimeoutFlag))
      }

      case Right(cashTransactions) =>
        if (cashTransactions.availableTransactions) {
          Ok(
            showAccountsView(form, CashAccountV2ViewModel(req.eori, account, cashTransactions))
          )
        } else {
          Ok(noTransactionsWithBalance(CashAccountViewModel(req.eori, account)))
        }
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
