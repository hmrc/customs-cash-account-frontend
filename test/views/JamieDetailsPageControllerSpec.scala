package views

import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.jamie_details_page

class JamieDetailsPageControllerSpec extends ViewTestHelper {
  "view should" in new Setup {
    titleShouldBeCorrect(view, "cf.cash-account.detail.title")
    shouldContainBackLinkUrl(view, expectedBackLinkUrl)
    shouldContainCorrectTitle(view)
    shouldContainCorrectNameField(view)
    shouldContainCorrectAgeField(view)
    shouldDisplayHelpAndSupportHeader(view)
    shouldDisplayHelpAndSupportTextAndLink(view)
    shouldContainCorrectNiNumberField(view)
  }

  private def shouldContainCorrectTitle(viewDocument: Document) = {
    viewDocument.getElementById("details-header").text mustBe "Person Details"
  }
  
  private def shouldContainCorrectNameField(viewDocument: Document) = {
    viewDocument.getElementById("details-name").text mustBe "Name: Jamie"
  }

  private def shouldContainCorrectNiNumberField(viewDocument: Document) = {
    viewDocument.getElementById("details-ni-number").text mustBe "NI Number: DF45667a"
  }
  
  private def shouldContainCorrectAgeField(viewDocument: Document) = {
    viewDocument.getElementById("details-age").text mustBe "Age: 41"
  }

  private def shouldDisplayHelpAndSupportHeader(viewDocument: Document)(implicit msgs: Messages) = {
    viewDocument.getElementById("help-and-support-header")
      .text mustBe msgs("cf.cash-account.transactions.request.support.heading")
  }

  private def shouldDisplayHelpAndSupportTextAndLink(viewDocument: Document)
                                                    (implicit msgs: Messages, config: AppConfig) = {
    val viewAsHtml = viewDocument.html()

    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.pre.v2")) mustBe true
    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text")) mustBe true
    viewAsHtml.contains(msgs("cf.cash-account.help-and-support.link.text.post")) mustBe true
    viewAsHtml.contains(config.cashAccountForCdsDeclarationsUrl) mustBe true
  }
  
  
  trait Setup {
    val name = "Jamie"
    val age = 41
    val niNumber = "DF45667a"
    val expectedBackLinkUrl: String =
      controllers.routes.JamiePageController.onPageLoad().url
    val view: Document = Jsoup.parse(app.injector.instanceOf[jamie_details_page].apply(name, age, Some(niNumber)).body)
  }
}
