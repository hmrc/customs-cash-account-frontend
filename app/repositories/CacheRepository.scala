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

package repositories

import crypto.{CashTransactionsEncrypter, CryptoAdapter}
import models.{CashTransactions, EncryptedCashTransactions}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import play.api.Configuration
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.crypto.Crypted

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.ToSingleObservablePublisher

@Singleton
class DefaultCacheRepository @Inject() (
  mongo: MongoComponent,
  config: Configuration,
  encrypter: CashTransactionsEncrypter
)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[CashTransactionsMongo](
      collectionName = "cash-account-cache",
      mongoComponent = mongo,
      domainFormat = CashTransactionsMongo.format,
      indexes = Seq(
        IndexModel(
          ascending("lastUpdated"),
          IndexOptions()
            .name("cash-account-cache-last-updated-index")
            .expireAfter(config.get[Long]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
        )
      )
    )
    with CacheRepository {

  override def get(id: String): Future[Option[CashTransactions]] =
    for {
      result <- collection.find(equal("_id", id)).toSingle().toFutureOption()
      account =
        result.map(cashAccountMongo => encrypter.decryptCashTransactions(cashAccountMongo.transactions))
    } yield account

  override def set(id: String, transactions: CashTransactions): Future[Boolean] = {
    val record: CashTransactionsMongo =
      CashTransactionsMongo(encrypter.encryptCashTransactions(transactions), Instant.now())

    collection.replaceOne(equal("_id", id), record, ReplaceOptions().upsert(true)).toFuture().map(_.wasAcknowledged())
  }

  override def remove(id: String): Future[Boolean] =
    collection.deleteOne(equal("_id", id)).toFuture().map(_.wasAcknowledged())
}

trait CacheRepository {

  def get(id: String): Future[Option[CashTransactions]]

  def set(id: String, transactions: CashTransactions): Future[Boolean]

  def remove(id: String): Future[Boolean]
}

case class CashTransactionsMongo(transactions: EncryptedCashTransactions, lastUpdated: Instant)

object CashTransactionsMongo {
  implicit val jodaTimeFormat: Format[Instant]        = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[CashTransactionsMongo] = Json.format[CashTransactionsMongo]
}
