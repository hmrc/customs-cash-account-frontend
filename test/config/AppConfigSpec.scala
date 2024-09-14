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

    "contain correct values for the provided configuration" in new Setup {
      appConfig.appName mustBe "customs-cash-account-frontend"
      appConfig.loginUrl mustBe "http://localhost:9553/bas-gateway/sign-in"
      appConfig.loginContinueUrl mustBe "http://localhost:9394/customs/cash-account"
      appConfig.signOutUrl mustBe "http://localhost:9553/bas-gateway/sign-out-without-state"

      appConfig.cashAccountTopUpGuidanceUrl mustBe
        "https://www.gov.uk/guidance/paying-into-your-cash-account-for-cds-declarations"

      appConfig.customsFinancialsFrontendHomepage mustBe "http://localhost:9876/customs/payment-records"

      appConfig.cashAccountForCdsDeclarationsUrl mustBe
        "https://www.gov.uk/guidance/use-a-cash-account-for-cds-declarations"

      appConfig.feedbackService mustBe "https://www.development.tax.service.gov.uk/feedback/CDS-FIN"
      appConfig.timeout mustBe 900
      appConfig.countdown mustBe 120
      appConfig.numberOfMonthsOfCashTransactionsToShow mustBe 6
      appConfig.numberOfDaysToShow mustBe 5
      appConfig.fixedTimeTesting mustBe true
      appConfig.transactionsTimeoutFlag mustBe false

      appConfig.helpMakeGovUkBetterUrl mustBe
        "https://signup.take-part-in-research.service.gov.uk?" +
          "utm_campaign=CDSfinancials&utm_source=Other&utm_medium=other&t=HMRC&id=249"

      appConfig.subscribeCdsUrl mustBe
        "https://www.tax.service.gov.uk/customs-enrolment-services/cds/subscribe"

      appConfig.customsDataStore mustBe "http://localhost:9893/customs-data-store"

      appConfig.emailFrontendUrl mustBe "http://localhost:9898/manage-email-cds/service/customs-finance"
    }
  }

  "isCashAccountV2FeatureFlagEnabled" should {
    "return the correct value" in new Setup {
      appConfig.isCashAccountV2FeatureFlagEnabled mustBe false
    }
  }

  trait Setup {
    val app: Application = application.build()
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
