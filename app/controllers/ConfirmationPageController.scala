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

import cats.data.EitherT
import cats.data.EitherT.fromOptionF
import config.AppConfig
import connectors.CustomsDataStoreConnector
import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Utils.emptyString
import repositories.RequestedTransactionsCache
import views.html.confirmation_page
import helpers.Formatters.{dateAsDayMonthAndYear, dateAsMonthAndYear}
import play.api.i18n.Messages

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate

class ConfirmationPageController @Inject()(override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           cache: RequestedTransactionsCache,
                                           customsDataStoreConnector: CustomsDataStoreConnector,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: confirmation_page)
                                          (implicit ec: ExecutionContext,
                                           appConfig: AppConfig,
                                           messages: Messages)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(from: LocalDate, end: LocalDate): Action[AnyContent] = identify.async {

    implicit request =>

      val displayDate = dateAsMonthAndYear(from)
      val displayToDate = dateAsDayMonthAndYear(end)

      val result = for {
        dates <- fromOptionF(cache.get(request.eori), Redirect(routes.SelectTransactionsController.onPageLoad()))
      } yield Ok(view(s"$displayDate ${messages("month.to")} $displayToDate"))

      result.merge.recover {
        case e => Redirect(routes.CashAccountController.showAccountUnavailable)
      }
  }
}
