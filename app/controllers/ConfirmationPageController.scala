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

import cats.data.EitherT.fromOptionF
import config.AppConfig
import connectors.CustomsDataStoreConnector
import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Utils.emptyString
import repositories.RequestedTransactionsCache
import views.html.confirmation_page
import helpers.Formatters.dateAsMonthAndYear

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate

class ConfirmationPageController @Inject()(override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           cache: RequestedTransactionsCache,
                                           customsDataStoreConnector: CustomsDataStoreConnector,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: confirmation_page)
                                          (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify.async {

    implicit request =>

      val fromDate = LocalDate.parse("2020-11-06")
      val toDate = LocalDate.parse("2020-12-08")

      val displayDate = dateAsMonthAndYear(fromDate)

        val result = for {
          dates <- cache.get(request.eori)
        } yield Ok(view(displayDate))

      result.recover {
        case e => Ok(view(displayDate))
      }
  }
}
