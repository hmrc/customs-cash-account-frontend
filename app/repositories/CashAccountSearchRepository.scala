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

import crypto.{CashAccountTransactionSearchResponseDetailEncrypter, EncryptedValue}
import models.response.CashAccountTransactionSearchResponseDetail
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import play.api.Configuration
import play.api.libs.json.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.ToSingleObservablePublisher

@Singleton
class CashAccountSearchRepository @Inject() (
  mongo: MongoComponent,
  config: Configuration,
  encrypter: CashAccountTransactionSearchResponseDetailEncrypter
)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[CashAccountTransactionSearchResponseDetailMongo](
      collectionName = "cash-account-search-cache",
      mongoComponent = mongo,
      domainFormat = CashAccountTransactionSearchResponseDetailMongo.format,
      indexes = Seq(
        IndexModel(
          ascending("lastUpdated"),
          IndexOptions()
            .name("cash-account-search-cache-last-updated-index")
            .expireAfter(config.get[Long]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
        )
      )
    )
    with CashAccountSearchRepositoryTrait {

  private val encryptionKey = config.get[String]("mongodb.encryptionKey")

  override def get(id: String): Future[Option[CashAccountTransactionSearchResponseDetail]] =
    for {
      result <- collection.find(equal("_id", id)).toSingle().toFutureOption()
      account = result.map(responseDetailMongo =>
                  encrypter.decryptSearchResponseDetail(responseDetailMongo.responseDetail, encryptionKey)
                )
    } yield account

  override def set(id: String, transactions: CashAccountTransactionSearchResponseDetail): Future[Boolean] = {
    val record: CashAccountTransactionSearchResponseDetailMongo = CashAccountTransactionSearchResponseDetailMongo(
      encrypter.encryptSearchResponseDetail(transactions, encryptionKey),
      Instant.now()
    )

    collection.replaceOne(equal("_id", id), record, ReplaceOptions().upsert(true)).toFuture().map(_.wasAcknowledged())
  }

  override def remove(id: String): Future[Boolean] =
    collection.deleteOne(equal("_id", id)).toFuture().map(_.wasAcknowledged())
}

trait CashAccountSearchRepositoryTrait {

  def get(id: String): Future[Option[CashAccountTransactionSearchResponseDetail]]

  def set(id: String, transactions: CashAccountTransactionSearchResponseDetail): Future[Boolean]

  def remove(id: String): Future[Boolean]
}

case class CashAccountTransactionSearchResponseDetailMongo(responseDetail: EncryptedValue, lastUpdated: Instant)

object CashAccountTransactionSearchResponseDetailMongo {

  implicit val jodaTimeFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  implicit val format: OFormat[CashAccountTransactionSearchResponseDetailMongo] =
    Json.format[CashAccountTransactionSearchResponseDetailMongo]
}
