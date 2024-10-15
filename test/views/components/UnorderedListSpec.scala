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

class UnorderedListSpec extends SpecBase {

  "component" should {

    "not display the component when li msg list is empty" in {
      val id = "testId"
      val unorderedList: Document = Jsoup.parse(new unorderedList().apply(id = Some(id)).body)

      unorderedList.select(s"#$id").size() mustBe 0
    }

    "display the component correctly when li msg list is present" in {
      val id = "testId"
      val msgList = List("test_msg1", "test_msg2", "test_msg3")

      val unorderedList: Document = Jsoup.parse(new unorderedList().apply(id = Some(id), liMsgList = msgList).body)

      unorderedList.select(s"#$id").size() must be > 0
      unorderedList.getElementsByTag("li").size() mustBe 3

      msgList.foreach(msg => unorderedList.html().contains(msg) mustBe true)
    }

    "display the component correctly (with provided style class) when classes and li msg list are present" in {
      val customClass = "test_class"
      val msgList = List("test_msg1", "test_msg2", "test_msg3")

      val unorderedList: Document =
        Jsoup.parse(new unorderedList().apply(liMsgList = msgList, classes = Some(customClass)).body)

      unorderedList.select(s".$customClass").size() must be > 0
      unorderedList.getElementsByTag("li").size() mustBe 3

      msgList.foreach(msg => unorderedList.html().contains(msg) mustBe true)
    }
  }
}
