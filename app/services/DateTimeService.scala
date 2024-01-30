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

import config.AppConfig

import java.time._
import javax.inject.Inject

class DateTimeService @Inject()(appConfig: AppConfig) {

  private val defaultTimeZone = "Europe/London"
  def getTimeStamp: OffsetDateTime = OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)

  def systemDateTime(zoneId: ZoneId): LocalDateTime = {

    val year = 2027
    val month = 12
    val day = 20
    val hour = 12
    val minute = 30

    if (appConfig.fixedTimeTesting) {
      LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute))
    }
    else {
      LocalDateTime.now(zoneId)
    }
  }

  def utcDateTime(): LocalDateTime = {
    systemDateTime(ZoneId.of("UTC"))
  }

  def localDateTime(): LocalDateTime = {
    systemDateTime(ZoneId.of(defaultTimeZone))
  }
}
