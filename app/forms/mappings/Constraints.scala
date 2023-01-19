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

package forms.mappings

import play.api.{Logger, LoggerLike}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.time.TaxYear
import uk.gov.hmrc.time.TaxYear.taxYearFor

import java.time.{Clock, LocalDate, LocalDateTime, Period}

trait Constraints {

  lazy val etmpStatementsDate: LocalDate = LocalDate.of(2019, 10, 1)
  lazy val currentDate: LocalDate = LocalDateTime.now().toLocalDate
  lazy val dayOfMonthThatTaxYearStartsOn = 6
  val log: LoggerLike = Logger(this.getClass)

  def beforeCurrentDate(errorKey:String): Constraint[LocalDate] = Constraint {
    case request if request.isAfter(currentDate)  =>
      log.info("entered date in constraints: "+request)
      log.info("current date in constraints: "+currentDate)
      Invalid(ValidationError(errorKey))
    case _ => Valid
  }

  def beforeCurrentMonth(errorKey:String): Constraint[LocalDate] = Constraint {
    case request if request.getMonthValue > (currentDate.getMonthValue)  =>
      Invalid(ValidationError(errorKey))
    case _ => Valid
  }

  def minTaxYear()(implicit clock:Clock): TaxYear = {
    lazy val currentDate: LocalDate = LocalDateTime.now(clock).toLocalDate
    val maximumNumberOfYears = 6
    taxYearFor(currentDate).back(maximumNumberOfYears)
  }

  def checkDates(systemStartDateErrorKey:String, taxYearErrorKey:String)(implicit clock: Clock): Constraint[LocalDate] = Constraint {
    case request if Period.between(request, etmpStatementsDate).toTotalMonths > 0 =>
      Invalid(ValidationError(systemStartDateErrorKey))
    case request if minTaxYear.starts.isAfter(request.withDayOfMonth(dayOfMonthThatTaxYearStartsOn))  =>
      Invalid(ValidationError(taxYearErrorKey))
    case _ => Valid
  }
}
