/*
 * Copyright 2025 HM Revenue & Customs
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

package crypto

import play.api.Configuration
import play.api.libs.json.*
import uk.gov.hmrc.crypto.{Crypted, PlainText, SymmetricCryptoFactory}

import javax.inject.Inject

class Crypto @Inject() (config: Configuration) {

  private val encryptionKey = config.get[String]("mongodb.encryptionKey")
  private val crypto        = SymmetricCryptoFactory.aesGcmCrypto(encryptionKey)

  def encrypt(plainText: String): Crypted     = crypto.encrypt(PlainText(plainText))
  def decrypt(encryptedData: Crypted): String = crypto.decrypt(encryptedData).value
}

object Crypted {
  implicit val format: OFormat[Crypted] = Json.format[Crypted]
}
