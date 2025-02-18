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

import models._
import models.metadata.CashStatementFileMetadata
import play.api.i18n.Messages
import utils.Utils.emptyString

import javax.inject.Singleton

@Singleton
class SdesGatekeeperService {

  implicit def convertToCashStatementFile(
    sdesResponseFile: FileInformation
  )(implicit messages: Messages): CashStatementFile = {

    val metadata = sdesResponseFile.metadata.asMap

    CashStatementFile(
      sdesResponseFile.filename,
      sdesResponseFile.downloadURL,
      sdesResponseFile.fileSize,
      CashStatementFileMetadata(
        metadata("PeriodStartYear").toInt,
        metadata("PeriodStartMonth").toInt,
        metadata("PeriodStartDay").toInt,
        metadata("PeriodEndYear").toInt,
        metadata("PeriodEndMonth").toInt,
        metadata("PeriodEndDay").toInt,
        FileFormat(metadata("FileType")),
        FileRole(metadata("FileRole")),
        metadata.get("statementRequestID")
      ),
      emptyString
    )
  }

  def convertTo[T <: SdesFile](implicit converter: FileInformation => T): Seq[FileInformation] => Seq[T] =
    _.map(converter)
}
