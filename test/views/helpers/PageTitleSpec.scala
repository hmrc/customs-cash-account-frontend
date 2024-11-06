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

package views.helpers

import utils.SpecBase

class PageTitleSpec extends SpecBase {

  "fullPageTitle" should {

    "return correct string for present title" in {
      val res: Option[String] = PageTitle.fullPageTitle(Some("abc"))

      res mustBe Some("abc - Manage import duties and VAT accounts - GOV.UK")
    }

    "return correct string for no title" in {
      val res: Option[String] = PageTitle.fullPageTitle(Some(""))

      res mustBe Some(" - Manage import duties and VAT accounts - GOV.UK")
    }
  }

}
