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

package models

import play.api.mvc.QueryStringBindable

import scala.util.{Failure, Success, Try}
import java.time.LocalDate

case class RequestedDateRange(from: LocalDate, to: LocalDate)
object RequestedDateRange {
  implicit val binder = new RequestedDateRangeQueryStringBindable
}

class RequestedDateRangeQueryStringBindable extends QueryStringBindable[RequestedDateRange] {
  override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, RequestedDateRange]] = {
    (getParam(params, "from"), getParam(params, "to")) match {
      case (Right(from), Right(to)) => Some(Right(RequestedDateRange(from, to)))
      case (_, Left(msg)) => Some(Left(msg))
      case (Left(msg), _) => Some(Left(msg))
    }
  }

  override def unbind(key: String, value: RequestedDateRange): String = {
    s"from=${value.from.toString}&to=${value.to.toString}"
  }

  private def getParam(params: Map[String, Seq[String]], paramName: String): Either[String, LocalDate] = {
    params.get(paramName).flatMap(_.headOption) match {
      case Some(value) => Try(LocalDate.parse(value)) match {
        case Failure(_) => Left(s"$paramName invalid date format")
        case Success(value) => Right(value)
      }
      case None => Left(s"$paramName is required")
    }
  }
}
