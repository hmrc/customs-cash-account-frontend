/*
 * Copyright 2024 HM Revenue & Customs
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

package repositories

import models.CashTransactionDates
import play.api.Configuration
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.ExecutionContext.Implicits.global
import utils.SpecBase

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future


class DefaultRequestedTransactionsCacheSpec extends SpecBase  {

  "DefaultRequestedTransactionsCache" should {
    "set and get data correctly" in new Setup {

      val id = "testId"
      val data = CashTransactionDates(start = startTime, end = endTime)

      val result = for {
        _ <- cache.set(id, data)
        retrieved <- cache.get(id)
      } yield retrieved

      result mustBe Some(data)
    }
  }

  trait Setup {

    val mockMongo: MongoComponent = mock[MongoComponent]
    val mockConfig: Configuration = mock[Configuration]
    val cache = new DefaultRequestedTransactionsCache(mockMongo, mockConfig)

    def startTime: LocalDate = LocalDateTime.now().toLocalDate
    def endTime: LocalDate = LocalDateTime.now().toLocalDate

  }
}