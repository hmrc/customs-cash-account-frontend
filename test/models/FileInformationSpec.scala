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

package models

import models.metadata.{Metadata, MetadataItem}
import play.api.libs.json.{JsObject, JsResult, JsResultException, JsSuccess, JsValue, Json}
import utils.SpecBase

class FileInformationSpec extends SpecBase {

  "FileInformation" should {

    "create an instance correctly" in new Setup {
      fileInfo.filename mustBe filename
      fileInfo.downloadURL mustBe downloadURL
      fileInfo.fileSize mustBe fileSize
      fileInfo.metadata mustBe metadata
    }

    "serialize and deserialize correctly" when {

      "handling single metadata item" in new Setup {
        Json.toJson(fileInfo) mustBe expectedJson(fileInfo)
        expectedJson(fileInfo).validate[FileInformation] mustBe JsSuccess(fileInfo)
      }

      "handling multiple metadata items" in new Setup {
        val multiMetadata: Metadata = Metadata(
          Seq(
            MetadataItem("keyHolder1", "valueHolder1"),
            MetadataItem("keyHolder2", "valueHolder2")
          )
        )

        val multiFileInfo: FileInformation = FileInformation("multi", "multiURL", fileSize + 2000, multiMetadata)
        val multiJson: JsObject            = Json.obj(
          "filename"    -> "multi",
          "downloadURL" -> "multiURL",
          "fileSize"    -> (fileSize + 2000),
          "metadata"    -> Json.arr(
            Json.obj(
              "metadata" -> "keyHolder1",
              "value"    -> "valueHolder1"
            ),
            Json.obj(
              "metadata" -> "keyHolder2",
              "value"    -> "valueHolder2"
            )
          )
        )

        Json.toJson(multiFileInfo) mustBe multiJson
        multiJson.validate[FileInformation] mustBe JsSuccess(multiFileInfo)
      }

      "handling empty metadata" in new Setup {
        val emptyMetadata: Metadata        = Metadata(Seq.empty)
        val emptyFileInfo: FileInformation =
          FileInformation("empty_metadata", "emptyURL", fileSize + 1000, emptyMetadata)

        val emptyJson: JsObject = Json.obj(
          "filename"    -> "empty_metadata",
          "downloadURL" -> "emptyURL",
          "fileSize"    -> (fileSize + 1000),
          "metadata"    -> Json.arr()
        )

        Json.toJson(emptyFileInfo) mustBe emptyJson
        emptyJson.validate[FileInformation] mustBe JsSuccess(emptyFileInfo)
      }
    }

    "fail to deserialize when invalid" in new Setup {
      val invalidJson: JsObject = Json.obj(
        "filename"    -> 12345,
        "downloadURL" -> true,
        "fileSize"    -> "large",
        "metadata"    -> Json.arr(
          Json.obj(
            "metadata" -> 67890,
            "value"    -> false
          )
        )
      )

      assertThrows[JsResultException] {
        invalidJson.as[FileInformation]
      }
    }

    "fail to deserialize with missing fields" in new Setup {
      val incompleteJson: JsObject = Json.obj(
        "filename" -> "incomplete"
      )

      assertThrows[JsResultException] {
        incompleteJson.as[FileInformation]
      }
    }
  }

  trait Setup {

    val fileSize: Long            = 100L
    val filename: String          = "fileName.csv"
    val downloadURL: String       = "downloadURL"
    val metadata: Metadata        = Metadata(Seq(MetadataItem("keyHolder", "valueHolder")))
    val fileInfo: FileInformation = FileInformation(filename, downloadURL, fileSize, metadata)

    def expectedJson(file: FileInformation): JsObject = Json.obj(
      "filename"    -> file.filename,
      "downloadURL" -> file.downloadURL,
      "fileSize"    -> file.fileSize,
      "metadata"    -> Json.arr(
        Json.obj(
          "metadata" -> "keyHolder",
          "value"    -> "valueHolder"
        )
      )
    )
  }
}
