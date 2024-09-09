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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views
import utils.SpecBase

import java.time.LocalDate
import play.api.Application
import play.api.test.FakeRequest
import uk.gov
import uk.gov.hmrc
import uk.gov.hmrc.govukfrontend.views.Aliases

class DeclarationDetailViewModelSpec extends SpecBase {

  "DeclarationDetailViewModel" should {

    "generate correctly declarationSummaryList data" in new Setup {

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.declarationSummaryList(declaration)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map(row => (
        row.key.content.asInstanceOf[Text].value,
        row.value.content.asInstanceOf[HtmlContent].asHtml.toString
      ))

      val expectedDeclarationSummaryData: Seq[(String, String)] = Seq(
        (messages("cf.cash-account.csv.date"), Formatters.dateAsDayMonthAndYear(declaration.date)),
        (messages("cf.cash-account.csv.movementReferenceNumber"), declaration.movementReferenceNumber),
        (messages("cf.cash-account.csv.uniqueConsignmentReference"), declaration.declarantReference
          .getOrElse(emptyString)),
        (messages("cf.cash-account.csv.declarantEori"), declaration.declarantEori),
        (messages("cf.cash-account.csv.importerEori"), declaration.importerEori.getOrElse(emptyString))
      )

      extractedResultData must contain allElementsOf expectedDeclarationSummaryData
    }


    "generate correctly taxSummaryList data" in new Setup {

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.taxSummaryList(declaration)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map(row => (
        row.key.content.asInstanceOf[Text].value,
        row.value.content.asInstanceOf[HtmlContent].asHtml.toString
      ))

      val expectedTaxData: Seq[(String, String)] = Seq(
        (messages("cf.cash-account.csv.duty"), Formatters.formatCurrencyAmount(fourHundred)),
        (messages("cf.cash-account.csv.vat"), Formatters.formatCurrencyAmount(hundred)),
        (messages("cf.cash-account.csv.excise"), emptyString),
        (messages("cf.cash-account.detail.total.paid"), Formatters.formatCurrencyAmount(fiveHundred))
      )

      extractedResultData must contain allElementsOf expectedTaxData
    }

    "handle missing optional fields correctly in declarationSummaryList" in new Setup {

      val declarationWithMissingFields: Declaration = declaration.copy(
        importerEori = None,
        declarantReference = None
      )

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.declarationSummaryList(declarationWithMissingFields)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map(row => (
        row.key.content.asInstanceOf[Text].value,
        row.value.content.asInstanceOf[HtmlContent].asHtml.toString
      ))

      val expectedDeclarationSummaryData: Seq[(String, String)] = Seq(
        (messages("cf.cash-account.csv.importerEori"), emptyString),
        (messages("cf.cash-account.csv.uniqueConsignmentReference"), emptyString)
      )

      extractedResultData must contain allElementsOf expectedDeclarationSummaryData
    }

    "handle excise duty correctly when present" in new Setup {

      val declarationWithExcise: Declaration = declaration.copy(
        taxGroups = Seq(
          TaxGroup(CustomsDuty, fourHundred, taxTypes),
          TaxGroup(ImportVat, hundred, taxTypes),
          TaxGroup(ExciseDuty, fifty, taxTypes)
        )
      )

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.taxSummaryList(declarationWithExcise)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map(row => (
        row.key.content.asInstanceOf[Text].value,
        row.value.content.asInstanceOf[HtmlContent].asHtml.toString
      ))

      val exciseValue: String = Formatters.formatCurrencyAmount(fifty)

      extractedResultData must contain((messages("cf.cash-account.csv.excise"), exciseValue))
    }

    "handle missing excise duty correctly" in new Setup {

      val summaryList: _root_.uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList =
        DeclarationDetailViewModel.taxSummaryList(declaration)

      val extractedResultData: Seq[(String, String)] = summaryList.rows.map(row => (
        row.key.content.asInstanceOf[Text].value,
        row.value.content.asInstanceOf[HtmlContent].asHtml.toString
      ))

      extractedResultData must contain(messages("cf.cash-account.csv.excise"), emptyString)
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

    val taxTypes: Seq[TaxType] = Seq(TaxType(reasonForSecurity = "Reason", taxTypeID = "50", amount = hundred))

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

    val app: Application = application.build()
    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
  }
}
