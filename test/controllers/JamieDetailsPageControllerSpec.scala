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
import play.api.Application
import play.api.i18n.Messages
import play.api.test.Helpers.{GET, running}
import utils.SpecBase
import play.api.mvc.Result
import play.api.test.Helpers.*
import views.html.jamie_details_page

import scala.concurrent.Future


class JamieDetailsPageControllerSpec extends SpecBase {
  "displayInputValues" should {
    "return Ok" in new Setup {
      val app: Application = application.build()
      implicit val msgs: Messages = messages(app)
      implicit val config: AppConfig = appConfig(app)

      running(app) {
        implicit val request = fakeRequest(GET, routes.JamieDetailsPageController.displayInputValues(name, age, niNumber).url)
        val result: Future[Result] = route(app, request).value
        status(result) mustEqual OK

//        val detailsPage = app.injector.instanceOf[jamie_details_page]
//
//        contentAsString(result) mustEqual detailsPage(name, age, niNumber)

        val content = contentAsString(result)
        content must include("Name: Jamie")
        content must include("Age: 30")
        content must include("NI Number: AB123456C")
      }
    }
  }

  trait Setup {
    val name = "Jamie"
    val age = 30
    val niNumber: Option[String] = Some("AB123456C")
  }
}
