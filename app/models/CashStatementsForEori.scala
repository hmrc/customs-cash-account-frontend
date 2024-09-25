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

trait OrderedByEoriHistory[T <: OrderedByEoriHistory[_]] extends Ordered[T] {
  val eoriHistory: EoriHistory

  override def compare(that: T): Int = {
    (for {
      thatValidFrom <- that.eoriHistory.validFrom
      thisValidFrom <- this.eoriHistory.validFrom
    } yield {
      thatValidFrom.compareTo(thisValidFrom)
    }).getOrElse(1)
  }
}

case class CashStatementsForEori(eoriHistory: EoriHistory,
                                 currentStatements: Seq[CashStatementsByMonth],
                                 requestedStatements: Seq[CashStatementsByMonth]
                                ) extends OrderedByEoriHistory[CashStatementsForEori]
