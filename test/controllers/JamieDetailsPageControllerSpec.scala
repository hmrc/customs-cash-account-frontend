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
import connectors.{BadRequest, CustomsFinancialsApiConnector, InternalServerErrorErrorResponse}
import models.PersonDetails
import org.mockito.Mockito.when
import play.api.Application
import play.api.i18n.Messages
import play.api.test.Helpers.{GET, running}
import utils.SpecBase
import play.api.mvc.Result
import play.api.test.Helpers.*
import views.html.jamie_details_page
import play.api.inject.bind
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future


class JamieDetailsPageControllerSpec extends SpecBase {
  "getNiNumberAndDisplay" should {
    "return Ok with correct name value" in new Setup {
      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      when(mockCustomsFinancialsApiConnector.getNiNumber(any)(any))
        .thenReturn(Future.successful(Right(PersonDetails(name, niNumber))))

      implicit val msgs: Messages = messages(app)
      implicit val config: AppConfig = appConfig(app)

      running(app) {
        implicit val request = fakeRequest(GET, routes.JamieDetailsPageController.getNiNumberAndDisplay(name, age).url)
        val result: Future[Result] = route(app, request).value

        status(result) mustEqual OK

        val detailsPage = app.injector.instanceOf[jamie_details_page]
        contentAsString(result) mustEqual detailsPage(name, age, niNumberOpt).body
      }
    }

    "return OK and do not display NI number when API call fails due to Internal_Server_Error" in new Setup {
      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      when(mockCustomsFinancialsApiConnector.getNiNumber(any)(any))
        .thenReturn(Future.successful(Left(InternalServerErrorErrorResponse)))

      implicit val msgs: Messages = messages(app)
      implicit val config: AppConfig = appConfig(app)

      running(app) {
        implicit val request = fakeRequest(GET, routes.JamieDetailsPageController.getNiNumberAndDisplay(name, age).url)
        val result: Future[Result] = route(app, request).value

        status(result) mustEqual OK

        val detailsPage = app.injector.instanceOf[jamie_details_page]
        contentAsString(result) mustEqual detailsPage(name, age, None).body
      }
    }

    "return OK and do not display NI number when API call fails due to BAD_REQUEST" in new Setup {
      val app: Application = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      when(mockCustomsFinancialsApiConnector.getNiNumber(any)(any))
        .thenReturn(Future.successful(Left(BadRequest)))

      implicit val msgs: Messages = messages(app)
      implicit val config: AppConfig = appConfig(app)

      running(app) {
        implicit val request = fakeRequest(GET, routes.JamieDetailsPageController.getNiNumberAndDisplay(name, age).url)

        val result: Future[Result] = route(app, request).value
        status(result) mustEqual OK

        val detailsPage = app.injector.instanceOf[jamie_details_page]
        contentAsString(result) mustEqual detailsPage(name, age, None).body
      }
    }
  }


  trait Setup {
    val name = "Jamie"
    val age = 30
    val ageInt = 30
    val ageString = "Thirty"
    val niNumberOpt: Option[String] = Some("AB123456C")
    val niNumber = "AB123456C"

    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
  }
}