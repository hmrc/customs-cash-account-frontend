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
import connectors.CustomsFinancialsApiConnector
import forms.JamieFormProvider
import models.JamieFormFields
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{jamie_details_page, jamie_input_page}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JamieDetailsPageController @Inject(
                                          jamieDetails: jamie_details_page,
                                          apiConnector: CustomsFinancialsApiConnector,
                                        )(implicit mcc: MessagesControllerComponents,
                                          ec: ExecutionContext, config: AppConfig) extends FrontendController(mcc) {

  def getNiNumberAndDisplay(name: String, age: Int): Action[AnyContent] = Action.async { implicit request =>
    apiConnector.getNiNumber(name).flatMap {
      case Right(personDetails) =>
        Future.successful(Ok(jamieDetails(name, age, Some(personDetails.niNumber))))
      case Left(_) =>
        Future.successful(Ok(jamieDetails(name, age, None)))
    }
  }
}
