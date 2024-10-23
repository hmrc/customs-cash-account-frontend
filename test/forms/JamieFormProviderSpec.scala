package forms

import play.api.data.FormError
import utils.SpecBase

class JamieFormProviderSpec extends SpecBase {
    "apply" should {
      "produce an error is no value is input" in {
        val formProvider = new JamieFormProvider()
        val form = formProvider.apply()
        val data = Map("name" -> emptyString)

        val boundForm = form.bind(data)

        boundForm.errors must contain (FormError("name", "error.required"))
      }

      "produce an error a name is entered but no number" in {
        ???
      }

      "produce an error when a number is entered but no string" in {
        ???
      }

      "produce an error when both values are numbers" in {
        ???
      }

      "produce an error when both values are string" in {

      }

      "produce no error when both values are entered" in {
        val formProvider = new JamieFormProvider()
        val form = formProvider.apply()
        val data = Map("name" -> "Hello")

        val boundForm = form.bind(data)

        boundForm.get mustBe "Hello"
      }
    }
}
