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

package viewmodels

import config.AppConfig
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import utils.TestData.*

class PaymentSearchResultStatementsViewModelSpec extends SpecBase {

  "apply" should {

    "return viewModel object with correct content" when {

      "cash transactions are available" in new Setup {
        val viewModel01: PaymentSearchResultStatementsViewModel =
          PaymentSearchResultStatementsViewModel(SEQ_OF_PAYMENT_DETAILS_01, None)

        viewModel01.dailyStatements.size mustBe 3
        viewModel01.dailyStatements.map(_.date) mustEqual Seq(DATE_AUG_17, DATE_AUG_16, DATE_AUG_15)
        viewModel01.hasTransactions mustBe true
        viewModel01.noTransactionsMessage mustBe None
      }

      "cash transactions are not present" in new Setup {
        val viewModel02: PaymentSearchResultStatementsViewModel =
          PaymentSearchResultStatementsViewModel(Seq.empty, None)

        viewModel02.dailyStatements mustBe empty
        viewModel02.hasTransactions mustBe false
        viewModel02.noTransactionsMessage.get.body.contains("no-transactions-to-display") mustBe true
      }
    }
  }

  trait Setup {
    val app: Application = application.build()
    implicit val msgs: Messages = messages(app)
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
