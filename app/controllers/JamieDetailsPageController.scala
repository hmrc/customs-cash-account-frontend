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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.jamie_details_page

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JamieDetailsPageController @Inject(
                                          jamieDetails: jamie_details_page,
                                         )(implicit mcc: MessagesControllerComponents,
                                           ec: ExecutionContext, config: AppConfig) extends FrontendController(mcc) {
  
  def displayInputValues(name: String, age: Int, niNumber: Option[String]): Action[AnyContent] = Action.async {
    implicit request => Future.successful(Ok(jamieDetails(name, age, niNumber)))
  }
}