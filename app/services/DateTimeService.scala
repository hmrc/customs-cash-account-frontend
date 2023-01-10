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

  def getTimeStamp: OffsetDateTime = OffsetDateTime.ofInstant( Instant.now() , ZoneOffset.UTC)

  def systemDateTime(zoneId: ZoneId): LocalDateTime = {

    if (appConfig.fixedTimeTesting) {
      LocalDateTime.of(LocalDate.of(2027, 12, 20), LocalTime.of(12,30)) // scalastyle:ignore
    }
    else {
      LocalDateTime.now(zoneId)
    }
  }

  def utcDateTime(): LocalDateTime = {
    systemDateTime(ZoneId.of("UTC"))
  }

  def localDateTime(): LocalDateTime = {
    systemDateTime(ZoneId.of("Europe/London"))
  }
}