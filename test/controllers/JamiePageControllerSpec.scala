package controllers

import forms.JamieFormProvider
import models.JamieFormFields
import play.api.Application
import play.api.test.Helpers.{POST, running}
import utils.SpecBase
import play.api.mvc.Result
import play.api.test.Helpers.*
import views.html.jamie_input_page
import play.api.data.Form

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
      }
    }
  }

  trait Setup {
    val name = "test name"
    val ageString = "28"
    val ageInt = 28
  }
}
