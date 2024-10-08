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

package views

import models.*
import models.response.{
  DeclarationSearch,
  DeclarationWrapper,
  TaxGroupSearch,
  TaxGroupWrapper,
  TaxTypeWithSecurity,
  TaxTypeWithSecurityContainer
}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import viewmodels.DeclarationDetailSearchViewModel
import views.html.cash_account_declaration_details_search

class CashAccountDeclarationDetailsSearchSpec extends ViewTestHelper {

  "CashAccountDeclarationDetailsSearch view" should {

    "render the correct title and headings" in new Setup {

      titleShouldBeCorrect(viewDoc, "cf.cash-account.detail.title")

      viewDoc.getElementsByTag("h1").text() mustBe s"Search results for $movementReferenceNumber"

      val accountNumber = s"${messages("cf.cash-account.detail.account", viewModel.account.number)}"

      viewDoc.getElementById("account-number").text() mustBe accountNumber
    }

    "have declaration and tax summary lists present" in new Setup {

      val summaryLists: Elements = viewDoc.getElementsByClass("govuk-summary-list")
      summaryLists.size mustBe 2

      val declarationSummaryList: Element = summaryLists.get(0)
      declarationSummaryList must not be None

      val taxSummaryList: Element = summaryLists.get(1)
      taxSummaryList must not be None
    }
  }

  trait Setup {

    val fiveHundred: BigDecimal = BigDecimal(500.00)
    val pageNumber: Option[Int] = Some(1)

    val eori = "GB987654321000"
    val number = "123456789"
    val owner = "GB491235123123"
    val movementReferenceNumber = "MRN1234567890"

    val declaration: Seq[DeclarationWrapper] = Seq(
      DeclarationWrapper(DeclarationSearch(
        declarationID = "18GB9JLC3CU1LFGVR8",
        declarantEORINumber = "GB123456789",
        importersEORINumber = "GB987654321",
        postingDate = "2022-07-15",
        acceptanceDate = "2022-07-01",
        amount = 2500.0,
        taxGroups = Seq(
          TaxGroupWrapper(TaxGroupSearch(
            taxGroupDescription = "VAT",
            amount = 2000.0,
            taxTypes = Seq(TaxTypeWithSecurityContainer(
              TaxTypeWithSecurity(
                reasonForSecurity = Some("Duty"),
                taxTypeID = "A10",
                amount = 2000.0
              )))
          ))
        )
      ))
    )

    val singleDeclaration: DeclarationSearch = declaration.head.declaration
    val viewModel: DeclarationDetailSearchViewModel = DeclarationDetailSearchViewModel(
      searchInput = movementReferenceNumber,
      account = CashAccount(
        number = number,
        owner = owner,
        status = AccountStatusOpen,
        balances = CDSCashBalance(Some(fiveHundred))
      ),
      eori = eori,
      declaration = singleDeclaration)(messages)

    val cashAccountDeclarationDetails: cash_account_declaration_details_search =
      app.injector.instanceOf[cash_account_declaration_details_search]

    val viewDoc: Document = Jsoup.parse(
      cashAccountDeclarationDetails.apply(viewModel, pageNumber)(request, messages).body
    )
  }
}
