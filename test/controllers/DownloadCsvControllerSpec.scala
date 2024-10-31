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

package controllers

import config.AppConfig
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException}
import models.*
import org.jsoup.Jsoup.parseBodyFragment
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.AuditingService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Random
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.verify

class DownloadCsvControllerSpec extends SpecBase {
  "downloadCSV" must {

    "return OK with the user's CSV of cash transactions" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = applicationBuilder
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

      val app: Application = applicationBuilder
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK

        val csv = contentAsString(result)
        val actualRows = csv.split("\n").toList

        val htmlTagPattern = "<.*?>".r
        val actualHeaders = actualRows.head.split(",").map { header =>
          htmlTagPattern.replaceAllIn(header.replace("\"", ""), "")
        }.toList

        val expectedHeaders = List(
          "Transaction date",
          "Transaction",
          "Declaration MRN",
          "Declaration UCR",
          "Declarant EORI",
          "Importer EORI",
          "Duty",
          "VAT",
          "Excise",
          "Credit",
          "Debit",
          "Balance"
        )

        actualHeaders mustEqual expectedHeaders
      }
    }

    "return content disposition of 'attachment' by default" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = applicationBuilder
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

      val app: Application = applicationBuilder
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

      val app: Application = applicationBuilder
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        redirectLocation(result).value mustEqual routes.DownloadCsvController.showUnableToDownloadCSV().url
      }
    }

    "generate the expected filename" in new Setup {
      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = applicationBuilder
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector)
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

      when(mockAuditingService.auditCsvDownload(any, any, any, any, any)(any))
        .thenReturn(Future.successful(AuditResult.Success))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      val app: Application = applicationBuilder
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[AuditingService].toInstance(mockAuditingService)
        ).configure("features.fixed-systemdate-for-tests" -> "true"
      ).build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK

        verify(mockAuditingService).auditCsvDownload(
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

      val app: Application = applicationBuilder
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
        val app: Application = applicationBuilder
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .configure("application.cash-account.numberOfDaysToShow" -> "10")
          .build()

        assume(!app.injector.instanceOf[AppConfig].isCashAccountV2FeatureFlagEnabled)

        val expectedTransaction = 25

        when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(cashAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveCashTransactions(eqTo(someCan), any, any)(any))
          .thenReturn(Future.successful(Right(randomCashTransaction(expectedTransaction))))

        running(app) {
          val request = FakeRequest(GET, routes.CashAccountController.showAccountDetails(None).url + "?page=5")
          val result = route(app, request).value
          val html = parseBodyFragment(contentAsString(result)).body
          val pageNumberLinks = html.select("li.govuk-pagination__item > a").asScala

          withClue("html did not contain any pagination links:") {
            pageNumberLinks.size must not be 0
          }

          pageNumberLinks.map { pageNumberLink =>
            withClue(s"page number link $pageNumberLink must include page number") {
              pageNumberLink.attr("href") must include(s"page=" + pageNumberLink.text())
            }
          }
        }
      }
    }

    "display too many results page when too many results returned during download" in new Setup {

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(cashAccountNumber), any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val app: Application = applicationBuilder
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
      when(mockAuditingService.auditCsvDownload(any, any, any, any, any)(any))
        .thenReturn(Future.successful(AuditResult.Success))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      running(newApp) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(
          Some("attachment"), dateRange).url)

        val result = route(newApp, request).value
        status(result) mustEqual OK
      }
    }

    "return Bad request when invalid dates are submitted" in new Setup {

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, "/customs/cash-account/download-requested-csv?from=20-02-01&to=2020-03-31")

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

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)

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

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)

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

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)

      running(newApp) {
        val result = route(newApp, request).value
        status(result) mustBe SEE_OTHER
      }
    }


    "audit the download of the CSV" in new Setup {
      when(mockAuditingService.auditCsvDownload(any, any, any, any, any)(any))
        .thenReturn(Future.successful(AuditResult.Success))

      when(mockCustomsFinancialsApiConnector.getCashAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(cashAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveCashTransactionsDetail(eqTo(someCan), any, any)(any))
        .thenReturn(Future.successful(Right(cashTransactionResponse)))

      running(newApp) {
        val request = FakeRequest(
          GET, routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dateRange).url)

        val result = route(newApp, request).value
        status(result) mustEqual OK
        verify(mockAuditingService).auditCsvDownload(
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
    val sMRN = "ic62zbad-75fa-445f-962b-cc92311686b8e"

    val mockAuditingService: AuditingService = mock[AuditingService]
    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val cashAccount: CashAccount = CashAccount(cashAccountNumber, eori,
      AccountStatusOpen, CDSCashBalance(Some(BigDecimal(123456.78))))

    val listOfPendingTransactions: Seq[Declaration] =
      Seq(Declaration("pendingDeclarationID", Some("pendingImporterEORI"),
        "pendingDeclarantEORINumber", Some("pendingDeclarantReference"),
        LocalDate.parse("2020-07-21"), -100.00, Nil, Some(sMRN)))

    val day = 10
    val month = 10
    val year = 2019

    val dateRange: RequestedDateRange =
      RequestedDateRange(LocalDate.of(year, month, day), LocalDate.of(year, month, day))

    val cashDailyStatements: Seq[CashDailyStatement] = Seq(
      CashDailyStatement(LocalDate.parse("2020-07-18"), 0.0, 1000.00,
        Seq(Declaration("mrn1", Some("Importer EORI"), "Declarant EORI",
          Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -84.00, Nil, Some(sMRN)),
          Declaration("mrn2", Some("Importer EORI"), "Declarant EORI",
            Some("Declarant Reference"), LocalDate.parse("2020-07-18"), -65.00, Nil, Some(sMRN))),
        Seq(Transaction(45.67, Payment, None), Transaction(-76.34, Withdrawal, Some("77665544")))),

      CashDailyStatement(LocalDate.parse("2020-07-20"), 0.0, 1200.00,
        Seq(Declaration("mrn3", Some("Importer EORI"), "Declarant EORI",
          Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -90.00, Nil, Some(sMRN)),
          Declaration("mrn4", Some("Importer EORI"), "Declarant EORI",
            Some("Declarant Reference"), LocalDate.parse("2020-07-20"), -30.00, Nil, Some(sMRN))),
        Seq(Transaction(67.89, Payment, None)))
    )

    val nonFatalResponse: UpstreamErrorResponse = UpstreamErrorResponse("ServiceUnavailable",
      Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)

    val cashTransactionResponse: CashTransactions = CashTransactions(listOfPendingTransactions, cashDailyStatements)

    val newApp: Application = applicationBuilder
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[AuditingService].toInstance(mockAuditingService)
      )
      .configure("features.fixed-systemdate-for-tests" -> "true")
      .build()
  }

  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def randomFloat: Float = Random.nextFloat()

  def randomLong: Long = Random.nextLong()

  def randomBigDecimal: BigDecimal = BigDecimal(randomFloat.toString)

  def randomLocalDate: LocalDate = {
    val monthsToSubtract: Int = 36
    LocalDate.now().minusMonths(Random.nextInt(monthsToSubtract))
  }

  def randomCashTransaction(howMany: Int): CashTransactions = {
    val pendingStatementsNumber: Int = 20
    CashTransactions(randomPendingDailyStatements(pendingStatementsNumber), randomCashDailyStatements(howMany))
  }


  def randomDeclaration: Declaration = {
    val randomStringLength: Int = 10

    Declaration(randomString(randomStringLength),
      Some(randomString(randomStringLength)),
      randomString(randomStringLength),
      Some(randomString(randomStringLength)),
      randomLocalDate,
      randomBigDecimal,
      Nil,
      Some("ic62zbad-75fa-445f-962b-cc92311686b8e"))
  }

  def randomCashDailyStatement: CashDailyStatement = {
    val declarations: Int = 5
    val otherTransactions: Int = 7

    CashDailyStatement(randomLocalDate,
      randomBigDecimal,
      randomBigDecimal,
      randomDeclarations(declarations),
      randomTransactions(otherTransactions))
  }

  val types: Seq[String] = Seq("Payment", "Withdrawal", "Transfer")

  def randomTransactions(howMany: Int): Seq[Transaction] = List.fill(howMany)(randomTransaction)

  def randomTransaction: Transaction = Transaction(randomBigDecimal, Transfer, None)

  def randomDeclarations(howMany: Int): Seq[Declaration] = List.fill(howMany)(randomDeclaration)

  def randomPendingDailyStatements(howMany: Int): Seq[Declaration] = List.fill(howMany)(randomDeclaration)

  def randomCashDailyStatements(howMany: Int): Seq[CashDailyStatement] = List.fill(howMany)(randomCashDailyStatement)
}
