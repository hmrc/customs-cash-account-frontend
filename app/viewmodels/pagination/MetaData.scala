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

package viewmodels.pagination

case class MetaData(from: Int, to: Int, count: Int, currentPage: Int, totalPages: Int)

object MetaData {

  def apply(totalNumberOfMovements: Int, numberOfMovementsPerPage: Int, currentPage: Int): MetaData = {

    val totalNumberOfPages: Int = Math.ceil(totalNumberOfMovements.toDouble / numberOfMovementsPerPage).toInt
    val from: Int               = numberOfMovementsPerPage * (currentPage - 1) + 1

    val noOfMovementsWhenCurrentPageIsOtherThanOne: Int = {
      val roundedNumberOfMovementsPerPage = numberOfMovementsPerPage * currentPage

      if (roundedNumberOfMovementsPerPage > totalNumberOfMovements) {
        totalNumberOfMovements
      } else {
        roundedNumberOfMovementsPerPage
      }
    }

    val to: Int = if (currentPage == 1) {
      numberOfMovementsPerPage
    } else {
      noOfMovementsWhenCurrentPageIsOtherThanOne
    }

    MetaData(from, to, totalNumberOfMovements, currentPage, totalNumberOfPages)
  }
}
