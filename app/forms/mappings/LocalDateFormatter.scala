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
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
                                            invalidKey: String,
                                            endOfMonth: Boolean,
                                            args: Seq[String]
                                          ) extends Formatter[LocalDate] with Formatters {

  private val fieldKeys: List[String] = List("month", "year")

  private def toDate(key: String, month: Int, year: Int): Either[Seq[FormError], LocalDate] = {
    Try(LocalDate.of(year, month, 1)) match {
      case Success(date) =>
        Right(LocalDate.of(year, month, if (endOfMonth) date.lengthOfMonth() else 1))
      case Failure(_) =>
        Left(Seq(FormError(key, invalidKey, args)))
    }
  }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      month <- int.bind(s"$key.month", data).right
      year <- int.bind(s"$key.year", data).right
      date <- toDate(key, month, year).right
    } yield date
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map {
      field =>
        field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    fields.count(_._2.isDefined) match {
      case 2 =>
        formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case _ =>
        Left(List(FormError(key, invalidKey, args)))
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )
}
