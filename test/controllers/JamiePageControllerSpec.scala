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

import connectors.CustomsFinancialsApiConnector
import forms.JamieFormProvider
import models.{JamieFormFields, PersonDetails}
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import play.api.Application
import play.api.test.Helpers.{POST, running}
import utils.SpecBase
import play.api.mvc.Result
import play.api.test.Helpers.*
import views.html.jamie_input_page
import play.api.data.Form
import play.api.inject.bind
import scala.concurrent.Future


class JamiePageControllerSpec extends SpecBase {

  "onPageLoad" must {
    "return ok" in new Setup {
      val app: Application = application.build()
      running(app) {
        val request = fakeRequest(GET, routes.JamiePageController.onPageLoad().url)
        val result: Future[Result] = route(app, request).value
        status(result) mustEqual OK
      }
    }
  }

    "onSubmit" must {
      "return SEE_OTHER when form submission is successful" in new Setup {
        val app: Application = application.build()
        running(app) {
          val request = fakeRequest(POST, routes.JamiePageController.onSubmit().url)
            .withFormUrlEncodedBody("name" -> name, "age" -> ageString)

          val result: Future[Result] = route(app, request).value
          status(result) mustEqual SEE_OTHER

          val expectedRedirectUrl = routes.JamiePageController.displayInputValues(name, ageInt).url
          redirectLocation(result).value mustEqual expectedRedirectUrl
        }
      }

      "return same page when form submission is unsuccessful with error validation present" in new Setup {
        val app: Application = application.build()

        running(app) {
          val formWithErrors: Form[JamieFormFields] = new JamieFormProvider()
            .apply()
            .bind(Map("name" -> name, "age" -> emptyString))

          val request = fakeRequest(POST, routes.JamiePageController.onSubmit().url)
            .withFormUrlEncodedBody("name" -> name, "age" -> emptyString)

          val result: Future[Result] = route(app, request).value
          status(result) mustEqual BAD_REQUEST

          val content = contentAsString(result)
          content must include("There is a problem")
          content must include("Numeric value expected")
        }
      }

      "connect to API and retrieve NI with correct name input" in new Setup {
        when(mockCustomsFinancialsApiConnector.getNiNumber(eqTo(name)))
          .thenReturn(Future.successful(personDetails))

        val app: Application = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          val request = fakeRequest(GET, routes.JamiePageController.displayInputValues(name, ageInt).url)
          val result: Future[Result] = route(app, request).value

          status(result) mustEqual OK
          contentAsString(result) must include(s"NI Number: $niNumber")
        }
      }
    }

    trait Setup {
      val name = "test name"
      val incorrectName = "J4mie"
      val ageString = "28"
      val ageInt = 28
      val niNumber = "QQ123456B"

      val jamieForm: JamieFormFields = JamieFormFields(name, ageInt)
      val personDetails: PersonDetails = PersonDetails(jamieForm, niNumber)

      val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    }
  }
