package models

import org.scalatest.matchers.should.Matchers.{should, shouldBe, shouldEqual}
import utils.SpecBase

class JamieFormFieldsSpec extends SpecBase {
  "JamieFormFields" should {
    "correctly unapply" in new Setup {
      jamieForm match {
        case JamieFormFields(name, age) =>
          name shouldEqual "Jamie"
          age shouldEqual 41
        case _ => fail("Pattern did not match")
      }
    }
    "return a Option containing name and age" in new Setup {
      val result: Option[(String, Int)] = JamieFormFields.unapply(jamieForm)
      result shouldBe Some(("Jamie", 41))
    }
    
    "should not return a None for valid instances" in new Setup {
      JamieFormFields.unapply(jamieForm) should not be None
    }
  }

  trait Setup {
    val string: String = "Jamie"
    val number: Int = 41
    val jamieForm: JamieFormFields = JamieFormFields(string, number)
  }
}
