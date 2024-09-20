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

import utils.SpecBase
import FileFormat.*
import play.api.libs.json.{JsString, JsSuccess, Json}

class FileFormatSpec extends SpecBase {

  "FileFormat" should {

    "compare FileFormats correctly" in {
      Pdf < Csv mustBe true
      Csv < UnknownFileFormat mustBe true
      UnknownFileFormat > Pdf mustBe true
      Pdf compare Csv must be <0
      Csv compare Pdf must be >0
      UnknownFileFormat compare Csv must be >0
      Pdf compare Pdf mustBe 0
    }
  }

  "FileFormat" should {

    "return the correct strings" in {
      Pdf mustBe FileFormat.Pdf
      Csv mustBe FileFormat.Csv
      UnknownFileFormat.toString mustBe "UNKNOWN FILE FORMAT"
    }
  }

  "FileFormat serialization" should {

    "serialize correctly" in {
      val pdfJson = Json.toJson[FileFormat](Pdf)
      pdfJson mustBe JsString("PDF")

      val csvJson = Json.toJson[FileFormat](Csv)
      csvJson mustBe JsString("CSV")

      val unknownJson = Json.toJson[FileFormat](UnknownFileFormat)
      unknownJson mustBe JsString("UNKNOWN FILE FORMAT")
    }
  }

  "FileFormat deserialization" should {

    "deserialize correctly" in {
      val pdf = Json.fromJson[FileFormat](JsString("PDF"))
      pdf mustBe JsSuccess(Pdf)

      val csv = Json.fromJson[FileFormat](JsString("CSV"))
      csv mustBe JsSuccess(Csv)

      val unknown = Json.fromJson[FileFormat](JsString("exe"))
      unknown mustBe JsSuccess(UnknownFileFormat)
    }
  }
}
