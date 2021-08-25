/*
 * Copyright 2021 HM Revenue & Customs
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

import java.security.{InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, SecureRandom}
import java.util.Base64

import javax.crypto._
import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}
import javax.inject.Inject
import play.api.libs.json.{Json, OFormat}

import scala.util.{Failure, Success, Try}

case class EncryptedValue(value: String, nonce: String)

object EncryptedValue {
  implicit lazy val format: OFormat[EncryptedValue] = Json.format[EncryptedValue]
}

case class EncryptionDecryptionException(method: String, reason: String, message: String) extends RuntimeException {
  val failureReason = s"$reason for $method"
  val failureMessage: String = message
}

class SecureGCMCipher @Inject()() {

  val IV_SIZE = 96
  val TAG_BIT_LENGTH = 128
  val ALGORITHM_TO_TRANSFORM_STRING = "AES/GCM/PKCS5Padding"
  lazy val secureRandom = new SecureRandom()
  val ALGORITHM_KEY = "AES"
  val METHOD_ENCRYPT = "encrypt"
  val METHOD_DECRYPT = "decrypt"

  def encrypt(valueToEncrypt: String, aesKey: String): EncryptedValue = {

    val initialisationVector = generateInitialisationVector
    val nonce = new String(Base64.getEncoder.encode(initialisationVector))
    val gcmParameterSpec = new GCMParameterSpec(TAG_BIT_LENGTH, initialisationVector)
    val secretKey = validateSecretKey(aesKey, METHOD_ENCRYPT)
    val cipherText = generateCipherText(valueToEncrypt, gcmParameterSpec, secretKey)

    EncryptedValue(cipherText, nonce)
  }

  def decrypt(valueToDecrypt: EncryptedValue, aesKey: String): String = {

    val initialisationVector = Base64.getDecoder.decode(valueToDecrypt.nonce)
    val gcmParameterSpec = new GCMParameterSpec(TAG_BIT_LENGTH, initialisationVector)
    val secretKey = validateSecretKey(aesKey, METHOD_DECRYPT)

    decryptCipherText(valueToDecrypt.value, gcmParameterSpec, secretKey)
  }

  private def generateInitialisationVector: Array[Byte] = {
    val iv = new Array[Byte](IV_SIZE)
    secureRandom.nextBytes(iv)
    iv
  }

  private def validateSecretKey(key: String, method: String): SecretKey = Try {
    val decodedKey = Base64.getDecoder.decode(key)
    new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM_KEY)
  } match {
    case Success(secretKey) => secretKey
    case Failure(ex) => throw new EncryptionDecryptionException(method, "The key provided is invalid", ex.getMessage)
  }

  def generateCipherText(valueToEncrypt: String, gcmParameterSpec: GCMParameterSpec, secretKey: SecretKey): String = {
    Try {
      val cipher = getCipherInstance
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec, new SecureRandom())
      cipher.doFinal(valueToEncrypt.getBytes)
    } match {
      case Success(cipherTextBytes) => new String(Base64.getEncoder.encode(cipherTextBytes))
      case Failure(ex) => throw processCipherTextFailure(ex, METHOD_ENCRYPT)
    }
  }

  def decryptCipherText(valueToDecrypt: String, gcmParameterSpec: GCMParameterSpec, secretKey: SecretKey): String = {
    Try {
      val cipher = getCipherInstance
      cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec, new SecureRandom())
      cipher.doFinal(Base64.getDecoder.decode(valueToDecrypt))
    } match {
      case Success(value) => new String(value)
      case Failure(ex) => throw processCipherTextFailure(ex, METHOD_DECRYPT)
    }
  }

  private[crypto] def getCipherInstance: Cipher = Cipher.getInstance(ALGORITHM_TO_TRANSFORM_STRING)


  private def processCipherTextFailure(ex: Throwable, method: String): Throwable = {
    val exception = ex match {
      case e: NoSuchAlgorithmException => EncryptionDecryptionException(method, "Algorithm being requested is not available in this environment",
        e.getMessage)
      case e: NoSuchPaddingException => EncryptionDecryptionException(method, "Padding Scheme being requested is not available this environment",
        e.getMessage)
      case e: InvalidKeyException => EncryptionDecryptionException(method, "Key being used is not valid." +
        " It could be due to invalid encoding, wrong length or uninitialized", e.getMessage)
      case e: InvalidAlgorithmParameterException => EncryptionDecryptionException(method, "Algorithm parameters being specified are not valid",
        e.getMessage)
      case e: IllegalStateException => EncryptionDecryptionException(method, "Cipher is in an illegal state", e.getMessage)
      case e: UnsupportedOperationException => EncryptionDecryptionException(method, "Provider might not be supporting this method", e.getMessage)
      case e: IllegalBlockSizeException => EncryptionDecryptionException(method, "Error occurred due to block size", e.getMessage)
      case e: BadPaddingException => EncryptionDecryptionException(method, "Error occurred due to padding scheme", e.getMessage)
      case _ => EncryptionDecryptionException(method, "Unexpected exception", ex.getMessage)
    }
    throw exception
  }
}