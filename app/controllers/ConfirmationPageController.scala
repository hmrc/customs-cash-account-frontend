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
import connectors.CustomsDataStoreConnector
import controllers.actions.IdentifierAction
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Utils.emptyString
import repositories.RequestedTransactionsCache
import views.html.confirmation_page
import helpers.Formatters.dateAsMonthAndYear
import models.CashTransactionDates
import play.api.i18n.Messages
import models.request.IdentifierRequest
import play.api.Logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmationPageController @Inject()(override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           cache: RequestedTransactionsCache,
                                           view: confirmation_page,
                                           customsDataStoreConnector: CustomsDataStoreConnector)
                                          (implicit mcc: MessagesControllerComponents,
                                           ec: ExecutionContext,
                                           appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  private val log = Logger(this.getClass)

  def onPageLoad(): Action[AnyContent] = identify.async {

    implicit request =>

      val result: Future[Result] = for {
        dates <- cache.get(request.eori)
        email <- customsDataStoreConnector.getEmail(request.eori)
      } yield {
        email match {
          case Right(email) =>
            checkDatesAndRedirect(dates, email.value)
          case Left(_) =>
            checkDatesAndRedirect(dates, emptyString)
        }
      }

      result.recover {
        case e: Exception =>
          log.error(s"filed to load ConfirmationPageController $e")
          Redirect(routes.CashAccountController.showAccountUnavailable)
      }
  }

  private def checkDatesAndRedirect(optionalDates: Option[CashTransactionDates], email: String)
                                   (implicit request: IdentifierRequest[AnyContent],
                                    messages: Messages): Result = {
    optionalDates match {
      case Some(dates) =>

        val startDate = dateAsMonthAndYear(dates.start)
        val endDate = dateAsMonthAndYear(dates.end)

        Ok(view(s"$startDate ${messages("month.to")} $endDate", email))

      case _ =>
        log.error(s"filed to load checkDatesAndRedirect $optionalDates")
        Redirect(routes.CashAccountController.showAccountUnavailable)
    }
  }
}
