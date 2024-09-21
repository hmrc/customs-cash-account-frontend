package views

import config.AppConfig
import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CashAccountViewModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import utils.SpecBase
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import views.html.cash_account_no_transactions_v2

class CashAccountNoTransactionsV2Spec extends ViewTestHelper {

  "CashAccountNoTransactionsV2 view" should {

    "populate the correct title and link" in new Setup {
      val viewDoc: Document = view(model)
      titleShouldBeCorrect(viewDoc, "cf.cash-account.detail.title")
      shouldContainBackLinkUrl(viewDoc, appConfig.customsFinancialsFrontendHomepage)
    }

    "display the correct paragraphs and links" in new Setup {
      val viewDoc: Document = view(model)
      containsImportAndAuthoriseAgentText(viewDoc)
      containsTopUpCashText(viewDoc)
      containsCDSDeclarationsTextAndLink(viewDoc)

      def containsImportAndAuthoriseAgentText(view: Document)(implicit pMsgKey: Messages): Assertion =
      view.select("p").get(1).text() mustBe pMsgKey("cf.cash-account.detail.no-transactions.p1")

      def containsTopUpCashText(view: Document)(implicit pMsgKey: Messages): Assertion =
        view.select("p").get(2).text() mustBe pMsgKey("cf.cash-account.top-up.guidance")

      def containsCDSDeclarationsTextAndLink(view: Document)(implicit linkMsg: Messages, config: AppConfig): Assertion =
        val linkElement = view.getElementById("cf.cash-account.how-to-use.guidance.link")

        linkElement.attribute("href").getValue mustBe config.cashAccountForCdsDeclarationsUrl
        linkElement.text() mustBe linkMsg("cf.cash-account.how-to-use.guidance.link.text")

        view.text().contains(linkMsg("cf.cash-account.how-to-use.guidance.text.pre")) mustBe true
        view.text().contains(linkMsg("cf.cash-account.how-to-use.guidance.text.post")) mustBe true
        view.text().contains(linkMsg("cf.cash-account.how-to-use.guidance.link.text")) mustBe true
    }

    "Display the correct Support heading, text and link" in new Setup {
      val viewDoc: Document = view(model)
      containsHeadingAndSupportTitle(viewDoc)
      containsContactHMRCTextAndLink(viewDoc)

      def containsHeadingAndSupportTitle(view: Document)(implicit titleMsg: Messages): Assertion =
        view.select("h2").get(1).text() mustBe titleMsg("cf.cash-account.transactions.request.support.heading")

      def containsContactHMRCTextAndLink(view: Document)(implicit linkMsg: Messages, config: AppConfig): Assertion =
        val linkElement = view.getElementsByClass("govuk-body govuk-!-margin-bottom-9")

        linkElement.get(0).getElementsByTag("a").attr("href") mustBe config.cashAccountForCdsDeclarationsUrl

        view.text().contains(linkMsg("cf.cash-account.help-and-support.link.text.pre")) mustBe true
        view.text().contains(linkMsg("cf.cash-account.help-and-support.link.text.post")) mustBe true
        view.text().contains(linkMsg("cf.cash-account.help-and-support.link.text")) mustBe true
    }
  }


  trait Setup {
    val eori = "GB123456"
    val balances: CDSCashBalance = CDSCashBalance(None)
    val accountNumber = "1111111"

    val cashAccount: CashAccount = CashAccount(number = accountNumber,
      owner = eori,
      status = AccountStatusOpen,
      balances = balances)

    val model: CashAccountViewModel = CashAccountViewModel(eori, cashAccount)

    val app: Application = application.build()
    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msgs: Messages = messages(app)

    def view(accountModel: CashAccountViewModel): Document =
      Jsoup.parse(app.injector.instanceOf[cash_account_no_transactions_v2].apply(accountModel).body)
    }
  }

