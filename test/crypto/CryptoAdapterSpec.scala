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

import play.api.inject
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.*
import play.api.libs.json.*
import uk.gov.hmrc.crypto.Crypted
import utils.SpecBase

class CryptoAdapterSpec extends SpecBase {

  "Crypto Adapter" should {

    "encrypt a plaintext string" in new Setup {
      val result = adapter.encrypt(plainText)

      result.isRight mustBe true
    }

    "decrypt a Right(Crypted(...)) with the new approach" in new Setup {
      val crypted   = adapter.encrypt(plainText)
      val decrypted = adapter.decrypt(crypted)

      decrypted mustBe plainText
      verify(mockAesGCM, never()).decrypt(any(), any())
    }

    "decrypt a Left(EncryptedValue(...)) with the deprecated approach" in new Setup {
      val legacyValue = EncryptedValue(cipherText, nonce)
      when(mockAesGCM.decrypt(eqTo(legacyValue), any())).thenReturn(plainText)

      val decrypted = adapter.decrypt(Left(legacyValue))

      decrypted mustBe plainText
      verify(mockAesGCM).decrypt(eqTo(legacyValue), any())
    }
  }

  "CryptoAdapterFormats" should {
    import crypto.CryptoAdapterFormats.*

    "read legacy EncryptedValue as Left(...)" in new Setup {
      val json   = Json.parse(s"""{"value":"$cipherText","nonce":"$nonce"}""")
      val result = json.validate[Either[EncryptedValue, Crypted]](eitherFormat)

      result.isSuccess mustBe true
      result.get.swap.toOption.get.value mustBe cipherText
      result.get.swap.toOption.get.nonce mustBe nonce
    }

    "read Crypted as Right(..)" in new Setup {
      val json   = Json.parse(s"""{"value":"$cipherText"}""")
      val result = json.validate[Either[EncryptedValue, Crypted]](eitherFormat)

      result.isSuccess mustBe true
      result.get.toOption.get.value mustBe cipherText
    }

    "fail to read invalid JSON" in {
      val json   = Json.parse(s"""{"invalid":"json"}""")
      val result = json.validate[Either[EncryptedValue, Crypted]](eitherFormat)

      result.isError mustBe true
    }

    "write a Right(Crypted(...)) as JSON" in new Setup {
      val crypted = Crypted(cipherText)
      val json    = Json.toJson(Right(crypted))(eitherFormat)

      (json \ "value").as[String] mustBe cipherText
    }

    "throw exception when writing Left(EncryptedValue(...))" in new Setup {
      val leftValue = Left(EncryptedValue(cipherText, nonce))

      an[IllegalStateException] mustBe thrownBy {
        Json.toJson(leftValue)(eitherFormat)
      }
    }
  }

  trait Setup {
    protected val mockAesGCM: AesGCMCrypto = mock[AesGCMCrypto]

    private val app = applicationBuilder
      .overrides(
        Seq(
          inject.bind[AesGCMCrypto].to(mockAesGCM)
        )
      )
      .build()

    protected val adapter: CryptoAdapter = app.injector.instanceOf[CryptoAdapter]

    protected val plainText  = "Plain text"
    protected val cipherText = "234564564576564376537"
    protected val nonce      = "567567"
  }
}
