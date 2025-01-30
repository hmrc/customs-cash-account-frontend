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

import models.FileRole

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.Utils.emptyString

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {
  lazy val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  lazy val sdesApiEndPoint: String =
    s"${servicesConfig.baseUrl("sdes")}${config.get[String]("microservice.services.sdes.endpoint")}"

  lazy val xClientIdHeader: String = config.get[String]("microservice.services.sdes.x-client-id")

  lazy val customsFinancialsApi: String = servicesConfig.baseUrl("customs-financials-api") +
    config.getOptional[String]("customs-financials-api.context").getOrElse("/customs-financials-api")

  private lazy val customsDataStoreBaseUrl       = servicesConfig.baseUrl("customs-data-store")
  private lazy val customsDataStoreContextUrl    =
    config.getOptional[String]("microservice.services.customs-data-store.context").getOrElse("/customs-data-store")
  private lazy val customsDataStoreVerifiedEmail = "/eori/verified-email"

  lazy val customsDataStore: String                 = s"$customsDataStoreBaseUrl$customsDataStoreContextUrl"
  lazy val customsDataStoreGetVerifiedEmail: String = s"$customsDataStore$customsDataStoreVerifiedEmail"

  private lazy val emailFrontendBaseUrl    = servicesConfig.baseUrl("customs-email-frontend")
  private lazy val emailFrontendContextUrl =
    config.getOptional[String]("customs-email-frontend.context").getOrElse("/manage-email-cds")
  private lazy val emailFrontendFinalUrl   = config.get[String]("microservice.services.customs-email-frontend.url")

  lazy val emailFrontendUrl: String =
    s"$emailFrontendBaseUrl$emailFrontendContextUrl$emailFrontendFinalUrl"

  lazy val appName: String                           = config.get[String]("appName")
  lazy val loginUrl: String                          = config.get[String]("urls.login")
  lazy val loginContinueUrl: String                  = config.get[String]("urls.loginContinue")
  lazy val signOutUrl: String                        = config.get[String]("urls.signOut")
  lazy val cashAccountTopUpGuidanceUrl: String       = config.get[String]("urls.cashAccountTopUpGuidanceUrl")
  lazy val customsFinancialsFrontendHomepage: String = config.get[String]("urls.customsFinancialsHomepage")
  lazy val cashAccountForCdsDeclarationsUrl: String  = config.get[String]("urls.cashAccountForCdsDeclarationsUrl")
  lazy val feedbackService: String                   = config.getOptional[String]("feedback.url").getOrElse("/feedback") +
    config.getOptional[String]("feedback.source").getOrElse("/CDS-FIN")

  lazy val requestedStatements: String = config.get[String]("urls.requestedStatements")

  lazy val timeout: Int   = config.get[Int]("timeout.timeout")
  lazy val countdown: Int = config.get[Int]("timeout.countdown")

  lazy val subscribeCdsUrl: String = config.get[String]("urls.cdsSubscribeUrl")

  lazy val numberOfMonthsOfCashTransactionsToShow: Int =
    config.get[Int]("application.cash-account.numberOfMonthsOfTransactionsToShow")

  lazy val numberOfDaysToShow: Int                    = config.get[Int]("application.cash-account.numberOfDaysToShow")
  lazy val fixedTimeTesting: Boolean                  = config.get[Boolean]("features.fixed-systemdate-for-tests")
  lazy val transactionsTimeoutFlag: Boolean           = config.get[Boolean]("features.transactions-timeout")
  lazy val isCashAccountV2FeatureFlagEnabled: Boolean = config.get[Boolean]("features.cash-account-v2-enabled")

  lazy val helpMakeGovUkBetterUrl: String = config.get[String]("urls.helpMakeGovUkBetterUrl")

  lazy val numberOfRecordsPerPage: Int = config.get[Int]("pagination.no-of-records-per-page")

  lazy val numberOfRecordsToDisableNavigationButtonsInPagination: Int =
    config.get[Int]("pagination.no-of-records-to-disable-navigation-buttons")

  def requestedStatements(fileRole: FileRole): String =
    fileRole match {
      case FileRole.CDSCashAccount => s"$requestedStatements${fileRole.featureName}"
    }
}
