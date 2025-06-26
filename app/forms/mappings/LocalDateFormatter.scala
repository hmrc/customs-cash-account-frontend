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

import java.time.LocalDate
import scala.util.{Success, Try}

private[mappings] class LocalDateFormatter(
  emptyStartMonth: String,
  emptyStartYear: String,
  emptyEndMonth: String,
  emptyEndYear: String,
  emptyStartDate: String,
  emptyEndDate: String,
  invalidMonth: String,
  invalidYear: String,
  invalidDate: String,
  args: Seq[String] = Seq.empty
) extends Formatter[LocalDate] {

  private val fieldKeys: List[String] = List("month", "year")

  private def toDate(key: String, month: Option[String], year: Option[String]): Either[Seq[FormError], LocalDate] =
    (validMonth(month), validYear(year)) match {
      case (Some(_), None)                     => Left(Seq(FormError(s"$key.year", invalidYear, args)))
      case (None, Some(_))                     => Left(Seq(FormError(s"$key.month", invalidMonth, args)))
      case (None, None)                        => Left(Seq(FormError(s"$key", invalidDate, args)))
      case (Some(validMonth), Some(validYear)) => Right(LocalDate.of(validYear, validMonth, 1))
    }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    val month = data.get(s"$key.month")
    val year  = data.get(s"$key.year")
    toDate(key, month, year)
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 2 => formatDate(key, data)
      case 1 =>
        (key, missingFields.head) match {
          case ("start", "month") =>
            Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyStartMonth, args)))

          case ("start", "year") =>
            Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyStartYear, args)))

          case ("end", "month") =>
            Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyEndMonth, args)))

          case ("end", "year") =>
            Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyEndYear, args)))

          case _ => Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), "Unknown", args)))
        }
      case _ =>
        (key, missingFields) match {
          case ("start", List("month", "year")) =>
            Left(List(FormError(key, emptyStartDate, args)))

          case ("end", List("month", "year")) =>
            Left(List(FormError(key, emptyEndDate, args)))

          case _ => Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), "Unknown", args)))
        }
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )

  private[mappings] def formErrorKeysInCaseOfEmptyOrNonNumericValues(key: String, data: Map[String, String]): String = {
    val monthValue = data.get(s"$key.month")
    val yearValue  = data.get(s"$key.year")

    (monthValue, yearValue) match {
      case (Some(m), _) if m.trim.isEmpty || Try(m.trim.toInt).isFailure => s"$key.month"
      case (_, Some(y)) if y.trim.isEmpty || Try(y.trim.toInt).isFailure => s"$key.year"
      case _                                                             => s"$key.month"
    }
  }

  private def validMonth(month: Option[String]): Option[Int] =
    Try(month.get.trim.toInt) match {
      case Success(value) if value > 0 && value < 13 => Some(value)
      case _                                         => None
    }

  private def validYear(year: Option[String]): Option[Int] =
    Try(year.get.trim.toInt) match {
      case Success(value) if value.toString.matches("^\\d{4}$") => Some(value)
      case _                                                    => None
    }
}
