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

package viewmodels

object CSVWriter {

  private def quote(string: String): String = s""""$string""""

  def toCSVWithHeaders(rows: Seq[CSVWritable with FieldNames], mappingFn: String => String = identity, footer: Option[String] = None): String = {
    val mapAndQuote: String => String = mappingFn andThen quote
    val headers: String = rows.headOption.map(_.fieldNames.map(mapAndQuote).mkString(",") + "\n").getOrElse("")
    val formattedFooter: String = footer.fold("")(text => s"""\n\n\n"$text"\n""")

    headers + rows.map(_.toCSVRow).mkString("\n") + formattedFooter
  }

}

