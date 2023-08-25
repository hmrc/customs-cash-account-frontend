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

package config

import play.api.Application
import utils.SpecBase

class AppConfigSpec extends SpecBase {

  "AppConfig" should {
    "retrieve correct values for the provided configuration" in new Setup {
      appConfig.appName mustBe "customs-cash-account-frontend"
      appConfig.subscribeCdsUrl mustBe
        "https://www.tax.service.gov.uk/customs-enrolment-services/cds/subscribe"
    }
  }

  trait Setup {
    val app: Application = application.build()
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
