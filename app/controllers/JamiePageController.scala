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

import config.{AppConfig, ErrorHandler}
import forms.JamieFormProvider
import models.JamieFormFields
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.*
import play.api.data.Form
import play.api.i18n.Lang.logger
import play.api.i18n.Messages


import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}



class JamiePageController @Inject()(
                                    jamieInput: jamie_input_page,
                                    jamieDetails: jamie_details_page,
                                    jamieForm: JamieFormProvider,
                                    errorHandler: ErrorHandler
                                   )(implicit mcc: MessagesControllerComponents,
  ec: ExecutionContext, config: AppConfig) extends FrontendController(mcc) {

  val form: Form[JamieFormFields] = jamieForm()

  def onPageLoad(): Action[AnyContent] = Action.async {
    implicit request => Future.successful(Ok(jamieInput(form))).recover {
      case e =>
        logger.error("There was an error loading the page")
        NotFound(errorHandler.notFoundTemplate)
    }
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    form.bindFromRequest().fold (
      formWithErrors => {
        showFormWithErrors(formWithErrors)
      },
      userData => Future.successful(
        Redirect(routes.JamiePageController.displayInputValues(userData.name, userData.age)))
    )
  }

  def displayInputValues(name: String, age: Int): Action[AnyContent] = Action.async {
    implicit request => Future.successful(Ok(jamieDetails(name, age)))
  }

  private def showFormWithErrors(formWithErrors: Form[JamieFormFields])
                        (implicit request: Request[AnyContent], messages: Messages): Future[Result] = {
    Future.successful(BadRequest(jamieInput(formWithErrors)))
  }
}