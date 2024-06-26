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

package views.components

import config.AppConfig
import helpers.Formatters.formatCurrencyAmount
import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CashAccountViewModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import views.html.components.cash_account_balance

class CashAccountBalanceSpec extends SpecBase {

  "component" should {

    "display correct text" when {

      "model has available balance and showBalance is true" in new Setup {
        val balancesValue: CDSCashBalance = CDSCashBalance(Some(BigDecimal(accountBalance)))

        val cashAccount: CashAccount = CashAccount(number = accNumber,
          owner = eori,
          status = AccountStatusOpen,
          balances = balancesValue)

        val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

        val componentView: Document = view(model)

        shouldDisplayAccountNumberWithText(componentView, accNumber)
        shouldDisplayCashAccountHeading(componentView)
        shouldDisplayCorrectAmountWithCorrectFormat(componentView, Some(BigDecimal(accountBalance)))
      }

      "model has no available balance and showBalance is true" in new Setup {
        val cashAccount: CashAccount = CashAccount(number = accNumber,
          owner = eori,
          status = AccountStatusOpen,
          balances = balances)

        val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

        val componentView: Document = view(model)

        shouldDisplayAccountNumberWithText(componentView, accNumber)
        shouldDisplayCashAccountHeading(componentView)
        shouldDisplayCorrectAmountWithCorrectFormat(componentView)
      }

      "model has available balance and showBalance is false" in new Setup {
        val balancesValue: CDSCashBalance = CDSCashBalance(Some(BigDecimal(accountBalance)))

        val cashAccount: CashAccount = CashAccount(number = accNumber,
          owner = eori,
          status = AccountStatusOpen,
          balances = balancesValue)

        val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

        val componentView: Document = view(model, showBalance = false)

        shouldDisplayAccountNumberWithText(componentView, accNumber)
        shouldDisplayCashAccountHeading(componentView)
        shouldNotDisplayAmountText(componentView)
      }

      "model has no available balance and showBalance is false" in new Setup {
        val cashAccount: CashAccount = CashAccount(number = accNumber,
          owner = eori,
          status = AccountStatusOpen,
          balances = balances)

        val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

        val componentView: Document = view(model, showBalance = false)

        shouldDisplayAccountNumberWithText(componentView, accNumber)
        shouldDisplayCashAccountHeading(componentView)
        shouldNotDisplayAmountText(componentView)
      }
    }
  }

  private def shouldDisplayAccountNumberWithText(viewDoc: Document,
                                                 accNumber: String)(implicit msgs: Messages): Assertion = {
    viewDoc.getElementById("account-number")
      .text() mustBe msgs("cf.cash-account.detail.account", accNumber)
  }

  private def shouldDisplayCashAccountHeading(viewDoc: Document)(implicit msgs: Messages): Assertion = {
    viewDoc.getElementsByTag("h1").text() mustBe msgs("cf.cash-account.detail.heading")
  }

  private def shouldDisplayCorrectAmountWithCorrectFormat(viewDoc: Document,
                                                          balance: Option[BigDecimal] = None)
                                                         (implicit msgs: Messages): Assertion = {
    if (balance.isDefined) {
      viewDoc.getElementById("balance-available")
        .text() mustBe s"${formatCurrencyAmount(balance.get)} ${msgs("cf.cash-account.detail.available")}"
    } else {
      viewDoc.getElementById("balance-available")
        .text() mustBe s"${formatCurrencyAmount(0)} ${msgs("cf.cash-account.detail.available")}"
    }
  }

  private def shouldNotDisplayAmountText(viewDoc: Document): Assertion = {
    Option(viewDoc.getElementById("balance-available")) mustBe empty
  }

  trait Setup {
    val app: Application = application.build()
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msgs: Messages = messages(app)

    val eori = "test_eori"
    val accountBalance = 6000
    val balances: CDSCashBalance = CDSCashBalance(None)
    val accNumber = "12345678"

    def view(accountModel: CashAccountViewModel,
             showBalance: Boolean = true): Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_balance].apply(accountModel, showBalance).body)
  }

}
