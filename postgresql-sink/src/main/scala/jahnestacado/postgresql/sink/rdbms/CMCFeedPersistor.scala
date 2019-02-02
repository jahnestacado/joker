package jahnestacado.postgresql.sink.rdbms

import java.sql.Connection

import akka.Done
import com.jahnestacado.cmc.model.CMCFeed
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}


object CMCFeedPersistor extends Persistor[CMCFeed] with LazyLogging {
  private val tableName = "CMCFeeds"

  val topic: String = "cmc-feed"

  def createTable(connection: Connection) = {
    logger.info(s"Initializing table $tableName")
    val preparedStatement = connection.prepareStatement(
      s"""CREATE TABLE IF NOT EXISTS ${tableName} (
      insertion_time timestamptz DEFAULT current_timestamp,
      id integer,
      name text,
      circulating_supply bigint,
      total_supply bigint,
      max_supply bigint,
      num_market_pairs bigint,
      cmc_rank int,
      last_updated text,
      currency text,
      price double precision,
      volume_24h double precision,
      percent_change_1h double precision,
      percent_change_24h double precision,
      percent_change_7h double precision,
      market_cap double precision
    );""")
    preparedStatement.executeUpdate()
    preparedStatement.close()
  }

  def insert(connection: Connection, feed: CMCFeed)(implicit executionContext: ExecutionContext): Future[Done] = Future {
    val statement = connection.prepareStatement(
      s"""INSERT INTO ${tableName} (
         id,
         name,
         circulating_supply,
         total_supply,
         max_supply,
         num_market_pairs,
         cmc_rank,
         last_updated,
         currency,
         price,
         volume_24h ,
         percent_change_1h,
         percent_change_24h,
         percent_change_7h,
         market_cap
        ) VALUES (${"?, " * 14} ?);""")

    statement.setInt(1, feed.id)
    statement.setString(2, feed.name)
    statement.setLong(3, feed.circulating_supply)
    statement.setLong(4, feed.total_supply)
    statement.setLong(5, feed.max_supply.getOrElse(0))
    statement.setLong(6, feed.num_market_pairs)
    statement.setInt(7, feed.cmc_rank)
    statement.setString(8, feed.last_updated)
    statement.setString(9, feed.currency)
    statement.setDouble(10, feed.price)
    statement.setDouble(11, feed.volume_24h)
    statement.setDouble(12, feed.percent_change_1h)
    statement.setDouble(13, feed.percent_change_7h)
    statement.setDouble(14, feed.percent_change_24h)
    statement.setDouble(15, feed.market_cap)

    statement.executeUpdate()
    statement.close()
    Done
  }

}
