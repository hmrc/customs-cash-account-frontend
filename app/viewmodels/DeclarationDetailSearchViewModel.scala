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
import models._
import models.response.DeclarationSearch
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, SummaryList, SummaryListRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value}
import utils.Utils.{dlComponent, emptyH1InnerComponent, emptyH2InnerComponent, emptyString}

import java.time.LocalDate

case class DeclarationDetailSearchViewModel(
  header: Html,
  subHeader: Html,
  declarationSummaryList: SummaryList,
  taxSummaryList: SummaryList
)

object DeclarationDetailSearchViewModel {

  def apply(searchInput: String, account: CashAccount, declaration: DeclarationSearch)(implicit
    messages: Messages
  ): DeclarationDetailSearchViewModel = {

    val headerHtml    = header(searchInput)
    val subHeaderHtml = subHeader(account)

    val declarationList = declarationSummaryList(declaration)
    val taxList         = taxSummaryList(declaration)

    DeclarationDetailSearchViewModel(
      header = headerHtml,
      subHeader = subHeaderHtml,
      declarationSummaryList = declarationList,
      taxSummaryList = taxList
    )
  }

  private def header(searchInput: String)(implicit messages: Messages): Html =
    if (searchInput.nonEmpty) {
      emptyH1InnerComponent(msg = "cf.cash-account.detail.declaration.search-title", innerMsg = searchInput)
    } else {
      Html(None)
    }

  private def subHeader(account: CashAccount)(implicit messages: Messages): Html =
    dlComponent(
      dtMsg = messages("cf.cash-account.detail.account", account.number),
      ddMsg = account.number,
      id = Some("account-number"),
      classes = "govuk-caption-xl"
    )

  private def toBigDecimal(amount: Any): BigDecimal = amount match {
    case bd: BigDecimal => bd
    case d: Double      => BigDecimal(d)
    case s: String      => BigDecimal(s)
    case _              => BigDecimal(0)
  }

  private def declarationSummaryList(declaration: DeclarationSearch)(implicit messages: Messages): SummaryList = {
    val declarationDateParsed: LocalDate = LocalDate.parse(declaration.postingDate, Formatters.yyyyMMddDateFormatter)

    SummaryList(
      attributes = Map("id" -> "mrn"),
      rows = Seq(
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.date"))),
          value = Value(content = HtmlContent(Formatters.dateAsDayMonthAndYear(declarationDateParsed)))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.movementReferenceNumber"))),
          value = Value(content = HtmlContent(declaration.declarationID))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.uniqueConsignmentReference"))),
          value = Value(content = HtmlContent(declaration.declarantRef.getOrElse(emptyString)))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.declarantEori"))),
          value = Value(content = HtmlContent(declaration.declarantEORINumber))
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.importerEori"))),
          value = Value(content = HtmlContent(declaration.importersEORINumber))
        )
      )
    )
  }

  private def taxSummaryList(declaration: DeclarationSearch)(implicit messages: Messages): SummaryList =
    SummaryList(
      attributes = Map("id" -> "tax-details"),
      rows = Seq(
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.duty"))),
          value = Value(content =
            HtmlContent(
              Formatters.formatCurrencyAmount(
                declaration.taxGroups
                  .find(_.taxGroup.taxGroupDescription == CustomsDuty.onWire)
                  .map(taxGroup => toBigDecimal(taxGroup.taxGroup.amount))
                  .getOrElse(BigDecimal(0))
              )
            )
          )
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.vat"))),
          value = Value(content =
            HtmlContent(
              Formatters.formatCurrencyAmount(
                declaration.taxGroups
                  .find(_.taxGroup.taxGroupDescription == ImportVat.onWire)
                  .map(taxGroup => toBigDecimal(taxGroup.taxGroup.amount))
                  .getOrElse(BigDecimal(0))
              )
            )
          )
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.csv.excise"))),
          value = Value(content =
            HtmlContent(
              declaration.taxGroups
                .find(_.taxGroup.taxGroupDescription == ExciseDuty.onWire)
                .map(_.taxGroup.amount)
                .fold(emptyString)(amount => Formatters.formatCurrencyAmount(amount))
            )
          )
        ),
        SummaryListRow(
          key = Key(content = HtmlContent(messages("cf.cash-account.detail.total.paid"))),
          value = Value(content =
            HtmlContent(
              Formatters.formatCurrencyAmount(declaration.amount)
            )
          )
        )
      )
    )
}
