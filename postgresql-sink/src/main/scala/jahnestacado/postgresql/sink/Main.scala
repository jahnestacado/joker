package jahnestacado.postgresql.sink

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.jahnestacado.cmc.model.CMCFeed
import com.jahnestacado.model.Tweet
import com.typesafe.scalalogging.LazyLogging
import jahnestacado.postgresql.sink.kafka.ConsumerStream
import jahnestacado.postgresql.sink.rdbms.{CMCFeedPersistor, ConnectionPool, TweetPersistor}

object Main extends App with LazyLogging {

  implicit val system = ActorSystem.create("postgresql-sink")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher;

  val config: Config = new Config();
  val connectionPool: ConnectionPool = new ConnectionPool(config)
  val connection = connectionPool.get()
  if (connection.isEmpty) {
    logger.error("Unable to connect to postgres. Shutting down...")
    System.exit(1)
  }

  CMCFeedPersistor.createTable(connection.get)
  TweetPersistor.createTable(connection.get)

  val cmcConsumer: ConsumerStream[CMCFeed] = new ConsumerStream[CMCFeed](connectionPool, config, CMCFeedPersistor)
  val tweetConsumer = new ConsumerStream[Tweet](connectionPool, config, TweetPersistor)

  cmcConsumer.run
  tweetConsumer.run

}

