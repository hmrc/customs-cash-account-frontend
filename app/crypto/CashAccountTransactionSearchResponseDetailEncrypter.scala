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

package crypto

import models.*
import models.response.CashAccountTransactionSearchResponseDetail
import play.api.libs.json.Json
import javax.inject.Inject

class CashAccountTransactionSearchResponseDetailEncrypter @Inject()(crypto: AesGCMCrypto) {

  def encryptSearchResponseDetail(cashTransactions: CashAccountTransactionSearchResponseDetail,
                                  key: String): EncryptedValue = {
    val json = Json.toJson(cashTransactions).toString()
    crypto.encrypt(json, key)
  }

  def decryptSearchResponseDetail(encryptedData: EncryptedValue,
                                  key: String): CashAccountTransactionSearchResponseDetail = {
    val decryptedJson = crypto.decrypt(encryptedData, key)
    Json.parse(decryptedJson).as[CashAccountTransactionSearchResponseDetail]
  }

}