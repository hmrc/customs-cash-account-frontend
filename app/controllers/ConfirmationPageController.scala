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

      val fromDate = Future.successful(LocalDate.parse("2020-11-06"))

        val result = for {
          dates <- cache.get(request.eori)
        } yield Ok(view(fromDate.toString))

      result.recover {
        case e => Ok(view(fromDate.toString))
      }

 /*     val result: EitherT[Future, Result, Result] = for {
        dates <- fromOptionF(cache.get(request.eori), Redirect(routes.SelectTransactionsController.onPageLoad()))
        account <- fromOptionF(apiConnector.getCashAccount(request.eori), NotFound(eh.notFoundTemplate))
        page <- EitherT.liftF(showAccountWithTransactionDetails(account, dates.start, dates.end))
      } yield page*/

  }
}
