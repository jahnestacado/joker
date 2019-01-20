package jahnestacado.postgresql.sink

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.jahnestacado.cmc.model.CMCFeed
import com.jahnestacado.model.Tweet
import jahnestacado.postgresql.sink.rdbms.{CMCFeedPersistor, Persistor, TweetPersistor}
import org.postgresql.jdbc3.Jdbc3PoolingDataSource

object Main extends App {
  implicit val system = ActorSystem.create("test")
  val config = system.settings.config.getConfig("akka.kafka.consumer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher;

  implicit val connectionPool: Jdbc3PoolingDataSource = {
    val source = new Jdbc3PoolingDataSource()
    source.setDataSourceName("A Data Source")
    source.setServerName("192.168.178.24")
    source.setPortNumber(5432)
    source.setDatabaseName("postgres")
    source.setUser("postgres")
    source.setPassword("postgres")
    source.setMaxConnections(10)
    source
  }

  def initializePostgresTable[T](persistor: Persistor[T]) = {
    val connection = connectionPool.getConnection()
    persistor.createTable(connection)
    connection.close()
  }

  Seq("cmc-feed", "tweet-stream").foreach {
    case "cmc-feed" => {
      initializePostgresTable(CMCFeedPersistor)
      new KafkaConsumer[CMCFeed]("cmc-feed", connectionPool, CMCFeedPersistor)
    }
    case "tweet-stream" => {
      initializePostgresTable(TweetPersistor)
      new KafkaConsumer[Tweet]("tweet-stream", connectionPool, TweetPersistor)
    }
  }

}

