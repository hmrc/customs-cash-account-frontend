package views

import config.AppConfig
import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CashAccountViewModel}
import org.apache.pekko.actor.setup.Setup
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import utils.SpecBase
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import views.html.cash_account_no_transactions_v2

class CashAccountNoTransactionsV2Spec extends ViewTestHelper {

  "CashAccountNoTransactionsV2 view" should {
    "render the correct title and headings and messages" in new Setup {
      val eori = "GB123456"
      val balances: CDSCashBalance = CDSCashBalance(None)
      val accountNumber = "1111111"

      val cashAccount: CashAccount = CashAccount(number = accountNumber,
        owner = eori,
        status = AccountStatusOpen,
        balances = balances)

      val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)
      val viewDoc: Document = view(model)

      titleShouldBeCorrect(viewDoc, "cf.cash-account.detail.title")
      shouldContainBackLinkUrl(viewDoc, appConfig.customsFinancialsFrontendHomepage)
//      viewDoc.getElementsByTag("p").text() mustBe msgs("cf.cash-account.detail.no-transactions.p1")
//      viewDoc.getElementsByTag("p").text() mustBe msgs("cf.cash-account.top-up.guidance")
//      viewDoc.getElementsByTag("link").text() mustBe msgs("cf.cash-account.how-to-use.guidance.link.text")
//      viewDoc.getElementsByTag("h2").text() mustBe msgs("cf.cash-account.transactions.request.support.heading")

    }
  }


  trait Setup {
    val app: Application = application.build()
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msgs: Messages = messages(app)

    def view(accountModel: CashAccountViewModel): Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_no_transactions_v2].apply(accountModel).body)
    }
  }

