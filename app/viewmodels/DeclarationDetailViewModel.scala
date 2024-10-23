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
                                      header: Html,
                                      subHeader: Html,
                                      declarationSummaryList: SummaryList,
                                      taxSummaryList: SummaryList)

object DeclarationDetailViewModel {

  def apply(account: CashAccount,
            eori: String,
            declaration: Declaration
           )(implicit messages: Messages): DeclarationDetailViewModel = {

    val headerHtml = header()
    val subHeaderHtml = subHeader(account)

    val declarationList = declarationSummaryList(declaration)
    val taxList = taxSummaryList(declaration)

    DeclarationDetailViewModel(
      eori = eori,
      account = account,
      header = headerHtml,
      subHeader = subHeaderHtml,
      declarationSummaryList = declarationList,
      taxSummaryList = taxList
    )
  }

  def header()(implicit messages: Messages): Html = {
    emptyH1InnerComponent(msg = "cf.cash-account.detail.declaration.title")
  }

  def subHeader(account: CashAccount)(implicit messages: Messages): Html = {
    emptyH2InnerComponent(
      msg = "cf.cash-account.detail.account",
      innerMsg = account.number,
      id = Some("account-number"),
      classes = "govuk-caption-xl"
    )
  }

  def declarationSummaryList(declaration: Declaration)(implicit messages: Messages): SummaryList = {
    SummaryList(
      attributes = Map("id" -> "mrn"),
      rows = Seq(
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.date"))),
          value = Value(content = HtmlContent(Formatters.dateAsDayMonthAndYear(declaration.date)))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(
            s"""
              ${messages("cf.cash-account.csv.declaration")}
              <abbr title="${messages("cf.cash-account.detail.movement-reference-number")}">
                ${messages("cf.cash-account.detail.mrn")}
              </abbr>
            """)
          ),
          value = Value(content = HtmlContent(declaration.movementReferenceNumber))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(
            s"""
              ${messages("cf.cash-account.csv.declaration")}
              <abbr title="${messages("cf.cash-account.detail.unique-consignment-reference")}">
                ${messages("cf.cash-account.detail.ucr")}
              </abbr>
            """)
          ),
          value = Value(content = HtmlContent(declaration.declarantReference.getOrElse(emptyString)))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(
            s"""
              ${messages("cf.cash-account.detail.declarant")}
              <abbr title="${messages("cf.cash-account.detail.eori-definition")}">
                ${messages("cf.cash-account.detail.eori")}
              </abbr>
            """)
          ),
          value = Value(content = HtmlContent(declaration.declarantEori))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.detail.importerEori"))),
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
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.duty"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.taxGroups.find(_.taxGroupDescription == CustomsDuty)
              .map(_.amount)
              .getOrElse(BigDecimal(0)))
          ))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.vat"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.taxGroups.find(_.taxGroupDescription == ImportVat)
              .map(_.amount)
              .getOrElse(BigDecimal(0)))
          ))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.excise"))),
          value = Value(content = HtmlContent(
            declaration.taxGroups.find(_.taxGroupDescription == ExciseDuty)
              .map(_.amount)
              .fold(emptyString)(amount => Formatters.formatCurrencyAmount(amount))
          ))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.detail.total.paid"))),
          value = Value(content = HtmlContent(
            Formatters.formatCurrencyAmount(declaration.amount)
          ))
        )
      )
    )
  }
}
