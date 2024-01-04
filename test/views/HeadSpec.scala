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

import config.AppConfig
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.SpecBase
import views.html.Head

class HeadSpec extends SpecBase {
  "view" should {

    "display correct contents" when {

      "headBlock contains Html" in new Setup {
        val sampleString = "test_str"
        val view: Document = viewDoc(Some(Html(sampleString)))
        val htmlView: String = view.html()

        view.getElementById("tracking-consent-script-tag").text() mustBe empty

        htmlView.contains(sampleString) mustBe true

        performTimeoutDialogContentsCheck(htmlView, msgs, config, request)
      }

      "headBlock is empty" in new Setup {
        val view: Document = viewDoc()
        val htmlView: String = view.html()

        view.getElementById("tracking-consent-script-tag").text() mustBe empty

        performTimeoutDialogContentsCheck(htmlView, msgs, config, request)
      }
    }
  }

  def performTimeoutDialogContentsCheck(htmlView: String,
                                        msgs: Messages,
                                        config: AppConfig,
                                        request: FakeRequest[AnyContentAsEmpty.type]): Assertion = {

    htmlView.contains(msgs("timeout.title")) mustBe true
    htmlView.contains(s"data-timeout=\"${config.timeout}\"") mustBe true
    htmlView.contains(config.countdown) mustBe true
    htmlView.contains(request.uri) mustBe true
    htmlView.contains(routes.LogoutController.logout.url) mustBe true
    htmlView.contains(routes.LogoutController.logoutNoSurvey.url) mustBe true
  }

  trait Setup {
    val app: Application = application.build()

    implicit val msgs: Messages = messages(app)
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "test_path")

    def viewDoc(headBlock: Option[Html] = None): Document =
      Jsoup.parse(app.injector.instanceOf[Head].apply(headBlock).body)
  }
}
