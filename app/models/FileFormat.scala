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

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import play.api.{Logger, LoggerLike}

import scala.collection.immutable.SortedSet

sealed abstract class FileFormat(val name: String) extends Ordered[FileFormat] {
  val order: Int

  def compare(that: FileFormat): Int = order.compare(that.order)

  override def toString: String = name
}

object FileFormat {

  case object Pdf extends FileFormat(name = "PDF") {
    val order = 1
  }

  case object Csv extends FileFormat(name = "CSV") {
    val order = 2
  }

  case object UnknownFileFormat extends FileFormat(name = "UNKNOWN FILE FORMAT") {
    val order = 99
  }

  val log: LoggerLike = Logger(this.getClass)

  val SdesFileFormats: SortedSet[FileFormat] = SortedSet(Pdf, Csv)

  def filterFileFormats[T <: SdesFile](allowedFileFormats: SortedSet[FileFormat])(files: Seq[T]): Seq[T] =
    files.filter(file => allowedFileFormats(file.metadata.fileFormat))

  def apply(name: String): FileFormat = name.toUpperCase match {
    case Pdf.name => Pdf
    case Csv.name => Csv
    case _ =>
      log.warn(s"Unknown file format: $name")
      UnknownFileFormat
  }

  def unapply(arg: FileFormat): Option[String] = Some(arg.name)

  implicit val fileFormatFormat: Format[FileFormat] = new Format[FileFormat] {
    def reads(json: JsValue): JsSuccess[FileFormat] = JsSuccess(apply(json.as[String]))

    def writes(obj: FileFormat): JsString = JsString(obj.name)
  }
}
