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

import helpers.Formatters
import models.{CashAccount, CustomsDuty, Declaration, ExciseDuty, ImportVat}
import models.domain.EORI
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, SummaryList, SummaryListRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value}
import utils.Utils.emptyString

case class DeclarationDetailViewModel(eori: EORI, account: CashAccount)

object DeclarationDetailViewModel {

  def declarationSummaryList(declaration: Declaration)(implicit messages: Messages): SummaryList = {
    SummaryList(
      attributes = Map("id" -> "mrn"),
      rows = Seq(
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.date"))),
          value = Value(content = HtmlContent(Formatters.dateAsDayMonthAndYear(declaration.date))),
          actions = None
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.movementReferenceNumber"))),
          value = Value(content = HtmlContent(declaration.movementReferenceNumber)),
          actions = None
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.uniqueConsignmentReference"))),
          value = Value(content = HtmlContent(declaration.declarantReference.getOrElse(emptyString))),
          actions = None
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.declarantEori"))),
          value = Value(content = HtmlContent(declaration.declarantEori)),
          actions = None
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.importerEori"))),
          value = Value(content = HtmlContent(declaration.importerEori.getOrElse(emptyString))),
          actions = None
        )
      )
    )
  }

  def taxSummaryList(declaration: Declaration)(implicit messages: Messages): SummaryList = {
    SummaryList(
      attributes = Map("id" -> "tax-details"),
      rows = Seq(
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.duty"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.taxGroups.find(_.taxTypeGroup == CustomsDuty)
              .map(_.amount)
              .getOrElse(BigDecimal(0)))
          )),
          actions = None
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.vat"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.taxGroups.find(_.taxTypeGroup == ImportVat)
              .map(_.amount)
              .getOrElse(BigDecimal(0)))
          )),
          actions = None
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.excise"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.taxGroups.find(_.taxTypeGroup == ExciseDuty)
              .map(_.amount)
              .getOrElse(BigDecimal(0)))
          )),
          actions = None
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.detail.total.paid"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.amount)
          )),
          actions = None
        )
      )
    )
  }
}
