package models

case class JamieFormFields(name: String, age: Int)

object JamieFormFields {
  def unapply(jamieFormFields: JamieFormFields): Option[(String, Int)] =
    Some((jamieFormFields.name, jamieFormFields.age))
}