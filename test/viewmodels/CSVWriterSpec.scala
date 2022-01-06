/*
 * Copyright 2022 HM Revenue & Customs
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

import utils.SpecBase


class CSVWriterSpec extends SpecBase {

  case class Foo(columnA: Option[String], columnB: Option[String], columnC: Option[String]) extends CSVWritable with FieldNames {
    override def fieldNames: Seq[String] = Seq("columnA", "columnB", "columnC")
  }

  case class Bar(foo: Foo, baz: Baz) extends CSVWritable with FieldNames {
    override def fieldNames: Seq[String] = Seq("foo", "baz")
  }

  case class Baz(foo1: Foo, foo2: Foo) extends CSVWritable with FieldNames {
    override def fieldNames: Seq[String] = Seq("foo1", "foo2")
  }

  "CSVWriter" should {
    "prepend header row with case class field names if requested" in {
      val records = Seq(
        Foo(Some("A"), Some("B"), Some("C")),
        Foo(Some("X"), Some("Y"), Some("Z"))
      )
      CSVWriter.toCSVWithHeaders(records) must be(
        """"columnA","columnB","columnC"
          |"A","B","C"
          |"X","Y","Z"""".stripMargin)
    }

    "map header names using supplied function" in {
      val mappingFn: String => String = s => s.toUpperCase()

      val records = Seq(
        Foo(Some("a"), Some("b"), Some("c"))
      )
      CSVWriter.toCSVWithHeaders(records, mappingFn) must be(
        """"COLUMNA","COLUMNB","COLUMNC"
          |"a","b","c"""".stripMargin)
    }

    "append footer row with text" in {
      val records = Seq(
        Foo(Some("A"), Some("B"), Some("C")),
        Foo(Some("X"), Some("Y"), Some("Z"))
      )
      CSVWriter.toCSVWithHeaders(records, footer = Some("footer text")) must be(
        """"columnA","columnB","columnC"
          |"A","B","C"
          |"X","Y","Z"
          |
          |
          |"footer text"
          |""".stripMargin)
    }
  }
}
