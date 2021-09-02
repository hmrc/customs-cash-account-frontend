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


class CSVWritableSpec extends SpecBase {

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
    "generate a comma-separated string from a given case class" in {
      val foo = Foo(Some("A"), Some("B"), Some("C"))
      foo.toCSVRow must be(""""A","B","C"""")
    }

    "omit optional fields that are None" in {
      val foo = Foo(Some("A"), None, Some("C"))
      foo.toCSVRow must be(""""A",,"C"""")
    }

    "flatten nested records" in {
      val bar = Bar(Foo(Some("A"), None, Some("C")), Baz(Foo(None, Some("B"), None), Foo(Some("X"), Some("Y"), None)))
      bar.toCSVRow must be(""""A",,"C",,"B",,"X","Y",""")
    }

  }
}
