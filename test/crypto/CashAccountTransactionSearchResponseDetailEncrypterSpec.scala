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
import models.response.*
import play.api.Configuration
import uk.gov.hmrc.crypto.Crypted
import utils.SpecBase

class CashAccountTransactionSearchResponseDetailEncrypterSpec extends SpecBase {

  private val secretKey     = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val config        = Configuration("mongodb.encryptionKey" -> secretKey)
  private val cryptoAdapter = new Crypto(config)
  private val encrypter     = new CashAccountTransactionSearchResponseDetailEncrypter(cryptoAdapter)

  "encrypter" must {

    "encrypt and decrypt declaration search response detail correctly" in new Setup {
      val encryptedValue = encrypter.encryptSearchResponseDetail(declarationsSearchResponseDetail)

      encryptedValue mustBe a[Crypted]

      val decrypted = encrypter.decryptSearchResponseDetail(encryptedValue)
      decrypted mustEqual declarationsSearchResponseDetail
    }

    "encrypt and decrypt payment search response detail correctly" in new Setup {
      val encryptedValue = encrypter.encryptSearchResponseDetail(paymentSearchResponseDetail)

      encryptedValue mustBe a[Crypted]

      val decrypted = encrypter.decryptSearchResponseDetail(encryptedValue)
      decrypted mustEqual paymentSearchResponseDetail
    }
  }

  trait Setup {

    val accNumber          = "testCAN"
    val eoriData: EoriData = EoriData(eoriNumber = "GB123456789012", name = "Test Importer")

    val declarations: Seq[DeclarationWrapper] = Seq(
      DeclarationWrapper(
        DeclarationSearch(
          declarationID = "18GB9JLC3CU1LFGVR8",
          declarantEORINumber = "GB123456789",
          importersEORINumber = "GB987654321",
          postingDate = "2022-07-15",
          acceptanceDate = "2022-07-01",
          amount = 2500.0,
          taxGroups = Seq(
            TaxGroupWrapper(
              TaxGroupSearch(
                taxGroupDescription = "VAT",
                amount = 2000.0,
                taxTypes = Seq(
                  TaxTypeWithSecurityContainer(
                    TaxTypeWithSecurity(reasonForSecurity = Some("Duty"), taxTypeID = "A10", amount = 2000.0)
                  )
                )
              )
            )
          )
        )
      )
    )

    val paymentsWithdrawalsAndTransfers: Seq[PaymentsWithdrawalsAndTransferContainer] = Seq(
      PaymentsWithdrawalsAndTransferContainer(
        PaymentsWithdrawalsAndTransfer(
          valueDate = "2022-07-20",
          postingDate = "2022-07-21",
          paymentReference = "REF123456",
          amount = 1500.0,
          `type` = PaymentType.Payment,
          bankAccount = Some("12345678"),
          sortCode = Some("12-34-56")
        )
      )
    )

    val paymentSearchResponseDetail: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        can = accNumber,
        eoriDetails = Seq(EoriDataContainer(eoriData = eoriData)),
        declarations = Some(Seq.empty),
        paymentsWithdrawalsAndTransfers = Some(paymentsWithdrawalsAndTransfers)
      )

    val declarationsSearchResponseDetail: CashAccountTransactionSearchResponseDetail =
      CashAccountTransactionSearchResponseDetail(
        can = accNumber,
        eoriDetails = Seq(EoriDataContainer(eoriData = eoriData)),
        declarations = Some(declarations),
        paymentsWithdrawalsAndTransfers = Some(Seq.empty)
      )
  }
}
