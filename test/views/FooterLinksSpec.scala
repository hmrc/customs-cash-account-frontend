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
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase

class FooterLinksSpec extends SpecBase {

  "apply" should {
    "return correct list of FooterItems" when {
      "matching message key is present for FooterItems" in new Setup {

        FooterLinks()(msgs, config).size mustBe 4
      }
    }
  }


  trait Setup {
    val app: Application = application.build()

    implicit val msgs: Messages = messages(app)
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
