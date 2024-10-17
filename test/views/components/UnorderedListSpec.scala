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

package views.components

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import utils.SpecBase
import views.html.components.unorderedList
import utils.TestData.TEST_ID

class UnorderedListSpec extends SpecBase {

  "component" should {

    "not display the unordered list when listItems are empty" in {
      val unorderedList: Document = Jsoup.parse(new unorderedList().apply(id = Some(TEST_ID)).body)

      unorderedList.select(s"#$TEST_ID").size() mustBe 0
    }

    "display the unordered list correctly when listItems are present" in {
      val itemList = List("test_msg1", "test_msg2", "test_msg3")

      val unorderedList: Document = Jsoup.parse(new unorderedList().apply(id = Some(TEST_ID), listItems = itemList).body)

      unorderedList.select(s"#$TEST_ID").size() must be > 0
      unorderedList.getElementsByTag("li").size() mustBe 3

      itemList.foreach(msg => unorderedList.html().contains(msg) mustBe true)
    }

    "display the unordered list correctly (with provided style class) when classes and listItems are present" in {
      val customClass = "test_class"
      val itemList = List("test_msg1", "test_msg2", "test_msg3")

      val unorderedList: Document =
        Jsoup.parse(new unorderedList().apply(listItems = itemList, classes = Some(customClass)).body)

      unorderedList.select(s".$customClass").size() must be > 0
      unorderedList.getElementsByTag("li").size() mustBe 3

      itemList.foreach(msg => unorderedList.html().contains(msg) mustBe true)
    }
  }
}
