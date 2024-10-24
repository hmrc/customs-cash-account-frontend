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
import utils.SpecBase

class SdesFileMetadataSpec extends SpecBase {

  "toMap" must {
    "correctly convert CashStatementFileMetadata to a Map" in new Setup {
      val metadata: CashStatementFileMetadata = CashStatementFileMetadata(
        periodStartYear = periodStartYear,
        periodStartMonth = periodStartMonth,
        periodStartDay = periodStartDay,
        periodEndYear = periodEndYear,
        periodEndMonth = periodEndMonth,
        periodEndDay = periodEndDay,
        fileFormat = FileFormat.Pdf,
        fileRole = FileRole(fileRoleName),
        statementRequestId = statementRequestId)

      val result: Map[String, String] = metadata.toMap

      result mustEqual Map(
        "periodStartYear" -> s"$periodStartYear",
        "periodStartMonth" -> s"$periodStartMonth",
        "periodStartDay" -> s"$periodStartDay",
        "periodEndYear" -> s"$periodEndYear",
        "periodEndMonth" -> s"$periodEndMonth",
        "periodEndDay" -> s"$periodEndDay",
        "fileFormat" -> s"${FileFormat.Pdf}",
        "fileRole" -> s"${FileRole(fileRoleName)}",
        "statementRequestId" -> s"$statementRequestId")
    }
  }

  trait Setup {
    val periodStartYear = 2024
    val periodStartMonth = 10
    val periodStartDay = 1
    val periodEndYear = 2024
    val periodEndMonth = 10
    val periodEndDay = 31
    val fileRoleName = "CDSCashAccount"
    val statementRequestId: Option[String] = Some("requestId")
  }
}
