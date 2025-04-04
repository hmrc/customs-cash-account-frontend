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

import helpers.Formatters
import models.metadata.{CashStatementFileMetadata, SdesFileMetadata}
import play.api.i18n.Messages
import utils.Utils.emptyString

import java.time.LocalDate

trait SdesFile {
  def metadata: SdesFileMetadata

  def downloadURL: String

  val fileFormat: FileFormat  = metadata.fileFormat
  val monthAndYear: LocalDate = LocalDate.of(metadata.periodStartYear, metadata.periodStartMonth, 1)
}

case class CashStatementFile(
  filename: String,
  downloadURL: String,
  size: Long,
  metadata: CashStatementFileMetadata,
  eori: String = emptyString
)(implicit messages: Messages)
    extends Ordered[CashStatementFile]
    with SdesFile {

  val formattedSize: String  = Formatters.fileSizeFormat(size)
  val formattedMonth: String = Formatters.dateAsMonth(monthAndYear)

  def compare(that: CashStatementFile): Int = that.metadata.fileFormat.compare(metadata.fileFormat)
}
