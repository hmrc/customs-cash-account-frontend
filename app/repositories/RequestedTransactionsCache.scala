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

import models.CashTransactionDates
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import play.api.Configuration
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefaultRequestedTransactionsCache @Inject()(mongoComponent: MongoComponent,
                                                  config: Configuration)(implicit executionContext: ExecutionContext)
  extends PlayMongoRepository[CashTransactionDates](
    collectionName = "requested-transactions-cache",
    mongoComponent = mongoComponent,
    domainFormat = CashTransactionDates.format,
    indexes = Seq(
      IndexModel(
        ascending("lastUpdated"),
        IndexOptions()
          .name("requested-transactions-cache-last-updated-index")
          .expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"),
            TimeUnit.SECONDS)
      ))) with RequestedTransactionsCache {

  override def get(id: String): Future[Option[CashTransactionDates]] = collection
      .find(equal("_id", id))
      .toSingle()
      .toFutureOption()

  override def clear(id: String): Future[Boolean] =
    collection.deleteOne(equal("_id", id))
      .toFuture()
      .map(_.wasAcknowledged())

  override def set(id: String, data: CashTransactionDates): Future[Boolean] = collection.replaceOne(
      equal("_id", id),
      data,
      ReplaceOptions().upsert(true)
    ).toFuture()
      .map(_.wasAcknowledged())
}

trait RequestedTransactionsCache {
  def get(id: String): Future[Option[CashTransactionDates]]
  def set(id: String, data: CashTransactionDates): Future[Boolean]
  def clear(id: String): Future[Boolean]
}

