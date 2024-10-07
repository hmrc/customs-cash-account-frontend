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

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.{Logger, LoggerLike}

import java.time.{LocalDate, LocalDateTime, YearMonth}
import scala.util.{Failure, Success, Try}

private[mappings] class SelectLocalDateFormatter(invalidKey: String,
                                                 monthKey: String,
                                                 yearKey: String,
                                                 invalidDateKey: String,
                                                 args: Seq[String],
                                                 useLastDayOfMonth: Boolean) extends Formatter[LocalDate] with Formatters {

  private val fieldKeys: List[String] = List("month", "year")
  val log: LoggerLike = Logger(this.getClass)
  val currentDate: LocalDate = LocalDateTime.now().toLocalDate

  private def toDate(key: String,
                   month: Int,
                   year: Int): Either[Seq[FormError], LocalDate] = {
    
    if (month < 1 || month > 12) {
      Left(Seq(FormError(s"$key.month", invalidDateKey, args)))
    } else {
      val day = if (useLastDayOfMonth) YearMonth.of(year, month).lengthOfMonth() else 1
      Try(LocalDate.of(year, month, day)) match {
        case Success(date) => Right(date)
        case Failure(_) =>
          Left(
            Seq(
              FormError(
                updateFormErrorKeys(key, month, year),
                invalidDateKey,
                args
              )
            )
          )
      }
    }
  }

  private def formatDate(key: String,
                         data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      month <- int.bind(s"$key.month", data)
      year <- int.bind(s"$key.year", data)
      date <- toDate(key, month, year)
    } yield {
      date
    }
  }

  override def bind(key: String,
                    data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val fields: Map[String, Option[String]] = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    fields.count(_._2.isDefined) match {
      case 2 | 3 => checkForFieldValues(key, data)
      case _ =>
        Left(
          List(
            FormError(
              formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data),
              invalidKey,
              args
            )
          )
        )
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )

  private[mappings] def updateFormErrorKeys(key: String,
                                            month: Int,
                                            year: Int): String =
    (month, year) match {
      case (m, _) if m < 1 || m > 12 => s"$key.month"
      case (_, y) if y < 1000 || y > 99999 => s"$key.year"
      case _ => s"$key.month"
    }

  private def checkForFieldValues(key: String,
                                  data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    data match {

      case value
        if value.contains(s"$key.month") && value(s"$key.month").isEmpty =>
        populateErrorMsg(key, data, monthKey)

      case value
        if value.contains(s"$key.year") && data(s"$key.year").isEmpty =>
        populateErrorMsg(key, data, yearKey)

      case _ =>
        formatDate(key, data).left.map {
          _.map(fe => fe.copy(key = fe.key, args = args))
        }
    }
  }

  private def populateErrorMsg(key: String,
                               data: Map[String, String],
                               errorMsg: String): Left[List[FormError], Nothing] = {
    Left(
      List(
        FormError(
          formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data),
          errorMsg,
          args
        )
      )
    )
  }

  private[mappings] def formErrorKeysInCaseOfEmptyOrNonNumericValues(key: String,
                                                                     data: Map[String, String]): String = {
    val monthValue = data.get(s"$key.month")
    val yearValue = data.get(s"$key.year")

    (monthValue, yearValue) match {
      case (Some(m), _) if m.trim.isEmpty || Try(m.trim.toInt).isFailure => s"$key.month"
      case (_, Some(y)) if y.trim.isEmpty || Try(y.trim.toInt).isFailure => s"$key.year"
      case _ => s"$key.month"
    }
  }
}
