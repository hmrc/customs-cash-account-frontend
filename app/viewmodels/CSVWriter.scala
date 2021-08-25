/*
 * Copyright 2021 HM Revenue & Customs
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

  implicit class Product2CSV(val caseClass: Product) extends AnyVal {

    def toCSV: String = caseClass.productIterator.map{
      case nestedProduct: Product => nestedProduct.toCSV
      case string: String => quote(string)
      case rest => rest
    }.mkString(",")

  }

  implicit class Seq2CSV(val sequence: Seq[Product]) extends AnyVal {

    def toCSVWithHeaders(mappingFn: String => String, footer: Option[String]): String = {
      val mapAndQuote = mappingFn andThen quote
      val headers = sequence.headOption.map(fieldNames(_).map(mapAndQuote).mkString(",") + "\n").getOrElse("")
      val formattedFooter = footer.fold("")(text => s"""\n\n\n"$text"\n""")
      headers + sequence.toCSV + formattedFooter
    }

    def toCSV: String = sequence.map(_.toCSV).mkString("\n")

    private def fieldNames(caseClass: Product):Array[String]= {
      caseClass.getClass.getDeclaredFields.map(_.getName).filterNot(_ == "$outer")
    }

  }

}

