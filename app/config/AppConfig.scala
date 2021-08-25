/*
 * Copyright 2021 HM Revenue & Customs
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

  lazy val appName: String = config.get[String]("appName")
  lazy val loginUrl: String = config.get[String]("urls.login")
  lazy val loginContinueUrl: String = config.get[String]("urls.loginContinue")
  lazy val signOutUrl: String = config.get[String]("urls.signOut")

  lazy val cashAccountTopUpGuidanceUrl = config.get[String]("urls.cashAccountTopUpGuidanceUrl")
  lazy val cashAccountWithdrawFundsGuidanceUrl = config.get[String]("urls.cashAccountWithdrawFundsGuidanceUrl")
  lazy val customsFinancialsFrontendHomepage = config.get[String]("urls.customsFinancialsHomepage")
  lazy val govUkHomepage = config.get[String]("urls.govUkHome")
  lazy val cashAccountForCdsDeclarationsUrl = config.get[String]("urls.cashAccountForCdsDeclarationsUrl")
  lazy val feedbackService = config.getOptional[String]("feedback.url").getOrElse("/feedback") +
    config.getOptional[String]("feedback.source").getOrElse("/CDS-FIN")

  lazy val timeout: Int = config.get[Int]("timeout.timeout")
  lazy val countdown: Int = config.get[Int]("timeout.countdown")

  lazy val registerCdsUrl: String = config.get[String]("urls.cdsRegisterUrl")
  lazy val subscribeCdsUrl: String = config.get[String]("urls.cdsSubscribeUrl")
  lazy val applicationStatusCdsUrl: String = config.get[String]("urls.applicationStatusUrl")

  lazy val numberOfMonthsOfCashTransactionsToShow: Int = config.get[Int]("application.cash-account.numberOfMonthsOfTransactionsToShow")
  lazy val numberOfDaysToShow: Int = config.get[Int]("application.cash-account.numberOfDaysToShow")
  lazy val fixedTimeTesting: Boolean = config.get[Boolean]("features.fixed-systemdate-for-tests")

  lazy val cashAccountInterval: Int = config.get[Int]("application.cash-account.updateTime.intervalMilliseconds")
  lazy val cashAccountTimeout: Int = config.get[Int]("application.cash-account.updateTime.timeoutMilliseconds")
}
