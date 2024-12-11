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

package views

import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.twirl.api.Html
import views.html.Head

class HeadSpec extends ViewTestHelper {
  "view" should {

    "display correct contents" when {

      "headBlock contains Html" in new Setup {
        val sampleString              = "test_str"
        val view: Document            = viewDoc(Some(Html(sampleString)))
        implicit val htmlView: String = view.html()

        view.getElementById("tracking-consent-script-tag").text() mustBe empty

        htmlView.contains(sampleString) mustBe true

        shouldContainTimeoutDialog
      }

      "headBlock is empty" in new Setup {
        val view: Document            = viewDoc()
        implicit val htmlView: String = view.html()

        view.getElementById("tracking-consent-script-tag").text() mustBe empty

        shouldContainTimeoutDialog
      }
    }
  }

  def shouldContainTimeoutDialog(implicit htmlView: String): Assertion = {

    htmlView.contains(messages("timeout.title")) mustBe true

    htmlView.contains(s"data-timeout=\"${appConfig.timeout}\"") mustBe true

    htmlView.contains(appConfig.countdown.toString) mustBe true

    htmlView.contains(request.uri) mustBe true

    htmlView.contains(routes.LogoutController.logout.url) mustBe true

    htmlView.contains(routes.LogoutController.logoutNoSurvey.url) mustBe true
  }

  trait Setup {
    def viewDoc(headBlock: Option[Html] = None): Document =
      Jsoup.parse(app.injector.instanceOf[Head].apply(headBlock).body)
  }
}
