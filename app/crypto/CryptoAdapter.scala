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

class CryptoAdapter @Inject() (config: Configuration, deprecatedAesGCMCrypto: AesGCMCrypto) {

  private val encryptionKey = config.get[String]("mongodb.encryptionKey")
  private val crypto        = SymmetricCryptoFactory.aesGcmCrypto(encryptionKey)

  def encrypt(plainText: String): Either[EncryptedValue, Crypted] =
    Right(crypto.encrypt(PlainText(plainText)))

  def decrypt(encryptedData: Either[EncryptedValue, Crypted]): String = encryptedData match {
    case Left(legacy)   =>
      deprecatedAesGCMCrypto.decrypt(legacy, encryptionKey)
    case Right(crypted) =>
      crypto.decrypt(crypted).value
  }
}

object CryptoAdapterFormats {

  implicit val cryptedformat: OFormat[Crypted] = Json.format[Crypted]

  implicit val eitherFormat: OFormat[Either[EncryptedValue, Crypted]] = new OFormat[Either[EncryptedValue, Crypted]] {
    override def writes(either: Either[EncryptedValue, Crypted]): JsObject = either match {
      case Right(crypted) => cryptedformat.writes(crypted)
      case Left(_)        => throw new IllegalStateException("Legacy format should never be written")
    }

    override def reads(json: JsValue): JsResult[Either[EncryptedValue, Crypted]] =
      json.validate[EncryptedValue] match {
        case JsSuccess(encryptedValue, _) => JsSuccess(Left(encryptedValue))
        case JsError(_)           =>
          json.validate[Crypted] match {
            case JsSuccess(crypted, _) => JsSuccess(Right(crypted))
            case JsError(_)                    => JsError("Invalid encrypted format")
          }
      }
  }
}
