package controllers

import config.AppConfig
import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import play.api.Application
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, running}
import utils.SpecBase
import play.api.inject.bind
import play.api.test.Helpers.*




class JamiePageControllerSpec extends SpecBase {
  
  "displayEnteredDetails" must {
    "return ok" in new Setup {
      val app: Application = application
        .overrides(bind[JamiePageController]
          .toInstance(mockJamieFormController)).build()
      
      running(app) {
        val request = fakeRequest(POST, routes.JamiePageController.onPageLoad().url)
        val result = route(app, request).value
        
        status(result) mustEqual OK
      }
    }
  }
  
  "onSubmit" must {
    "return SEE_OTHER when form submission is successful" in new Setup {
      val app: Application = application
        .overrides(bind[JamiePageController].toInstance(mockJamieFormController))
        .build()

      running(app) {
        val request = fakeRequest(POST, routes.JamiePageController.onSubmit()
          .url).withFormUrlEncodedBody("value" -> "name")
      }
    }
    "return same page when form submission is unsuccessful with error validation present" in new Setup {
      
    }
  }

  trait Setup {
    val name = "test name"
    val age = "28"
    val mockJamieFormController: JamiePageController = mock[JamiePageController]
    val page: Option[Int] = Some(1)
  }
}
