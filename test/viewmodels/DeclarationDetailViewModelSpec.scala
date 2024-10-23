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
import models.domain.EORI
import models.{CustomsDuty, Declaration, ExciseDuty, ImportVat, TaxGroup, TaxType}
import org.mockito.Mockito.*
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views
import utils.SpecBase
import utils.Utils.singleSpace

import java.time.LocalDate
import play.api.Application
import play.api.test.FakeRequest
import uk.gov
import uk.gov.hmrc
import uk.gov.hmrc.govukfrontend.views.Aliases

class DeclarationDetailViewModelSpec extends SpecBase {

  "DeclarationDetailViewModel" should {

    "generate correctly declarationSummaryList data with normalized HTML content" in new Setup {

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.declarationSummaryList(declaration)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map { row =>
        (
          normalizeHtml(row.key.content.asInstanceOf[HtmlContent].asHtml.toString),
          normalizeHtml(row.value.content.asInstanceOf[HtmlContent].asHtml.toString)
        )
      }

      val expectedDeclarationSummaryData: Seq[(String, String)] = Seq(
        (normalizeHtml(messages("cf.cash-account.csv.date")), Formatters.dateAsDayMonthAndYear(declaration.date)),
        (normalizeHtml(
          s"""
            ${messages("cf.cash-account.csv.declaration")}
            <abbr title="${messages("cf.cash-account.detail.movement-reference-number")}">
              ${messages("cf.cash-account.detail.mrn")}
            </abbr>
          """), declaration.movementReferenceNumber),
        (normalizeHtml(
          s"""
            ${messages("cf.cash-account.csv.declaration")}
            <abbr title="${messages("cf.cash-account.detail.unique-consignment-reference")}">
              ${messages("cf.cash-account.detail.ucr")}
            </abbr>
          """), declaration.declarantReference.getOrElse(emptyString)),
        (normalizeHtml(
          s"""
            ${messages("cf.cash-account.detail.declarant")}
            <abbr title="${messages("cf.cash-account.detail.eori-definition")}">
              ${messages("cf.cash-account.detail.eori")}
            </abbr>
          """), declaration.declarantEori),
        (normalizeHtml(messages("cf.cash-account.detail.importerEori")), declaration.importerEori.getOrElse(emptyString))
      )

      extractedResultData must contain allElementsOf expectedDeclarationSummaryData
    }

    "generate correctly taxSummaryList data with normalized HTML content" in new Setup {

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.taxSummaryList(declaration)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map { row =>
        (
          normalizeHtml(row.key.content.asInstanceOf[HtmlContent].asHtml.toString),
          normalizeHtml(row.value.content.asInstanceOf[HtmlContent].asHtml.toString)
        )
      }

      val expectedTaxData: Seq[(String, String)] = Seq(
        (normalizeHtml(messages("cf.cash-account.csv.duty")), Formatters.formatCurrencyAmount(fourHundred)),
        (normalizeHtml(messages("cf.cash-account.csv.vat")), Formatters.formatCurrencyAmount(hundred)),
        (normalizeHtml(messages("cf.cash-account.csv.excise")), emptyString),
        (normalizeHtml(messages("cf.cash-account.detail.total.paid")), Formatters.formatCurrencyAmount(fiveHundred))
      )

      extractedResultData must contain allElementsOf expectedTaxData
    }

    "handle missing optional fields correctly in declarationSummaryList with normalized HTML content" in new Setup {

      val declarationWithMissingFields: Declaration = declaration.copy(
        importerEori = None,
        declarantReference = None
      )

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.declarationSummaryList(declarationWithMissingFields)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map { row =>
        (
          normalizeHtml(row.key.content.asInstanceOf[HtmlContent].asHtml.toString),
          normalizeHtml(row.value.content.asInstanceOf[HtmlContent].asHtml.toString)
        )
      }

      val expectedDeclarationSummaryData: Seq[(String, String)] = Seq(
        (normalizeHtml(messages("cf.cash-account.detail.importerEori")), emptyString),
        (normalizeHtml(s"""
          ${messages("cf.cash-account.csv.declaration")}
          <abbr title="${messages("cf.cash-account.detail.unique-consignment-reference")}">
            UCR
          </abbr>
        """), emptyString)
      )

      extractedResultData must contain allElementsOf expectedDeclarationSummaryData
    }

    "handle excise duty correctly when present with normalized HTML content" in new Setup {

      val declarationWithExcise: Declaration = declaration.copy(
        taxGroups = Seq(
          TaxGroup(CustomsDuty, fourHundred, taxTypes),
          TaxGroup(ImportVat, hundred, taxTypes),
          TaxGroup(ExciseDuty, fifty, taxTypes)
        )
      )

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.taxSummaryList(declarationWithExcise)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map { row =>
        (
          normalizeHtml(row.key.content.asInstanceOf[HtmlContent].asHtml.toString),
          normalizeHtml(row.value.content.asInstanceOf[HtmlContent].asHtml.toString)
        )
      }

      val exciseValue: String = Formatters.formatCurrencyAmount(fifty)

      extractedResultData must contain((normalizeHtml(messages("cf.cash-account.csv.excise")), exciseValue))
    }

    "handle missing excise duty correctly with normalized HTML content" in new Setup {

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.taxSummaryList(declaration)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map { row =>
        (
          normalizeHtml(row.key.content.asInstanceOf[HtmlContent].asHtml.toString),
          normalizeHtml(row.value.content.asInstanceOf[HtmlContent].asHtml.toString)
        )
      }

      extractedResultData must contain((normalizeHtml(messages("cf.cash-account.csv.excise")), emptyString))
    }
  }

  trait Setup {

    val year2024 = 2024
    val month4 = 4
    val day4 = 29
    val date: LocalDate = LocalDate.of(year2024, month4, day4)

    val fiveHundred: BigDecimal = BigDecimal(500.00)
    val fourHundred: BigDecimal = BigDecimal(400.00)
    val hundred: BigDecimal = BigDecimal(100.00)
    val fifty: BigDecimal = BigDecimal(50.00)

    val eori = "GB987654321000"
    val number = "123456789"
    val owner = "GB491235123123"
    val movementReferenceNumber = "MRN1234567890"
    val importerEori: Option[String] = Some("GB123456789000")
    val declarantEori = "GB987654321000"
    val declarantReference: Option[String] = Some("UCR12345")
    val secureMovementReferenceNumber: Option[String] = Some("5a71a767-5c1c-4df8-8eef-2b83769b8fda")

    val taxTypes: Seq[TaxType] = Seq(TaxType(reasonForSecurity = Some("Reason"), taxTypeID = "50", amount = hundred))

    val declaration: Declaration = Declaration(
      movementReferenceNumber = movementReferenceNumber,
      importerEori = importerEori,
      declarantEori = declarantEori,
      declarantReference = declarantReference,
      date = date,
      amount = fiveHundred,
      taxGroups = Seq(
        TaxGroup(CustomsDuty, fourHundred, taxTypes),
        TaxGroup(ImportVat, hundred, taxTypes)
      ),
      secureMovementReferenceNumber = secureMovementReferenceNumber
    )

    def normalizeHtml(html: String): String = html.replaceAll("\\s+", singleSpace).trim

    val app: Application = application.build()
    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
  }
}
