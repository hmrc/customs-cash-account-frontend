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

package views.email

import utils.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.email.undeliverable_email

class UndeliverableEmailSpec extends SpecBase {

  "view" should {

    "display correct guidance and text" in new Setup {
      view.title() mustBe
        s"${messages("cf.undeliverable.email.title")} - ${messages("service.name")} - GOV.UK"

      view.getElementsByTag("h1").text() mustBe messages("cf.undeliverable.email.heading")

      view.text().contains(messages("cf.undeliverable.email.p1")) mustBe true
      view.html.contains(messages("cf.undeliverable.email.p2", email))

      view.text().contains(messages("cf.undeliverable.email.verify.heading")) mustBe true
      view.text().contains(messages("cf.undeliverable.email.verify.text.p1")) mustBe true
      view.text().contains(messages("cf.undeliverable.email.change.heading")) mustBe true

      view.text().contains(messages("cf.undeliverable.email.change.text.p1")) mustBe true
      view.text().contains(messages("cf.undeliverable.email.change.text.p2")) mustBe true

      view.text().contains(messages("cf.undeliverable.email.link-text")) mustBe true

      view.toString must include(nextPageUrl)
      view.text().contains(email.get) mustBe true
    }

    "not display the email paragraph if there is no email" in new Setup {
      viewWithNoEmail.text().contains(email.get) mustBe false
    }
  }

  trait Setup {
    val nextPageUrl = "test_url"
    val email: Option[String] = Some("test@test.com")

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val view: Document = Jsoup.parse(
      application.injector.instanceOf[undeliverable_email].apply(nextPageUrl, email).body)

    val viewWithNoEmail: Document = Jsoup.parse(
      application.injector.instanceOf[undeliverable_email].apply(nextPageUrl, None).body)
  }
}
