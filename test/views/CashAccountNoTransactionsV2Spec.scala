package views

import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CashAccountViewModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import utils.Utils.singleSpace
import views.html.cash_account_no_transactions_v2

class CashAccountNoTransactionsV2Spec extends ViewTestHelper {

  "view" should {

    "display correct contents" in new Setup {
      val viewDoc: Document = view(model)
      
      titleShouldBeCorrect(viewDoc, "cf.cash-account.detail.title")
      shouldContainBackLinkUrl(viewDoc, appConfig.customsFinancialsFrontendHomepage)

      shouldDisplayCorrectAccountDetails(viewDoc, accNumber)
      shouldDisplayPayImportDutyAndTaxesGuidance(viewDoc)
      shouldDisplayTopUpGuidance(viewDoc)
      shouldDisplayFindOutHowLink(viewDoc)
      shouldDisplayHelpAndSupportGuidance(viewDoc)
    }

  }

  private def shouldDisplayCorrectAccountDetails(viewDoc: Document, accNumber: String)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-number").text mustBe msgs("cf.cash-account.detail.account", accNumber)
    viewDoc.getElementById("cash-account-header").text mustBe msgs("cf.cash-account.detail.title")
    viewDoc.getElementById("zero-balance-display").text() mustBe s"£0$singleSpace${msgs("cf.cash-account.detail.available")}"
  }

  private def shouldDisplayPayImportDutyAndTaxesGuidance(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-import-duties-guidance").text() mustBe msgs("cf.cash-account.detail.no-transactions.p1")
  }

  private def shouldDisplayTopUpGuidance(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-authorise-agent-guidance").text() mustBe msgs("cf.cash-account.top-up.guidance")
  }

  private def shouldDisplayFindOutHowLink(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-top-up-guidance-pre").text() mustBe msgs("cf.cash-account.how-to-use.guidance.text.pre")
    viewDoc.getElementById("cash-account-top-up-guidance-link").text() mustBe msgs("cf.cash-account.detail.link")
    viewDoc.getElementById("cash-account-top-up-guidance-post").text() mustBe msgs("cf.cash-account.how-to-use.guidance.text.post")
  }

  private def shouldDisplayHelpAndSupportGuidance(viewDoc: Document)(implicit msgs: Messages) = {
    viewDoc.getElementById("cash-account-help-and-support-guidance-header").text() mustBe msgs("cf.cash-account.transactions.request.support.heading")
    viewDoc.getElementById("cash-account-help-and-support-guidance-text-pre").text() mustBe msgs("cf.cash-account.help-and-support.link.text.pre")
    viewDoc.getElementById("cash-account-help-and-support-guidance-text-link").text() mustBe msgs("cf.cash-account.help-and-support.link.text")
    viewDoc.getElementById("cash-account-help-and-support-guidance-text-post").text() mustBe msgs("cf.cash-account.help-and-support.link.text.post")
  }



  trait Setup {
    val app: Application = application.build()
    implicit val msgs: Messages = messages(app)
    
    val eori = "test_eori"
    val balances: CDSCashBalance = CDSCashBalance(None)
    val accNumber = "12345678"

    val cashAccount: CashAccount = CashAccount(number = accNumber,
      owner = eori,
      status = AccountStatusOpen,
      balances = balances)

    val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

    def view(account: CashAccountViewModel): Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_no_transactions_v2].apply(account).body)
  }
}

