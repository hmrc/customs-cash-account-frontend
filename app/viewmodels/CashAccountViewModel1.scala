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

import models.CashAccount
import models.domain.EORI
import utils.Utils.emptyString
import models.CashTransactions

case class CashAccountViewModel1(pageTitle: String,
                                 backLink: String,
                                 requestTransactionsHeading: String,
                                 downloadCSVFileLinkUrl: String,
                                 downloadCSVFileGuidanceText: String,
                                 dashboard: CashAccountDashboardViewModel)

object CashAccountViewModel1 {

  def apply(eori: EORI,
            account: CashAccount,
            cashTrans: CashTransactions): CashAccountViewModel1 = {

    val cashAccountDashboardViewModel = CashAccountDashboardViewModel()

    CashAccountViewModel1(
      pageTitle = emptyString,
      backLink = emptyString,
      requestTransactionsHeading = emptyString,
      downloadCSVFileLinkUrl = emptyString,
      downloadCSVFileGuidanceText = emptyString,
      cashAccountDashboardViewModel)
  }
}
