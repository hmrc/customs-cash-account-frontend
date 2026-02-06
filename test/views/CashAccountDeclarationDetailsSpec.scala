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

import models._
import viewmodels.DeclarationDetailViewModel
import views.html.cash_account_declaration_details
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import java.time.LocalDate

class CashAccountDeclarationDetailsSpec extends ViewTestHelper {

  "CashAccountDeclarationDetails view" should {

    "render the correct title and headings" in new Setup {

      titleShouldBeCorrect(viewDoc, "cf.cash-account.detail.declaration.title")

      viewDoc.getElementsByTag("h1").text() mustBe messages("cf.cash-account.detail.declaration.title")

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

    "have a backlink" in new Setup {

      val expectedBackLinkUrl: String =
        controllers.routes.CashAccountV2Controller.showAccountDetails(page = pageNumber).url

      shouldContainBackLinkUrl(viewDoc, expectedBackLinkUrl)
    }
  }

  trait Setup {

    val year2024        = 2024
    val month4          = 4
    val day4            = 4
    val date: LocalDate = LocalDate.of(year2024, month4, day4)

    val fiveHundred: BigDecimal = BigDecimal(500.00)
    val fourHundred: BigDecimal = BigDecimal(400.00)
    val hundred: BigDecimal     = BigDecimal(100.00)

    val pageNumber: Option[Int] = Some(1)

    val eori                               = "GB987654321000"
    val number                             = "123456789"
    val owner                              = "GB491235123123"
    val movementReferenceNumber            = "MRN1234567890"
    val importerEori: Option[String]       = Some("GB123456789000")
    val declarantEori                      = "GB987654321000"
    val declarantReference: Option[String] = Some("UCR12345")

    val taxTypes: Seq[TaxType] =
      Seq(TaxType(reasonForSecurity = Some("Reason"), taxTypeID = "50", amount = fourHundred))

    val declaration: Declaration = Declaration(
      movementReferenceNumber = movementReferenceNumber,
      importerEori = importerEori,
      declarantEori = declarantEori,
      declarantReference = declarantReference,
      date = date,
      amount = hundred,
      taxGroups = Seq(
        TaxGroup(CustomsDuty, fiveHundred, taxTypes),
        TaxGroup(ImportVat, fourHundred, taxTypes),
        TaxGroup(ExciseDuty, hundred, taxTypes)
      ),
      secureMovementReferenceNumber = None
    )

    val viewModel: DeclarationDetailViewModel = DeclarationDetailViewModel(
      eori = eori,
      account = CashAccount(
        number = number,
        owner = owner,
        status = AccountStatusOpen,
        balances = CDSCashBalance(Some(fiveHundred))
      ),
      declaration = declaration
    )(messages)

    val cashAccountDeclarationDetails: cash_account_declaration_details =
      app.injector.instanceOf[cash_account_declaration_details]

    val viewDoc: Document = Jsoup.parse(
      cashAccountDeclarationDetails.apply(viewModel, pageNumber)(request, messages).body
    )
  }
}
