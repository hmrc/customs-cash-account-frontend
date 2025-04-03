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
import uk.gov.hmrc.crypto.Crypted
import utils.SpecBase
import java.util.Base64

class CryptoSpec extends SpecBase {

  "Crypto" should {
    "encrypt and decrypt a string correctly" in new Setup {
      val encrypted: Crypted = crypto.encrypt(plainText)
      val decrypted: String  = crypto.decrypt(encrypted)

      decrypted mustBe plainText
    }
  }

  trait Setup {
    private val KeyLength: Int        = 32
    private val FillByte: Byte        = 1.toByte
    private val TestPlainText: String = "test-string"

    private val encryptionKeyBytes: Array[Byte] = Array.fill(KeyLength)(FillByte)
    private val encryptionKey: String           = Base64.getEncoder.encodeToString(encryptionKeyBytes)

    val config: Configuration = Configuration.from(Map("mongodb.encryptionKey" -> encryptionKey))
    val crypto: Crypto        = new Crypto(config)
    val plainText: String     = TestPlainText
  }
}
