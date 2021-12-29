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

package controllers

import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException}
import models.{AccountStatusOpen, CDSCashBalance, CashAccount, CashDailyStatement, CashTransactions, Declaration, Payment, RequestedDateRange, Transaction, Transfer, Withdrawal}
import org.jsoup.Jsoup.parseBodyFragment
import play.api.http.Status
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditingService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.SpecBase

import java.time.LocalDate
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.Future
import scala.util.Random

class DownloadCsvControllerSpec extends SpecBase {
  "downloadCSV" must {
    "return OK with the user's CSV of cash transactions" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(""","mrn3",""")
      }
    }

    "order daily statements by descending date" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK

        val csv = contentAsString(result)
        val actualRows = csv.split("\n").toList
        val actualHeaders = actualRows.head.split(",")
        val expectedHeaders = List(
          "\"Date (posted)\"",
          "\"Transaction\"",
          "\"MRN\"",
          "\"UCR\"",
          "\"Declarant EORI\"",
          "\"VAT\"",
          "\"Duty\"",
          "\"Excise\"",
          "\"Credit\"",
          "\"Debit\"",
          "\"Balance\""
        )
        actualHeaders must be(expectedHeaders)
      }
    }

    "return content disposition of 'attachment' by default" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        val actualHeaders = headers(result)
        actualHeaders("Content-Disposition") must startWith("attachment")
      }
    }

    "return any content disposition passed in (via query parameter)" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET,
          routes.DownloadCsvController.downloadCsv(Some("FakeDisposition")).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        val actualHeaders = headers(result)
        actualHeaders("Content-Disposition") must startWith("FakeDisposition")
      }
    }

    "redirect to download csv error when failed to download csv" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        redirectLocation(result).value mustEqual routes.DownloadCsvController.showUnableToDownloadCSV.url
      }
    }

    "generate the expected filename" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        ).configure("features.fixed-systemdate-for-tests" -> "true"
      ).build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        val actualHeaders = headers(result)
        actualHeaders("Content-Disposition") must include(s"filename=Cash_Account_Transactions_20271220123000.CSV")
      }
    }

    "audit the download of the CSV" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockAuditingservice.auditCsvDownload(any, any, any, any, any)(any)).thenReturn(Future.successful(AuditResult.Success))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[AuditingService].toInstance(mockAuditingservice)
        ).configure("features.fixed-systemdate-for-tests" -> "true"
      ).build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK

        verify(mockAuditingservice).auditCsvDownload(
          eqTo("exampleEori"),
          eqTo("1234567"),
          any[java.time.LocalDateTime],
          any[LocalDate],
          any[LocalDate])(any)
      }
    }

    "the user does not have a cash account" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      val app = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector)
        ).configure("features.fixed-systemdate-for-tests" -> "true"
      ).build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual NOT_FOUND
      }
    }

    "paginator" should {
      "be compatible with the page and mrn query parameter" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(someCan), any, any)(any))
          .thenReturn(Future.successful(Right(randomCashTransaction(25))))

        val app = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .configure("application.cash-account.numberOfDaysToShow" -> "10")
          .build()

        running(app) {
          val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(None).url + "?page=5")
          val result = route(app, request).value
          val html = parseBodyFragment(contentAsString(result)).body
          val pageNumberLinks = html.select("li.govuk-pagination__number > a").asScala
          withClue("html did not contain any pagination links:") {
            pageNumberLinks.size must not be (0)
          }
          pageNumberLinks.map { pageNumberLink =>
            withClue(s"page number link $pageNumberLink must include page number") {
              pageNumberLink.attr("href") must include(s"page=" + pageNumberLink.text())
            }
          }
        }
      }

      "show the expected location description" in new Setup {
        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(someCan), any, any)(any))
          .thenReturn(Future.successful(Right(randomCashTransaction(25))))

        val app = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .configure("application.cash-account.numberOfDaysToShow" -> "25")
          .build()

        running(app) {
          val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(None).url + "?page=5&mrn=true")
          val result = route(app, request).value
          val html = parseBodyFragment(contentAsString(result)).body
          val actualLocationDescription = html.getElementsByClass("govuk-pagination__results").asScala.map(_.text).head
          actualLocationDescription must startWith("Showing 26")
        }
      }
    }

    "display too many results page when too many results returned during download" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()
      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        contentAsString(result) must include regex "Your search returned too many results"
      }

    }
  }

  "downloadRequestedCSV" must {
    "return OK" in new Setup {
      when(mockAuditingservice.auditCsvDownload(any, any, any, any, any)(any)).thenReturn(Future.successful(AuditResult.Success))
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      running(newApp) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)
        val result = route(newApp, request).value
        status(result) mustEqual OK
      }
    }

    "return Bad request when invalid dates are submitted" in new Setup {

      val request = fakeRequest(GET,"/customs/cash-account/download-requested-csv?from=20-02-01&to=2020-03-31")

      running(newApp) {
        val result = route(newApp, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return No Transactions view when no data is returned for the search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      val request = fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)

      running(newApp) {
        val result = route(newApp, request).value
        contentAsString(result) must include regex "No cash account transactions"
      }
    }

    "return Exceeded Threshold view when too many results returned for the search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val request = fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)

      running(newApp) {
        val result = route(newApp, request).value
        contentAsString(result) must include regex "Your search returned too many results"
      }
    }

    "return redirect to unable to download csv search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      val request = fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)

      running(newApp) {
        val result = route(newApp, request).value
        status(result) mustBe SEE_OTHER
      }
    }


    "audit the download of the CSV" in new Setup {
      when(mockAuditingservice.auditCsvDownload(any, any, any, any, any)(any)).thenReturn(Future.successful(AuditResult.Success))
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      running(newApp) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)
        val result = route(newApp, request).value
        status(result) mustEqual OK
        verify(mockAuditingservice).auditCsvDownload(
          eqTo("exampleEori"),
          eqTo("1234567"),
          any[java.time.LocalDateTime],
          any[LocalDate],
          any[LocalDate])(any)
      }
    }
  }

 trait Setup {
   val cashAccountNumber = "1234567"
   val eori = "exampleEori"
   val someCan = "1234567"

   val mockAuditingservice = mock[AuditingService]
   val mockCustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
   val cashAccount = CashAccount(cashAccountNumber, eori, AccountStatusOpen, CDSCashBalance(Some(BigDecimal(123456.78))))
   val listOfPendingTransactions =
     Seq(Declaration("pendingDeclarationID", "pendingDeclarantEORINumber", Some("pendingDeclarantReference"), LocalDate.parse("2020-07-21"), -100.00, Nil))

   val dateRange = RequestedDateRange(LocalDate.of(2019,10,10),LocalDate.of(2019,10,10))

   val cashDailyStatements = Seq(
     CashDailyStatement(LocalDate.parse("2020-07-18"), 0.0, 1000.00,
       Seq(Declaration("mrn1", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil),
         Declaration("mrn2", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -65.00, Nil)),
       Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))),
     CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 1200.00,
       Seq(Declaration("mrn3", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil),
         Declaration("mrn4", "Declarant EORI", Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil)),
       Seq(Transaction(67.89, Payment, None))))

   val nonFatalResponse = UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)
   val cashTransactionResponse = CashTransactions(listOfPendingTransactions, cashDailyStatements)

   val newApp = application
     .overrides(
       bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
       bind[AuditingService].toInstance(mockAuditingservice)
     )
     .configure("features.fixed-systemdate-for-tests" -> "true")
     .build()
 }

  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def randomFloat: Float = Random.nextFloat()

  def randomLong: Long = Random.nextLong()

  def randomBigDecimal: BigDecimal = BigDecimal(randomFloat.toString)

  def randomLocalDate: LocalDate = LocalDate.now().minusMonths(Random.nextInt(36))

  def randomCashTransaction(howMany: Int): CashTransactions =
    CashTransactions(randomPendingDailyStatements(20), randomCashDailyStatements(howMany))

  def randomDeclaration: Declaration =
    Declaration(randomString(10),
      randomString(10),
      Some(randomString(10)),
      randomLocalDate,
      randomBigDecimal, Nil)

  def randomCashDailyStatement: CashDailyStatement =
    CashDailyStatement(randomLocalDate,
      randomBigDecimal,
      randomBigDecimal,
      randomDeclarations(5),
      randomTransactions(7))

  val types = Seq("Payment", "Withdrawal", "Transfer")

  def randomTransactions(howMany: Int): Seq[Transaction] = List.fill(howMany)(randomTransaction)

  def randomTransaction: Transaction = Transaction(randomBigDecimal, Transfer, None)

  def randomDeclarations(howMany: Int): Seq[Declaration] = List.fill(howMany)(randomDeclaration)

  def randomPendingDailyStatements(howMany: Int): Seq[Declaration] = List.fill(howMany)(randomDeclaration)

  def randomCashDailyStatements(howMany: Int): Seq[CashDailyStatement] = List.fill(howMany)(randomCashDailyStatement)

}
