/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import org.scalatest.matchers.should.Matchers.{should, shouldBe, shouldEqual}
import utils.SpecBase

class JamieFormFieldsSpec extends SpecBase {
  "JamieFormFields" should {
    "correctly unapply" in new Setup {
      jamieForm match {
        case JamieFormFields(name, age) =>
          name shouldEqual "Jamie"
          age shouldEqual 41
        case _ => fail("Pattern did not match")
      }
    }
    "return a Option containing name and age" in new Setup {
      val result: Option[(String, Int)] = JamieFormFields.unapply(jamieForm)
      result shouldBe Some(("Jamie", 41))
    }
    
    "should not return a None for valid instances" in new Setup {
      JamieFormFields.unapply(jamieForm) should not be None
    }
  }

  trait Setup {
    val string: String = "Jamie"
    val number: Int = 41
    val jamieForm: JamieFormFields = JamieFormFields(string, number)
  }
}
