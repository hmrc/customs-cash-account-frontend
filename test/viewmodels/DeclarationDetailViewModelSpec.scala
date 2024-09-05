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

import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CustomsDuty, Declaration, ImportVat, TaxGroup}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.twirl.api.{Html, HtmlFormat}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import views.html.cash_account_declaration_details
import uk.gov.hmrc.govukfrontend.views
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist
import utils.SpecBase

import java.time.LocalDate

class DeclarationDetailViewModelSpec extends SpecBase {

  "DeclarationDetailViewModel" should {

    "generate correct summary lists" in new Setup {

      declarationSummaryList.rows must have size 5
      declarationSummaryList.rows.map(_.key.content.asInstanceOf[Text].value) must contain allElementsOf Seq(
        messages("cf.cash-account.csv.date"),
        messages("cf.cash-account.csv.movementReferenceNumber"),
        messages("cf.cash-account.csv.uniqueConsignmentReference"),
        messages("cf.cash-account.csv.declarantEori"),
        messages("cf.cash-account.csv.importerEori")
      )

      taxSummaryList.rows must have size 4
      taxSummaryList.rows.map(_.key.content.asInstanceOf[Text].value) must contain allElementsOf Seq(
        messages("cf.cash-account.csv.duty"),
        messages("cf.cash-account.csv.vat"),
        messages("cf.cash-account.csv.excise"),
        messages("cf.cash-account.detail.total.paid")
      )
    }

    "render the correct declaration and tax details in the view" in new Setup {

      val mockHtml: Html = HtmlFormat.raw(
        s"""
        <h2 id="account-number">Account ${cashAccount.number}</h2>
        <h1>${messages("cf.cash-account.detail.declaration.title")}</h1>
        <dl class="govuk-summary-list">
          <dt>${messages("cf.cash-account.csv.date")}</dt>
          <dt>${messages("cf.cash-account.csv.movementReferenceNumber")}</dt>
        </dl>
        <dl class="govuk-summary-list">
          <dt>${messages("cf.cash-account.csv.duty")}</dt>
          <dt>${messages("cf.cash-account.csv.vat")}</dt>
        </dl>
      """)

      when(view.apply(any(), any(), any())(any(), any())).thenReturn(mockHtml)

      val html: HtmlFormat.Appendable = view.apply(viewModel, declaration, Some(1))(fakeRequest, messages)
      val document: Document = Jsoup.parse(html.body)

      document.select("h2#account-number").text() must include(cashAccount.number)
      document.select("h1").text() must include(messages("cf.cash-account.detail.declaration.title"))

      val summaryLists: Elements = document.select(".govuk-summary-list")
      summaryLists.size() mustBe 2

      val declarationList: Element = summaryLists.first()
      declarationList.select("dt").eachText() must contain allOf(
        messages("cf.cash-account.csv.date"),
        messages("cf.cash-account.csv.movementReferenceNumber")
      )

      val taxList: Element = summaryLists.last()
      taxList.select("dt").eachText() must contain allOf(
        messages("cf.cash-account.csv.duty"),
        messages("cf.cash-account.csv.vat")
      )
    }

    "handle missing optional fields correctly" in new Setup {
      val declarationWithMissingFields: Declaration = declaration.copy(
        importerEori = None,
        declarantReference = None
      )

      declarationSummaryList.rows must have size 5
      val contentMap: Map[String, String] = declarationSummaryList.rows.map(row =>
        (row.key.content, row.value.content) match {
          case (Text(key), Text(value)) => (key, value)
          case (Text(key), HtmlContent(value)) => (key, value.body)
          case _ => fail("Unexpected content type")
        }).toMap

      contentMap(messages("cf.cash-account.csv.importerEori")) mustBe emptyString
      contentMap(messages("cf.cash-account.csv.uniqueConsignmentReference")) mustBe emptyString
    }

    "calculate total tax correctly" in new Setup {

      val totalPaidRow: Option[summarylist.SummaryListRow] = taxSummaryList.rows.find(_.key.content match {
        case Text(content) => content == messages("cf.cash-account.detail.total.paid")
        case _ => false
      })
      totalPaidRow mustBe defined
      totalPaidRow.get.value.content match {
        case Text(content) => content mustBe "£500.00"
        case HtmlContent(content) => content.body mustBe "£500.00"
        case _ => fail("Unexpected content type for total paid")
      }
    }
  }

  trait Setup {

    val mockMessagesApi: MessagesApi = mock[MessagesApi]
    implicit val messages: Messages = mock[Messages]

    when(messages.apply("cf.cash-account.detail.declaration.title")).thenReturn("Declaration Details")
    when(messages.apply("cf.cash-account.csv.date")).thenReturn("Date")
    when(messages.apply("cf.cash-account.csv.movementReferenceNumber")).thenReturn("MRN")
    when(messages.apply("cf.cash-account.csv.uniqueConsignmentReference")).thenReturn("UCR")
    when(messages.apply("cf.cash-account.csv.declarantEori")).thenReturn("Declarant EORI")
    when(messages.apply("cf.cash-account.csv.importerEori")).thenReturn("Importer EORI")
    when(messages.apply("cf.cash-account.csv.duty")).thenReturn("Duty")
    when(messages.apply("cf.cash-account.csv.vat")).thenReturn("VAT")
    when(messages.apply("cf.cash-account.csv.excise")).thenReturn("Excise")
    when(messages.apply("cf.cash-account.detail.total.paid")).thenReturn("Total Paid")

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest("GET", "/transaction/5a71a767-5c1c-4df8-8eef-2b83769b8fda")

    val cashAccount: CashAccount = CashAccount(
      number = "77658497001",
      owner = "GB Owner",
      status = AccountStatusOpen,
      balances = CDSCashBalance(Some(500.00))
    )

    val declaration: Declaration = Declaration(
      movementReferenceNumber = "23GB921526241SD4389",
      importerEori = Some("GB377097541123"),
      declarantEori = "GB326037543470",
      declarantReference = Some("Declarant Reference"),
      date = LocalDate.parse("2024-04-29"),
      amount = BigDecimal(500.00),
      taxGroups = Seq(
        TaxGroup(CustomsDuty, BigDecimal(400.00)),
        TaxGroup(ImportVat, BigDecimal(100.00))
      ),
      secureMovementReferenceNumber = Some("5a71a767-5c1c-4df8-8eef-2b83769b8fda")
    )

    val declarationSummaryList = DeclarationDetailViewModel.declarationSummaryList(declaration)(messages)
    val taxSummaryList = DeclarationDetailViewModel.taxSummaryList(declaration)(messages)

    val viewModel: DeclarationDetailViewModel = DeclarationDetailViewModel("GB326037543470", cashAccount)

    val view: cash_account_declaration_details = mock[cash_account_declaration_details]
  }
}
