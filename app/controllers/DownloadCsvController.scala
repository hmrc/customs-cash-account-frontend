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

import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested}
import controllers.actions.IdentifierAction
import helpers.CashAccountUtils
import models.RequestedDateRange
import models.request.IdentifierRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{AuditingService, DateTimeService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.CashTransactionCsvRow.DailyStatementCsvRowsViewModel
import viewmodels.{CSVWriter, ResultsPageSummary}
import views.html.{cash_account_unable_download_csv, cash_transactions_no_result, cash_transactions_too_many_results}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class DownloadCsvController @Inject()(
                                       identify: IdentifierAction,
                                       apiConnector: CustomsFinancialsApiConnector,
                                       auditingService: AuditingService,
                                       dateTimeService: DateTimeService,
                                       cashAccountUtils: CashAccountUtils,
                                       unableToDownloadCSV: cash_account_unable_download_csv,
                                       tooManyResults: cash_transactions_too_many_results,
                                       noResults: cash_transactions_no_result
                                     )(implicit ec: ExecutionContext,
                                       eh: ErrorHandler,
                                       mcc: MessagesControllerComponents,
                                       appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  def downloadRequestedCsv(disposition: Option[String],
                           dateRange: RequestedDateRange): Action[AnyContent] =
    identify.async { implicit request =>
      Try(dateRange) match {
        case Failure(_) => Future.successful(BadRequest)
        case Success(value) => getAndDownloadCsv(value.from, value.to, disposition,
          cashAccountUtils.filenameRequestCashTransactions(value.from, value.to))
      }
    }

  def downloadCsv(disposition: Option[String]): Action[AnyContent] = identify.async { implicit request =>
    val (from, to) = cashAccountUtils.transactionDateRange()
    getAndDownloadCsv(from, to, disposition, cashAccountUtils.filenameWithDateTime())
  }

  def showUnableToDownloadCSV(): Action[AnyContent] = identify { implicit req =>
    Ok(unableToDownloadCSV())
  }

  private def getAndDownloadCsv(from: LocalDate,
                                to: LocalDate,
                                disposition: Option[String],
                                filename: String)(implicit request: IdentifierRequest[AnyContent]): Future[Result] = {
    apiConnector.getCashAccount(request.eori) flatMap {
      case None => Future.successful(NotFound(eh.notFoundTemplate))
      case Some(cashAccount) => {
        for {
          cashTransactions <- apiConnector.retrieveCashTransactionsDetail(cashAccount.number, from, to)
          result = cashTransactions match {

            case Left(errorResponse) => errorResponse match {
              case TooManyTransactionsRequested =>
                Ok(tooManyResults(new ResultsPageSummary(from, to),
                  controllers.routes.CashAccountController.showAccountDetails(None).url))
              case NoTransactionsAvailable =>
                Ok(noResults(new ResultsPageSummary(from, to)))
              case _ =>
                Redirect(routes.DownloadCsvController.showUnableToDownloadCSV())
            }

            case Right(transactions) => {
              auditingService.auditCsvDownload(
                request.eori, cashAccount.number, dateTimeService.utcDateTime(), from, to)
              val csvContent = CSVWriter.toCSVWithHeaders(
                rows = transactions.cashDailyStatements.sorted.flatMap(_.toReportLayout),
                mappingFn = cashAccountUtils.makeHumanReadable,
                footer = None)
              val contentHeaders = "Content-Disposition" ->
                s"${disposition.getOrElse("attachment")}; filename=${filename}"
              Ok(csvContent).withHeaders(contentHeaders)
            }
          }
        } yield result
      }
    }
  }
}
