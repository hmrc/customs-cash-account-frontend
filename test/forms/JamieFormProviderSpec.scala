package forms

import models.JamieFormFields
import play.api.data.FormError
import utils.SpecBase
import utils.RegexPatterns.noDigitsRegex

import scala.collection.immutable.ArraySeq

class JamieFormProviderSpec extends SpecBase {
    "apply" should {
      "produce an error is no value is input" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> emptyString, "age" -> emptyString)

        form.bind(data).errors must contain (FormError("name", "error.required"))
        form.bind(data).errors must contain (FormError("age", "error.number"))
      }

      "produce an error when a name is entered but no age" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "Jamie", "age" -> emptyString)

        form.bind(data).errors mustBe Seq(FormError("age", List("error.number")))
      }

      "produce an error when a age is entered but no name" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> emptyString, "age" -> "30")

        form.bind(data).errors mustBe Seq(FormError("name", List("error.required")))
      }

      "produce an error when both values are numbers" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "28", "age" -> "38")

        form.bind(data).errors mustBe Seq(
          FormError("name", List("Enter your name"), ArraySeq(noDigitsRegex.regex)))
      }

      "produce an error when name is correct but number out of required range" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "Jamie", "age" -> "214")

        form.bind(data).errors mustBe Seq(FormError("age", List("Enter a number between 1 - 120")))
      }

      "produce an error when name has a number in it" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "J4mie", "age" -> "22")

        form.bind(data).errors mustBe Seq(
          FormError("name", List("Enter your name"), ArraySeq(noDigitsRegex.regex)))
      }

      "produce an error when both values are a lettered strings" in {
        val form = new JamieFormProvider()()
        val data = Map("name" -> "Jamie", "age" -> "Two")

        form.bind(data).errors mustBe Seq(FormError("age", List("error.number")))
      }

      "produce no error when both values are entered" in {
        val form = new JamieFormProvider()()

        val testName = "Jamie"
        val testAge = 28
        val data = Map("name" -> testName, "age" -> testAge.toString)

        form.bind(data).get == JamieFormFields(testName, testAge)
      }
    }
}
