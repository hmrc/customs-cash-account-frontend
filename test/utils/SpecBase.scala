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

package utils

import com.codahale.metrics.MetricRegistry
import config.AppConfig
import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import controllers.actions.{FakeIdentifierAction, IdentifierAction}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

class FakeMetrics extends Metrics {
  override val defaultRegistry: MetricRegistry = new MetricRegistry
}

trait SpecBase
    extends AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val emptyString          = ""
  val sessionId: SessionId = SessionId("session_1234")

  lazy val applicationBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .overrides(
      bind[IdentifierAction].to[FakeIdentifierAction],
      bind[Metrics].toInstance(new FakeMetrics)
    )
    .configure("play.filters.csp.nonce.enabled" -> "false", "auditing.enabled" -> "false")

  lazy val application: Application = applicationBuilder.build()

  def fakeRequest(method: String = emptyString, path: String = emptyString): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  implicit lazy val messages: Messages =
    application.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

  implicit lazy val appConfig: AppConfig = applicationBuilder.injector().instanceOf[AppConfig]

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))
}
