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
import html.cash_account_v2
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class CashAccountV2Spec extends SpecBase {

  "view" should {

    "display correct contents" when {

      "there are no transactions" in new Setup {

      }

      "transactions are present in the model" in new Setup {

      }
    }
  }

  trait Setup {
    val app: Application = application.build()
    val msgs: Messages = messages(app)
    val config: AppConfig = app.injector.instanceOf[AppConfig]

    protected def createView(viewModel: CashAccountViewModelV2): Document = {
      Jsoup.parse(app.injector.instanceOf[cash_account_v2].apply(viewModel).body)
    }
  }
}
