package jahnestacado.postgresql.sink

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.jahnestacado.cmc.model.CMCFeed
import com.jahnestacado.model.Tweet
import jahnestacado.postgresql.sink.rdbms.{CMCFeedPersistor, ConnectionPool, Persistor, TweetPersistor}

object Main extends App {
  val config: Config = new Config();
  implicit val system = ActorSystem.create(config.kafkaConsumer.group)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher;
  val topics: Seq[String] = Seq("cmc-feed", "tweet-stream")

  val connectionPool: ConnectionPool = new ConnectionPool(config)
  val connection = connectionPool.get()

  topics.foreach {
    case "cmc-feed" => {
      CMCFeedPersistor.createTable(connection)
      new KafkaConsumer[CMCFeed]("cmc-feed", connectionPool, CMCFeedPersistor)
    }
    case "tweet-stream" => {
      TweetPersistor.createTable(connection)
      new KafkaConsumer[Tweet]("tweet-stream", connectionPool, TweetPersistor)
    }
  }

}

