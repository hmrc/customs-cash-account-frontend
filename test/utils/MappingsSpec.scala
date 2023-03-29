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

package utils

import java.time.LocalDate
import forms.mappings._
import play.api.data.FieldMapping
import play.api.data.format.Formats.{booleanFormat, localDateFormat, stringFormat}

class MappingsSpec extends SpecBase with Mappings {

  "Mappings" must {
    "boolean" in {
      val map = FieldMapping[Boolean]("",List.empty)
      val test = boolean()
      test mustBe map
    }

    "localDate - end of month is true" in {
      val map = FieldMapping[LocalDate]("",List.empty)
      val test = localDate("",true)
      test mustBe map
    }

    "localDate - end of month is false" in {
      val map = FieldMapping[LocalDate]("",List.empty)
      val test = localDate("",false)
      test mustBe map
    }

    "decimal" in {
      val map = FieldMapping[String]("",List.empty)
      val test = decimal()
      test mustBe map
    }
  }
}
