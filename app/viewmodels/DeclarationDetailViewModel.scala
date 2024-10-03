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
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, SummaryList, SummaryListRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value}
import utils.Utils.{emptyH1InnerComponent, emptyH2InnerComponent, emptyString}

case class DeclarationDetailViewModel(eori: String,
                                      account: CashAccount,
                                      cameViaSearch: Boolean,
                                      searchInput: String,
                                      header: Html,
                                      subHeader: Html,
                                      declarationSummaryList: SummaryList,
                                      taxSummaryList: SummaryList)

object DeclarationDetailViewModel {

  def apply(eori: String,
            account: CashAccount,
            cameViaSearch: Boolean,
            searchInput: String,
            declaration: Declaration)(implicit messages: Messages): DeclarationDetailViewModel = {

    val headerHtml = header(DeclarationDetailViewModel(
      eori,
      account,
      cameViaSearch,
      searchInput,
      Html(emptyString),
      Html(emptyString),
      SummaryList(Seq.empty),
      SummaryList(Seq.empty)))

    val subHeaderHtml = subHeader(DeclarationDetailViewModel(
      eori,
      account,
      cameViaSearch,
      searchInput,
      Html(emptyString),
      Html(emptyString),
      SummaryList(Seq.empty),
      SummaryList(Seq.empty)))

    val declarationList = declarationSummaryList(declaration)
    val taxList = taxSummaryList(declaration)

    DeclarationDetailViewModel(
      eori = eori,
      account = account,
      cameViaSearch = cameViaSearch,
      searchInput = searchInput,
      header = headerHtml,
      subHeader = subHeaderHtml,
      declarationSummaryList = declarationList,
      taxSummaryList = taxList)
  }

  def header(viewModel: DeclarationDetailViewModel)(implicit messages: Messages): Html = {
    if (viewModel.cameViaSearch && viewModel.searchInput.nonEmpty) {
      emptyH1InnerComponent(msg = "cf.cash-account.detail.declaration.search-title", innerMsg = viewModel.searchInput)
    } else {
      emptyH1InnerComponent(msg = "cf.cash-account.detail.declaration.title")
    }
  }

  def subHeader(viewModel: DeclarationDetailViewModel)(implicit messages: Messages): Html = {
    emptyH2InnerComponent(
      msg = "cf.cash-account.detail.account",
      innerMsg = viewModel.account.number,
      id = Some("account-number"),
      classes = "govuk-caption-xl"
    )
  }

  def declarationSummaryList(declaration: Declaration)(implicit messages: Messages): SummaryList = {
    SummaryList(
      attributes = Map("id" -> "mrn"),
      rows = Seq(
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.date"))),
          value = Value(content = HtmlContent(Formatters.dateAsDayMonthAndYear(declaration.date)))
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.movementReferenceNumber"))),
          value = Value(content = HtmlContent(declaration.movementReferenceNumber))
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.uniqueConsignmentReference"))),
          value = Value(content = HtmlContent(declaration.declarantReference.getOrElse(emptyString)))
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.declarantEori"))),
          value = Value(content = HtmlContent(declaration.declarantEori))
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.importerEori"))),
          value = Value(content = HtmlContent(declaration.importerEori.getOrElse(emptyString)))
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
            Formatters.formatCurrencyAmount(declaration.taxGroups.find(_.taxGroupDescription == CustomsDuty)
              .map(_.amount)
              .getOrElse(BigDecimal(0)))
          ))
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.vat"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.taxGroups.find(_.taxGroupDescription == ImportVat)
              .map(_.amount)
              .getOrElse(BigDecimal(0)))
          ))
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.csv.excise"))),
          value = Value(content = HtmlContent(
            declaration.taxGroups.find(_.taxGroupDescription == ExciseDuty)
              .map(_.amount)
              .fold(emptyString)(amount => Formatters.formatCurrencyAmount(amount))
          ))
        ),
        SummaryListRow(
          key = Key(content = Text(messages("cf.cash-account.detail.total.paid"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.amount)
          ))
        )
      )
    )
  }
}
