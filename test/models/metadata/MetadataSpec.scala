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

package models.metadata

import play.api.libs.json.Json
import utils.SpecBase

import play.api.libs.json._

class MetadataSpec extends SpecBase {

  "MetaData" must {

    "convert items to map correctly" in {
      val metadataItems = Seq(MetadataItem("key1", "value1"), MetadataItem("key2", "value2"))
      val metadata = Metadata(metadataItems)

      val expectedMap = Map("key1" -> "value1", "key2" -> "value2")
      metadata.asMap mustBe expectedMap
    }

    "write the JSON correctly" in {
      val metadataItems = Seq(MetadataItem("key1", "value1"), MetadataItem("key2", "value2"))
      val metadata = Metadata(metadataItems)

      val expectedJson = Json.arr(
        Json.obj("metadata" -> "key1", "value" -> "value1"),
        Json.obj("metadata" -> "key2", "value" -> "value2")
      )

      Json.toJson(metadata) mustBe expectedJson
    }

    "read the JSON correctly" in {
      val json = Json.arr(
        Json.obj("metadata" -> "key1", "value" -> "value1"),
        Json.obj("metadata" -> "key2", "value" -> "value2")
      )

      val expectedMetadataItems = Seq(MetadataItem("key1", "value1"), MetadataItem("key2", "value2"))
      json.as[Metadata] mustBe Metadata(expectedMetadataItems)
    }
  }
}
