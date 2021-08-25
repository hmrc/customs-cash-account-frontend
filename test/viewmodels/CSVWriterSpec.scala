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

import utils.SpecBase


class CSVWriterSpec extends SpecBase {

  import CSVWriter._

  case class Foo(columnA: Option[String], columnB: Option[String], columnC: Option[String])
  case class Bar(foo: Foo, baz: Baz)
  case class Baz(foo1: Foo, foo2: Foo)

  "CSVWriter" should {
    "generate a comma-separated string from a given case class" in {
      val foo = Foo(Some("A"), Some("B"), Some("C"))
      foo.toCSV must be (""""A","B","C"""")
    }

    "omit optional fields that are None" in {
      val foo = Foo(Some("A"), None, Some("C"))
      foo.toCSV must be (""""A",,"C"""")
    }

    "flatten nested records" in {
      val bar = Bar(Foo(Some("A"), None, Some("C")), Baz(Foo(None, Some("B"), None), Foo(Some("X"), Some("Y"), None)))
      bar.toCSV must be (""""A",,"C",,"B",,"X","Y",""")
    }

    "generate carriage-return-separated records from a collection" in {
      val records = Seq(
        Foo(Some("A"), Some("B"), Some("C")),
        Foo(Some("X"), Some("Y"), Some("Z"))
      )
      records.toCSV must be (""""A","B","C"
          |"X","Y","Z"""".stripMargin)
    }

    "prepend header row with case class field names if requested" in {
      val records = Seq(
        Foo(Some("A"), Some("B"), Some("C")),
        Foo(Some("X"), Some("Y"), Some("Z"))
      )
      records.toCSVWithHeaders(mappingFn = identity, footer = None) must be (""""columnA","columnB","columnC"
          |"A","B","C"
          |"X","Y","Z"""".stripMargin)
    }

    "map header names using supplied function" in {
      val mappingFn: String => String = s => s.toUpperCase()

      val records = Seq(
        Foo(Some("a"), Some("b"), Some("c"))
      )
      records.toCSVWithHeaders(mappingFn, None) must be (""""COLUMNA","COLUMNB","COLUMNC"
          |"a","b","c"""".stripMargin)
    }

    "append footer row with text" in {
      val records = Seq(
        Foo(Some("A"), Some("B"), Some("C")),
        Foo(Some("X"), Some("Y"), Some("Z"))
      )
      records.toCSVWithHeaders(mappingFn = identity, footer = Some("footer text")) must be (""""columnA","columnB","columnC"
                                            |"A","B","C"
                                            |"X","Y","Z"
                                            |
                                            |
                                            |"footer text"
                                            |""".stripMargin)
    }
  }
}
