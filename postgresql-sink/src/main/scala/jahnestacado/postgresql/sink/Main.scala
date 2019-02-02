package jahnestacado.postgresql.sink

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.jahnestacado.cmc.model.CMCFeed
import com.jahnestacado.model.Tweet
import com.typesafe.scalalogging.LazyLogging
import jahnestacado.postgresql.sink.rdbms.{CMCFeedPersistor, ConnectionPool, Persistor, TweetPersistor}

object Main extends App with LazyLogging {
  val config: Config = new Config();
  implicit val system = ActorSystem.create(config.kafkaConsumer.group)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher;
  val topics: Seq[String] = Seq("tweet-stream")

  val connectionPool: ConnectionPool = new ConnectionPool(config)
  val connection = connectionPool.get()

  if(connection.isEmpty) {
    logger.error("Unable to connect to postgres. Shutting down...")
    System.exit(1)
  }

  topics.foreach {
    case "cmc-feed" => {
      CMCFeedPersistor.createTable(connection.get)
      new KafkaConsumer[CMCFeed](connectionPool, config, CMCFeedPersistor)
    }
    case "tweet-stream" => {
      TweetPersistor.createTable(connection.get)
      new KafkaConsumer[Tweet]( connectionPool,config, TweetPersistor)
    }
  }

}

