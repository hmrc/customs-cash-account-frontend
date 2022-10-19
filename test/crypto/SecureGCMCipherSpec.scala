/*
 * Copyright 2022 HM Revenue & Customs
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

import utils.SpecBase

import java.util.Base64
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.{Cipher, IllegalBlockSizeException, KeyGenerator, NoSuchPaddingException}
import java.security.InvalidAlgorithmParameterException

class SecureGCMCipherSpec extends SpecBase
{
  private val encrypter      = new SecureGCMCipher
  private val secretKey      = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val secretKey2     = "cXo7u0HuJK8B/52xLwW7eQ=="
  private val textToEncrypt  = "textNotEncrypted"
  private val encryptedText  = EncryptedValue("sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
    "RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO")

  "encrypt" must {

    "must encrypt some text" in {
      val encryptedValue = encrypter.encrypt(textToEncrypt, secretKey)
      encryptedValue mustBe an[EncryptedValue]
    }
  }

  "decrypt" must {

    "must decrypt text when the same associatedText, nonce and secretKey were used to encrypt it" in {
      val decryptedText  = encrypter.decrypt(encryptedText, secretKey)
      decryptedText mustEqual textToEncrypt
    }

    "must return an EncryptionDecryptionException if the encrypted value is different" in {
      val invalidText = Base64.getEncoder.encodeToString("invalid value".getBytes)
      val invalidEncryptedValue = EncryptedValue(invalidText, encryptedText.nonce)

      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(invalidEncryptedValue, secretKey)
      )

      decryptAttempt.failureReason must include("Error occurred due to padding scheme")
    }

    "must return an EncryptionDecryptionException if the nonce is different" in {
      val invalidNonce = Base64.getEncoder.encodeToString("invalid value".getBytes)
      val invalidEncryptedValue = EncryptedValue(encryptedText.value, invalidNonce)

      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(invalidEncryptedValue, secretKey)
      )

      decryptAttempt.failureReason must include("Error occurred due to padding scheme")
    }

    "must return an EncryptionDecryptionException if the secret key is different" in {
      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(encryptedText, secretKey2)
      )

      decryptAttempt.failureReason must include("Error occurred due to padding scheme")
    }

    "must return an EncryptionDecryptionException if the key is empty" in {
      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(encryptedText, "")
      )

      decryptAttempt.failureReason must include("The key provided is invalid")
    }

    "must return an EncryptionDecryptionException if the key is invalid" in {
      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(encryptedText, "invalidKey")
      )

      decryptAttempt.failureReason must include("Key being used is not valid." +
        " It could be due to invalid encoding, wrong length or uninitialized")
    }

    "return an EncryptionDecryptionError if the secret key is an invalid type" in {

      val keyGen = KeyGenerator.getInstance("DES")
      val key = keyGen.generateKey()
      val secureGCMEncryter = new SecureGCMCipher {
        override val ALGORITHM_KEY: String = "DES"
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.generateCipherText(textToEncrypt,
          new GCMParameterSpec(96, "hjdfbhvbhvbvjvjfvb".getBytes), key)
      )

      encryptedAttempt.failureReason must include("Key being used is not valid." +
        " It could be due to invalid encoding, wrong length or uninitialized")
    }

    "return an EncryptionDecryptionError if the algorithm is invalid" in {
      val secureGCMEncryter = new SecureGCMCipher {
        override val ALGORITHM_TO_TRANSFORM_STRING: String = "invalid"
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, secretKey)
      )

      encryptedAttempt.failureReason must include("Algorithm being requested is not available in this environment")
    }

    "return an EncryptionDecryptionError if the padding is invalid" in {
      val secureGCMEncryter = new SecureGCMCipher {
        override def getCipherInstance: Cipher = throw new NoSuchPaddingException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, secretKey)
      )

      encryptedAttempt.failureReason must include("Padding Scheme being requested is not available this environment")
    }

    "return an EncryptionDecryptionError if an InvalidAlgorithmParameterException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipher {
        override def getCipherInstance: Cipher = throw new InvalidAlgorithmParameterException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, secretKey)
      )

      encryptedAttempt.failureReason must include("Algorithm parameters being specified are not valid")
    }

    "return an EncryptionDecryptionError if a IllegalStateException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipher {
        override def getCipherInstance: Cipher = throw new IllegalStateException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, secretKey)
      )

      encryptedAttempt.failureReason must include("Cipher is in an illegal state")
    }

    "return an EncryptionDecryptionError if a UnsupportedOperationException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipher {
        override def getCipherInstance: Cipher = throw new UnsupportedOperationException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, secretKey)
      )

      encryptedAttempt.failureReason must include("Provider might not be supporting this method")
    }

    "return an EncryptionDecryptionError if a IllegalBlockSizeException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipher{
        override def getCipherInstance: Cipher = throw new IllegalBlockSizeException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, secretKey)
      )

      encryptedAttempt.failureReason must include("Error occurred due to block size")
    }

    "return an EncryptionDecryptionError if a RuntimeException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipher {
        override def getCipherInstance: Cipher = throw new RuntimeException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, secretKey)
      )

      encryptedAttempt.failureReason must include("Unexpected exception")
    }
  }
}
