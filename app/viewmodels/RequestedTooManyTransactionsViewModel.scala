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
import utils.Utils.*
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.{h1, p1}
import play.api.i18n.Messages

import java.time.LocalDate

case class RequestedTooManyTransactionsViewModel(pageTitle: String,
                                                 backLink: String,
                                                 heading: HtmlFormat.Appendable,
                                                 tryAgainLink: HtmlFormat.Appendable,
                                                 statement01: HtmlFormat.Appendable,
                                                 helpAndSupportGuidance: GuidanceRow)

object RequestedTooManyTransactionsViewModel {

  def apply(from: LocalDate,
            to: LocalDate,
            tryAgainUrl: String,
            backUrl: String)(implicit msgs: Messages, config: AppConfig): RequestedTooManyTransactionsViewModel = {

    RequestedTooManyTransactionsViewModel(
      pageTitle = msgs("cf.cash-account.detail.title"),
      backLink = backUrl,
      heading = formHeading(),
      statement01 = formStatement(from, to),
      tryAgainLink = formLink(tryAgainUrl),
      helpAndSupportGuidance = helpAndSupport)
  }

  private def formHeading()(implicit msgs: Messages): HtmlFormat.Appendable = {
    new h1().apply(msg = "cf.cash-account.transactions.requested.tooMany.transactions")
  }

  private def formStatement(from: LocalDate, to: LocalDate)(implicit msgs: Messages): HtmlFormat.Appendable = {
    new p1().apply(
      content = Html(s"${
        msgs("cf.cash-account.transactions.requested.statement.msg",
          dateAsMonthAndYear(from), dateAsMonthAndYear(to))
      }"),
      classes = Some("govuk-body")
    )
  }

  private def dateAsMonthAndYear(date: LocalDate)(implicit messages: Messages): String = {
    s"${messages(s"month.${date.getMonthValue}")} ${date.getYear}"
  }

  private def formLink(tryAgainUrl: String)(implicit msgs: Messages): HtmlFormat.Appendable = {
    linkComponent(
      LinkComponentValues(
        location = tryAgainUrl,
        linkMessageKey = "cf.cash-account.transactions.requested.tryAgain",
        pClass = "govuk-body govuk-!-margin-bottom-9"))
  }

  private def helpAndSupport(implicit appConfig: AppConfig, messages: Messages): GuidanceRow = {
    GuidanceRow(
      h2Heading = h2Component(
        id = Some("search-transactions-support-message-heading"),
        msgKey = "site.support.heading"
      ),

      link = Some(hmrcNewTabLinkComponent(linkMessage = "cf.cash-account.help-and-support.link.text",
        href = appConfig.cashAccountForCdsDeclarationsUrl,
        preLinkMessage = Some("cf.cash-account.help-and-support.link.text.pre.v2"),
        postLinkMessage = Some("cf.cash-account.help-and-support.link.text.post")))
    )
  }
}
