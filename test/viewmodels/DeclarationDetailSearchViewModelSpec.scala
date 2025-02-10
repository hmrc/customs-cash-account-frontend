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
import models.response.{
  DeclarationSearch, TaxGroupSearch, TaxGroupWrapper, TaxTypeWithSecurity, TaxTypeWithSecurityContainer
}
import uk.gov
import uk.gov.hmrc
import uk.gov.hmrc.govukfrontend
import uk.gov.hmrc.govukfrontend.views
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utils.SpecBase

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DeclarationDetailSearchViewModelSpec extends SpecBase {

  "DeclarationDetailSearchViewModel" should {

    "generate correct declarationSummaryList data" in new Setup {

      val viewModel: DeclarationDetailSearchViewModel =
        DeclarationDetailSearchViewModel(searchInput, account, declaration)

      val extractedResultData: Seq[(String, String)] = extractSummaryData(viewModel.declarationSummaryList)
      val dateFormatter: DateTimeFormatter           = DateTimeFormatter.ofPattern("yyyy-MM-dd")

      val expectedDeclarationSummaryData: Seq[(String, String)] = Seq(
        messages("cf.cash-account.csv.date")                       ->
          Formatters.dateAsDayMonthAndYear(LocalDate.parse(declaration.postingDate, dateFormatter)),
        messages("cf.cash-account.csv.uniqueConsignmentReference") -> declaration.declarantRef.getOrElse(emptyString),
        messages("cf.cash-account.csv.declarantEori")              -> declaration.declarantEORINumber,
        messages("cf.cash-account.csv.importerEori")               -> declaration.importersEORINumber
      )

      extractedResultData must contain allElementsOf expectedDeclarationSummaryData
    }

    "generate correct taxSummaryList data" in new Setup {

      val declarationWithTaxGroups: DeclarationSearch = declaration.copy(
        taxGroups = Seq(
          TaxGroupWrapper(
            taxGroup = TaxGroupSearch(
              taxGroupDescription = "Import VAT",
              amount = hundred.toDouble,
              taxTypes = Seq(taxType1)
            )
          ),
          TaxGroupWrapper(
            taxGroup = TaxGroupSearch(
              taxGroupDescription = "Customs",
              amount = threeHundred.toDouble,
              taxTypes = Seq(taxType1)
            )
          ),
          TaxGroupWrapper(
            taxGroup = TaxGroupSearch(
              taxGroupDescription = "Excise",
              amount = hundred.toDouble,
              taxTypes = Seq(taxType1)
            )
          )
        )
      )

      val viewModel: DeclarationDetailSearchViewModel =
        DeclarationDetailSearchViewModel(searchInput, account, declarationWithTaxGroups)

      val extractedResultData: Seq[(String, String)] = extractSummaryData(viewModel.taxSummaryList)

      val zeroValue: String         = Formatters.formatCurrencyAmount(BigDecimal(0))
      val hundredValue: String      = Formatters.formatCurrencyAmount(hundred.toDouble)
      val threeHundredValue: String = Formatters.formatCurrencyAmount(threeHundred.toDouble)

      val expectedTaxData: Seq[(String, String)] = Seq(
        messages("cf.cash-account.csv.duty")          -> threeHundredValue,
        messages("cf.cash-account.csv.vat")           -> hundredValue,
        messages("cf.cash-account.csv.excise")        -> hundredValue,
        messages("cf.cash-account.detail.total.paid") -> zeroValue
      )

      extractedResultData must contain allElementsOf expectedTaxData
    }

    "handle missing optional fields correctly in declarationSummaryList" in new Setup {

      val declarationWithMissingFields: DeclarationSearch =
        declaration.copy(importersEORINumber = emptyString, declarantRef = None)

      val viewModel: DeclarationDetailSearchViewModel =
        DeclarationDetailSearchViewModel(searchInput, account, declarationWithMissingFields)

      val extractedResultData: Seq[(String, String)] = extractSummaryData(viewModel.declarationSummaryList)

      val expectedDeclarationSummaryData: Seq[(String, String)] = Seq(
        messages("cf.cash-account.csv.importerEori")               -> emptyString,
        messages("cf.cash-account.csv.uniqueConsignmentReference") -> emptyString
      )

      extractedResultData must contain allElementsOf expectedDeclarationSummaryData
    }

    "handle excise duty correctly when present" in new Setup {

      val hundredValue: String = Formatters.formatCurrencyAmount(hundred.toDouble)

      val declarationWithExcise: DeclarationSearch = declaration.copy(
        taxGroups = Seq(
          TaxGroupWrapper(
            taxGroup = TaxGroupSearch(
              taxGroupDescription = "Excise",
              amount = hundred.toDouble,
              taxTypes = Seq(taxType1)
            )
          )
        )
      )

      val viewModel: DeclarationDetailSearchViewModel =
        DeclarationDetailSearchViewModel(searchInput, account, declarationWithExcise)

      val extractedResultData: Seq[(String, String)] = extractSummaryData(viewModel.taxSummaryList)

      extractedResultData must contain(messages("cf.cash-account.csv.excise") -> hundredValue)
    }

    "handle missing excise duty correctly" in new Setup {

      val viewModel: DeclarationDetailSearchViewModel =
        DeclarationDetailSearchViewModel(searchInput, account, declaration)

      val extractedResultData: Seq[(String, String)] = extractSummaryData(viewModel.taxSummaryList)

      extractedResultData must contain(messages("cf.cash-account.csv.excise") -> emptyString)
    }
  }

  trait Setup {

    val zero: BigDecimal         = BigDecimal(0)
    val hundred: BigDecimal      = BigDecimal(100.00)
    val threeHundred: BigDecimal = BigDecimal(300.00)

    val movementReferenceNumber            = "MRN1234567890"
    val importerEori: Option[String]       = Some("GB123456789000")
    val declarantEori                      = "GB987654321000"
    val declarantReference: Option[String] = Some("UCR12345")
    val eori                               = "GB123456789000"
    val searchInput                        = "someInput"
    val owner                              = "someOwner"

    val account: CashAccount = CashAccount("Account123", owner, AccountStatusOpen, CDSCashBalance(Some(threeHundred)))

    val taxType1: TaxTypeWithSecurityContainer = TaxTypeWithSecurityContainer(
      TaxTypeWithSecurity(
        reasonForSecurity = Some("Import VAT"),
        taxTypeID = "50",
        amount = zero.toDouble
      )
    )

    val declaration: DeclarationSearch = DeclarationSearch(
      declarationID = movementReferenceNumber,
      declarantEORINumber = declarantEori,
      declarantRef = declarantReference,
      c18OrOverpaymentReference = Some("C18Reference"),
      importersEORINumber = importerEori.getOrElse(emptyString),
      postingDate = "2024-04-29",
      acceptanceDate = "2024-04-28",
      amount = zero.toDouble,
      taxGroups = Seq(
        TaxGroupWrapper(
          TaxGroupSearch(
            taxGroupDescription = "Import VAT",
            amount = zero.toDouble,
            taxTypes = Seq(taxType1)
          )
        )
      )
    )

    def extractSummaryData(summaryList: SummaryList): Seq[(String, String)] =
      summaryList.rows.map { row =>
        (
          row.key.content.asInstanceOf[HtmlContent].asHtml.toString,
          row.value.content.asInstanceOf[HtmlContent].asHtml.toString
        )
      }
  }
}
