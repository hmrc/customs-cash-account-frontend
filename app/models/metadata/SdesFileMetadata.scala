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

import models.{FileFormat, FileRole}

trait SdesFileMetadata {
  this: Product =>

  def fileFormat: FileFormat

  def fileRole: FileRole

  def periodStartYear: Int

  def periodStartMonth: Int

  def toMap[T <: SdesFileMetadata with Product]: Map[String, String] = {
    val fieldNames: Seq[String] = getClass.getDeclaredFields.toIndexedSeq.map(_.getName)
    val fieldValues: Seq[String] = productIterator.toSeq.map(_.toString)

    fieldNames.zip(fieldValues).toMap
  }
}

case class CashStatementFileMetadata(periodStartYear: Int,
                                     periodStartMonth: Int,
                                     periodStartDay: Int,
                                     periodEndYear: Int,
                                     periodEndMonth: Int,
                                     periodEndDay: Int,
                                     fileFormat: FileFormat,
                                     fileRole: FileRole,
                                     statementRequestId: Option[String]) extends SdesFileMetadata
