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

package forms

import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}

object FormTestHelper extends Matchers {

  def error(key: String, value: String): Seq[FormError] = Seq(singleError(key, value))

  def singleError(key: String, value: String): FormError = FormError(key, value)

  def checkForError(form: Form[_], data: Map[String, String], expectedErrors: Seq[FormError]): Unit =
    form
      .bind(data)
      .fold(
        formWithErrors => formWithErrors.errors mustBe expectedErrors,
        _ => fail("Expected a validation error when binding the form, but it was bound successfully.")
      )
}
