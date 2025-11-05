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

import models.FileRole.CDSCashAccount
import utils.SpecBase

class AppConfigSpec extends SpecBase {

  "AppConfig" should {

    "contain correct values for the provided configuration" in {
      appConfig.appName mustBe "customs-cash-account-frontend"
      appConfig.loginUrl mustBe "http://localhost:9553/bas-gateway/sign-in"
      appConfig.loginContinueUrl mustBe "http://localhost:9394/customs/cash-account"
      appConfig.signOutUrl mustBe "http://localhost:9553/bas-gateway/sign-out-without-state"

      appConfig.cashAccountTopUpGuidanceUrl mustBe
        "https://www.gov.uk/guidance/paying-into-your-cash-account-for-cds-declarations"

      appConfig.customsFinancialsFrontendHomepage mustBe "http://localhost:9876/customs/payment-records"

      appConfig.cashAccountForCdsDeclarationsUrl mustBe
        "https://www.gov.uk/guidance/use-a-cash-account-for-cds-declarations"

      appConfig.feedbackService mustBe "http://localhost:9514/feedback/CDS-FIN"
      appConfig.timeout mustBe 900
      appConfig.countdown mustBe 120
      appConfig.numberOfMonthsOfCashTransactionsToShow mustBe 6
      appConfig.numberOfDaysToShow mustBe 5
      appConfig.fixedTimeTesting mustBe false
      appConfig.transactionsTimeoutFlag mustBe true

      appConfig.helpMakeGovUkBetterUrl mustBe
        "https://survey.take-part-in-research.service.gov.uk/jfe/form/SV_74GjifgnGv6GsMC?Source=BannerList_HMRC_CDS_MIDVA"

      appConfig.subscribeCdsUrl mustBe
        "https://www.tax.service.gov.uk/customs-enrolment-services/cds/subscribe"

      appConfig.customsDataStore mustBe "http://localhost:9893/customs-data-store"
      appConfig.customsDataStoreGetVerifiedEmail mustBe "http://localhost:9893/customs-data-store/eori/verified-email"

      appConfig.emailFrontendUrl mustBe "http://localhost:9898/manage-email-cds/service/customs-finance"

      appConfig.requestedStatements(CDSCashAccount) mustBe
        "http://localhost:9396/customs/historic-statement/requested/cash-statement"

      appConfig.sdesApiEndPoint must include("/customs-financials-sdes-stub/files-available/list/CDSCashAccount")
    }
  }

  "isCashAccountV2FeatureFlagEnabled" should {
    "return the correct value" in {
      assume(!appConfig.isCashAccountV2FeatureFlagEnabled)

      appConfig.isCashAccountV2FeatureFlagEnabled mustBe false
    }
  }

  "numberOfRecordsPerPage" should {
    "return the correct value" in {
      appConfig.numberOfRecordsPerPage mustBe 30
    }
  }

  "numberOfRecordsToDisableNavigationButtonsInPagination" should {
    "return the correct value" in {
      appConfig.numberOfRecordsToDisableNavigationButtonsInPagination mustBe 450
    }
  }

}
