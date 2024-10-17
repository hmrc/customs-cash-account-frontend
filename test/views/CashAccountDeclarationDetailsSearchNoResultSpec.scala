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

import utils.SpecBase
import behaviours.{ComponentDetailsForAssertion, LinkDetails, GuidancePageBehaviour}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.cash_account_declaration_details_search_no_result

class CashAccountDeclarationDetailsSearchNoResultSpec extends SpecBase with GuidancePageBehaviour {

  val accNumber = "12345678"
  val searchInputValue = "test_MNR"

  override val view: Document = Jsoup.parse(app.injector.instanceOf[cash_account_declaration_details_search_no_result]
    .apply(Some(1), accNumber, searchInputValue).body)

  override val titleMsgKey: String = "cf.cash-account.detail.title"
  override val backLink: Option[String] = Some(controllers.routes.CashAccountV2Controller.showAccountDetails(Some(1)).url)
  override val componentIdsToVerify: List[String] = populateComponentIdsToVerify

  override val otherComponentGuidanceList: List[ComponentDetailsForAssertion] =
    populateComponentGuidanceList(accNumber, searchInputValue)

  override val linksToVerify: List[LinkDetails] = populateLinksToVerify()

  "view" should {
    behave like guidancePage()
  }

  private def populateComponentIdsToVerify = {
    List("account-number", "search-result-heading", "search-result-guidance-not-returned-any-results",
      "search-result-guidance-because-you-entered", "invalid-inputs-guidance-list", "search-again-link")
  }

  private def populateComponentGuidanceList(accNumber: String,
                                            searchInput: String) = {
    val accountHeadingAndValue = ComponentDetailsForAssertion(
      testDescription = "display correct account label with number",
      id = Some("account-number"),
      expectedValue = s"Account: $accNumber")

    val searchResultSubHeading = ComponentDetailsForAssertion(
      testDescription = "display correct search result heading with value",
      id = Some("search-result-heading"),
      expectedValue = s"Search results for $searchInput")

    val searchResultParagraph1 = ComponentDetailsForAssertion(
      testDescription = "display correct search result guidance's first paragraph",
      id = Some("search-result-guidance-not-returned-any-results"),
      expectedValue = s"Your search $searchInput has not returned any results.")

    val searchResultParagraph2 = ComponentDetailsForAssertion(
      testDescription = "display correct search result guidance's second paragraph",
      id = Some("search-result-guidance-because-you-entered"),
      expectedValue = "This could be because you entered:")

    val unorderedListGuidance = ComponentDetailsForAssertion(
      testDescription = "display correct guidance for invalid inputs ",
      id = Some("invalid-inputs-guidance-list"),
      expectedValue =
        "an incorrect Movement Reference Number (MRN) or Unique Consignment Number (UCR)" +
          " a payment amount that was not found a payment amount that does not include ‘£’")

    List(accountHeadingAndValue,
      searchResultSubHeading,
      searchResultParagraph1,
      searchResultParagraph2,
      unorderedListGuidance)
  }

  private def populateLinksToVerify() = {
    List(
      LinkDetails(id = Some("search-again-link"),
        url = controllers.routes.CashAccountV2Controller.showAccountDetails(Some(1)).url,
        urlText = "Search again")
    )
  }
}
