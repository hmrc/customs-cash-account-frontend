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

package helpers

import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import services.DateTimeService
import utils.SpecBase
import java.time.{LocalDate, LocalDateTime}

class CashAccountUtilsSpec extends SpecBase {
  val mockDateTimeService = mock[DateTimeService]
  when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-19T09:30:59"))

  val app = application
    .overrides(
      bind[DateTimeService].toInstance(mockDateTimeService)
    )
    .build()
  implicit val messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
  val cashAccountUtils = app.injector.instanceOf[CashAccountUtils]

  "filenameWithDateTime" should {
    "return a correctly formatted filename" in {
      val result = cashAccountUtils.filenameWithDateTime()(messages)
      result must be("Cash_Account_Transactions_20200419093059.CSV")
    }
  }

  "filenameRequestCashTransactions" should {
    "return a correctly formatted filename for the given dates" in {
      val fromDate = LocalDate.parse("2020-11-06")
      val toDate = LocalDate.parse("2020-12-12")
      val result = cashAccountUtils.filenameRequestCashTransactions(fromDate, toDate)(messages)
      result must be("Cash_Account_Transactions_06112020-12122020.CSV")
    }
  }
}
