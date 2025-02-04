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

import org.scalatest.Assertion
import play.api.i18n.Messages
import utils.SpecBase
import java.time.LocalDate

class RequestedTooManyTransactionsViewModelSpec extends SpecBase {

  "apply method" should {

    "return correct view model object" in new Setup {

      val viewModel: RequestedTooManyTransactionsViewModel =
        RequestedTooManyTransactionsViewModel(fromDate, toDate, selectedTxnUrl, selectedTxnUrl)

      shouldProduceCorrectTitle(viewModel.pageTitle)
      shouldProduceCorrectBackLink(viewModel.backLink, selectedTxnUrl)
      shouldProduceCorrectHeading(viewModel.heading.body)
      shouldProduceCorrectTryAgainLink(viewModel.tryAgainLink.body, selectedTxnUrl)
      shouldProduceCorrectFromDateInInfoSection(viewModel.infoSection.body, fromDate)
      shouldProduceCorrectToDateInInfoSection(viewModel.infoSection.body, toDate)
    }
  }

  private def shouldProduceCorrectTitle(pageTitle: String)(implicit messages: Messages): Assertion =
    pageTitle mustBe messages("cf.cash-account.detail.title")

  private def shouldProduceCorrectBackLink(backLink: String, url: String): Assertion =
    url.contains(backLink) mustBe true

  private def shouldProduceCorrectHeading(heading: String)(implicit messages: Messages): Assertion =
    heading.contains(messages("cf.cash-account.transactions.requested.tooMany.transactions")) mustBe true

  private def shouldProduceCorrectTryAgainLink(tryAgainLink: String, url: String): Assertion =
    tryAgainLink.contains(url) mustBe true

  private def shouldProduceCorrectFromDateInInfoSection(statement: String, fromDate: LocalDate)(implicit
    messages: Messages
  ): Assertion =
    statement.contains(s"${messages(s"month.${fromDate.getMonthValue}")} ${fromDate.getYear}") mustBe true

  private def shouldProduceCorrectToDateInInfoSection(statement: String, toDate: LocalDate)(implicit
    messages: Messages
  ): Assertion =
    statement.contains(s"${messages(s"month.${toDate.getMonthValue}")} ${toDate.getYear}") mustBe true

  trait Setup {
    val fromDate: LocalDate    = LocalDate.parse("2020-07-18")
    val toDate: LocalDate      = LocalDate.parse("2020-07-20")
    val selectedTxnUrl: String = controllers.routes.SelectedTransactionsController.onPageLoad().url
  }
}
