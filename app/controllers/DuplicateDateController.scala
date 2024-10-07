/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.{FrontendBaseController, FrontendController}
import utils.Utils.emptyString
import repositories.RequestedTransactionsCache
import views.html.duplicate_date
import helpers.Formatters.{dateAsDayMonthAndYear, dateAsMonthAndYear}
import models.CashTransactionDates
import play.api.i18n.Messages
import models.request.IdentifierRequest
import play.api.Logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate

class DuplicateDateController @Inject()(override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           cache: RequestedTransactionsCache,
                                           view: duplicate_date)
                                          (implicit mcc: MessagesControllerComponents,
                                           ec: ExecutionContext,
                                           appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  private val log = Logger(this.getClass)

  def onPageLoad(): Action[AnyContent] = identify.async {

    implicit request =>

      val result: Future[Result] = for {
        dates: Option[CashTransactionDates] <- cache.get(request.eori)
      } yield {
        checkDatesAndRedirect(dates)
      }

      result.recover {
        case e: Exception =>
          log.error(s"filed to load ConfirmationPageController $e")
          Redirect(routes.CashAccountController.showAccountUnavailable)
      }
  }

  private def checkDatesAndRedirect(optionalDates: Option[CashTransactionDates])
                                   (implicit request: IdentifierRequest[AnyContent],
                                    messages: Messages): Result = {
    optionalDates match {
      case Some(dates) =>

        val startDate = dateAsMonthAndYear(dates.start)
        val endDate = dateAsMonthAndYear(dates.end)

        val displayMsg: String = messages("cf.cash-account.duplicate.message", startDate, endDate)

        Ok(view(displayMsg))

      case _ =>
        log.error(s"filed to load checkDatesAndRedirect $optionalDates")
        Redirect(routes.CashAccountController.showAccountUnavailable)
    }
  }
}
