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

import models.domain.CAN
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import 

class CashAccountViewModel1Spec extends SpecBase {

  "CashAccountViewModel1" should {

    "return correct contents" in new Setup {

    }
  }

  private def shouldDisplayCorrectTitle() = ???
  private def shouldDisplayCorrectBackLink() = ???
  private def shouldDisplayCorrectRequestTransactionsHeading() = ???
  private def shouldDisplayCorrectDownloadCSVFileLinkUrl() = ???
  private def shouldDisplayCorrectDownloadCSVFileGuidanceText() = ???
  private def shouldProduceCorrectCashAccountDashboardViewModel() = ???

  trait Setup {

    val eoriNumber = "test_eori"
    val can = "12345678"

    val app: Application = application.build()
    implicit val msgs: Messages = messages(app)

    val cashAccount: CashAccount = CashAccount(number = can,
      owner = eoriNumber,
      status: CDSAccountStatus,
      balances: CDSCashBalance)

  }

}
