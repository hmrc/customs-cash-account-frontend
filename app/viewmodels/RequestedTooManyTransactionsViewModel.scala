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

import helpers.Formatters.dateAsMonthAndYear
import utils.Utils.{LinkComponentValues, linkComponent}
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.{h1, p1}
import play.api.i18n.Messages

import java.time.LocalDate

case class RequestedTooManyTransactionsViewModel(
  pageTitle: String,
  backLink: String,
  heading: HtmlFormat.Appendable,
  tryAgainLink: HtmlFormat.Appendable,
  infoSection: HtmlFormat.Appendable
)

object RequestedTooManyTransactionsViewModel {

  def apply(from: LocalDate, to: LocalDate, tryAgainUrl: String, backUrl: String)(implicit
    messages: Messages
  ): RequestedTooManyTransactionsViewModel =
    RequestedTooManyTransactionsViewModel(
      pageTitle = messages("cf.cash-account.detail.title"),
      backLink = backUrl,
      heading = populateHeading(),
      infoSection = populateInfoSection(from, to),
      tryAgainLink = populateLink(tryAgainUrl)
    )

  private def populateHeading()(implicit msgs: Messages): HtmlFormat.Appendable =
    new h1().apply(
      id = Some("requested-too-many-transactions-message-heading"),
      msg = "cf.cash-account.transactions.requested.tooMany.transactions"
    )

  private def populateInfoSection(from: LocalDate, to: LocalDate)(implicit msgs: Messages): HtmlFormat.Appendable =
    new p1().apply(
      id = Some("requested-too-many-transactions-message-statement"),
      content = Html(
        s"${msgs("cf.cash-account.transactions.requested.statement.msg", dateAsMonthAndYear(from), dateAsMonthAndYear(to))}"
      ),
      classes = Some("govuk-body")
    )

  private def populateLink(tryAgainUrl: String)(implicit msgs: Messages): HtmlFormat.Appendable =
    linkComponent(
      LinkComponentValues(
        location = tryAgainUrl,
        linkMessageKey = "cf.cash-account.transactions.requested.tryAgain",
        pClass = "govuk-body govuk-!-margin-bottom-9"
      )
    )

}
