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
import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.{FrontendBaseController, FrontendController}
import utils.Utils.emptyString
import repositories.RequestedTransactionsCache
import views.html.confirmation_page
import helpers.Formatters.{dateAsDayMonthAndYear, dateAsMonthAndYear}
import models.CashTransactionDates
import play.api.i18n.Messages
import models.request.IdentifierRequest
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate

class ConfirmationPageController @Inject()(override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           cache: RequestedTransactionsCache,
                                           view: confirmation_page)
                                          (implicit mcc: MessagesControllerComponents,
                                           ec: ExecutionContext,
                                           appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async {

    implicit request =>

      val result: Future[Result] = for {
        dates: Option[CashTransactionDates] <- cache.get(request.eori)
      } yield {
        checkDatesAndRedirect(dates)
      }

      result.recover {
        case _: Exception => Redirect(routes.CashAccountController.showAccountUnavailable)
      }
  }

  private def checkDatesAndRedirect(optionalDates: Option[CashTransactionDates])
                                  (implicit request: IdentifierRequest[AnyContent],
                                   messages: Messages): Result = {
    optionalDates match {
      case Some(dates) =>

        val startDate = dateAsMonthAndYear(dates.start)
        val endDate = dateAsMonthAndYear(dates.end)

        Ok(view(s"$startDate ${messages("month.to")} $endDate"))

      case _ => Redirect(routes.CashAccountController.showAccountUnavailable)
    }
  }
}
