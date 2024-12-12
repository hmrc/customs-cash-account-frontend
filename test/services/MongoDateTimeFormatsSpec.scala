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

package services

import models.MongoDateTimeFormats
import utils.SpecBase

import java.time.LocalDateTime

class MongoDateTimeFormatsSpec extends SpecBase {

  "MongoDateTimeFormats" must {

    "localDateTimeWrite must return valid value" in {
      val day    = 1
      val month  = 1
      val year   = 2023
      val minute = 1
      val hour   = 1

      val dateTime = LocalDateTime.of(year, month, day, hour, minute)

      val test   = MongoDateTimeFormats.localDateTimeWrite
      val result = test.writes(dateTime)

      result.toString() mustBe "{\"$date\":1672534860000}"
    }
  }
}
