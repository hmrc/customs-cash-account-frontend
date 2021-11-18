/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.SpecBase

class LogoutControllerSpec extends SpecBase {

  "logout" must {
    "redirect the user to logout with the continue as the feedback survey url" in {
      val app = application
        .configure("feedback.url" -> "/some-continue", "feedback.source" -> "/CDS-FIN")
        .build()
      running(app) {
        val request = FakeRequest(GET, routes.LogoutController.logout.url)

        val result = route(app, request).value
        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/sign-out-without-state?continue=%2Fsome-continue%2FCDS-FIN"
      }
    }
  }

  "logoutNoSurvey" must {
    "redirect the user to logout with no continue location" in {
      val app = application
        .configure("feedback.url" -> "/some-continue", "feedback.source" -> "/CDS-FIN")
        .build()
      running(app) {
        val request = FakeRequest(GET, routes.LogoutController.logoutNoSurvey.url)

        val result = route(app, request).value
        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/sign-out-without-state"
      }
    }
  }
}
