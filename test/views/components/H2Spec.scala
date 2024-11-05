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

package views.components

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import utils.SpecBase
import views.html.components.h2

class H2Spec extends SpecBase {

  "component" should {

   "display correct contents" when {
     "h2 component has extra content" in new Setup {
       h2Component.getElementById(id).text() mustBe s"$msg $extraContent"
       h2Component.getElementsByClass(classes).size() mustBe 1
     }

     "h2 component can exist without extra content" in new Setup {
       h2ComponentWithoutContent.getElementById(idNoContent).text() mustBe msg
       h2ComponentWithoutContent.getElementsByClass(classes).size() mustBe 1
     }
   }
  }

  trait Setup {
    val app: Application = buildApp

    val msg: String = "some message"
    val id: String = "test_id"
    val idNoContent: String = "test_id_noContent"
    val classes: String = "govuk-heading-m"
    val extraContent: String = "some extra content"

    val h2Component: Document =
      Jsoup.parse(app.injector.instanceOf[h2].apply(msg, Some(id), classes, Some(extraContent)).body)

    val h2ComponentWithoutContent: Document =
      Jsoup.parse(app.injector.instanceOf[h2].apply(msg, Some(idNoContent), classes).body)
  }
}
