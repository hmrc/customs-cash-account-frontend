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

import java.time.YearMonth
import scala.util.Try

private[mappings] class SelectYearMonthFormatter(emptyMonthAndYearKey: String,
                                                 emptyMonthKey: String,
                                                 emptyYearKey: String,
                                                 invalidDateKey: String,
                                                 args: Seq[String]) extends Formatter[YearMonth] with Formatters {

  val log: LoggerLike = Logger(this.getClass)

  private val fieldKeys: List[String] = List("month", "year")
  private val monthValueMin = 1
  private val monthValueMax = 12
  private val yearValueMin = 1000
  private val yearValueMax = 99999

  override def bind(key: String,
                    data: Map[String, String]): Either[Seq[FormError], YearMonth] = {

    val fields: Map[String, Option[String]] = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    if (fields.count(_._2.isDefined) == 0) {
      Left(
        List(
          FormError(
            formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data),
            emptyMonthAndYearKey,
            args
          )
        )
      )
    } else {
      checkForFieldValues(key, data)
    }
  }

  override def unbind(key: String, value: YearMonth): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )

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

  private[mappings] def updateFormErrorKeys(key: String,
                                            month: Int,
                                            year: Int): String =
    (month, year) match {
      case (m, _) if m < monthValueMin || m > monthValueMax => s"$key.month"
      case (_, y) if y < yearValueMin || y > yearValueMax => s"$key.year"
      case _ => s"$key.month"
    }

  private def checkForFieldValues(key: String,
                                  data: Map[String, String]): Either[Seq[FormError], YearMonth] = {
    data match {
      case value if isMonthEmpty(key, value) => populateErrorMsg(key, data, emptyMonthKey)
      case value if isYearEmpty(key, value) => populateErrorMsg(key, data, emptyYearKey)
      case _ => createDateOrGenerateFormError(key, data)
    }
  }

  private def formatDate(key: String,
                         data: Map[String, String]): Either[Seq[FormError], YearMonth] = {

    val int = intFormatter(
      requiredKey = invalidDateKey,
      wholeNumberKey = invalidDateKey,
      nonNumericKey = invalidDateKey,
      args
    )

    for {
      month <- int.bind(s"$key.month", data)
      year <- int.bind(s"$key.year", data)
      yearMonth <- toYearMonth(key, month, year)
    } yield {
      yearMonth
    }
  }

  private def toYearMonth(key: String,
                     month: Int,
                     year: Int): Either[Seq[FormError], YearMonth] = {

    if (month < monthValueMin || month > monthValueMax) {
      Left(Seq(FormError(s"$key.month", invalidDateKey, args)))
    } else {
      Right(YearMonth.of(year, month))
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

  private def isMonthEmpty(key: String, value: Map[String, String]) = {
    value.contains(s"$key.month") && value(s"$key.month").isEmpty
  }

  private def isYearEmpty(key: String, value: Map[String, String]) = {
    value.contains(s"$key.year") && value(s"$key.year").isEmpty
  }

  private def createDateOrGenerateFormError(key: String, data: Map[String, String]) = {
    formatDate(key, data).left.map {
      _.map(fe => fe.copy(key = fe.key, args = args))
    }
  }
}
