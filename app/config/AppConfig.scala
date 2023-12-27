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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  lazy val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  lazy val customsFinancialsApi: String = servicesConfig.baseUrl("customs-financials-api") +
    config.getOptional[String]("customs-financials-api.context").getOrElse("/customs-financials-api")

  lazy val customsDataStore: String = s"${servicesConfig.baseUrl("customs-data-store")}${
    config.getOptional[String](
      "microservice.services.customs-data-store.context").getOrElse("/customs-data-store")
  }"

  lazy val emailFrontendUrl: String = s"${servicesConfig.baseUrl("customs-email-frontend")}${
    config.getOptional[String]("customs-email-frontend.context").getOrElse("/manage-email-cds")
  }${
    config.get[String]("microservice.services.customs-email-frontend.url")
  }"

  lazy val appName: String = config.get[String]("appName")
  lazy val loginUrl: String = config.get[String]("urls.login")
  lazy val loginContinueUrl: String = config.get[String]("urls.loginContinue")
  lazy val signOutUrl: String = config.get[String]("urls.signOut")
  lazy val cashAccountTopUpGuidanceUrl: String = config.get[String]("urls.cashAccountTopUpGuidanceUrl")
  lazy val customsFinancialsFrontendHomepage: String = config.get[String]("urls.customsFinancialsHomepage")
  lazy val govUkHomepage: String = config.get[String]("urls.govUkHome")
  lazy val cashAccountForCdsDeclarationsUrl: String = config.get[String]("urls.cashAccountForCdsDeclarationsUrl")
  lazy val feedbackService: String = config.getOptional[String]("feedback.url").getOrElse("/feedback") +
    config.getOptional[String]("feedback.source").getOrElse("/CDS-FIN")

  lazy val timeout: Int = config.get[Int]("timeout.timeout")
  lazy val countdown: Int = config.get[Int]("timeout.countdown")

  lazy val registerCdsUrl: String = config.get[String]("urls.cdsRegisterUrl")
  lazy val subscribeCdsUrl: String = config.get[String]("urls.cdsSubscribeUrl")

  lazy val numberOfMonthsOfCashTransactionsToShow: Int = config.get[Int]("application.cash-account.numberOfMonthsOfTransactionsToShow")
  lazy val numberOfDaysToShow: Int = config.get[Int]("application.cash-account.numberOfDaysToShow")
  lazy val fixedTimeTesting: Boolean = config.get[Boolean]("features.fixed-systemdate-for-tests")
  lazy val transactionsTimeoutFlag: Boolean = config.get[Boolean]("features.transactions-timeout")

  lazy val cashAccountInterval: Int = config.get[Int]("application.cash-account.updateTime.intervalMilliseconds")
  lazy val cashAccountTimeout: Int = config.get[Int]("application.cash-account.updateTime.timeoutMilliseconds")

  lazy val helpMakeGovUkBetterUrl: String = config.get[String]("urls.helpMakeGovUkBetterUrl")
}
