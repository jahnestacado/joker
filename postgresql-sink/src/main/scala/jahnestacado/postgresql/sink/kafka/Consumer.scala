package jahnestacado.postgresql.sink.kafka

import akka.kafka.scaladsl.{Consumer => KafkaConsumer}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Materializer
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.{RestartSource, Sink}
import com.typesafe.scalalogging.LazyLogging
import jahnestacado.postgresql.sink.Config
import jahnestacado.postgresql.sink.rdbms.{ConnectionPool, Persistor}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Consumer[T](connectionPool: ConnectionPool, config: Config, Persistor: Persistor[T])
                 (implicit executionContext: ExecutionContext, materializer: Materializer) extends LazyLogging{

  private val parallelism: Int = 2
  private val consumerSettings: ConsumerSettings[String, T] = config.kafkaConsumer.getConsumerSettings[T]
  private val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 30.seconds,
    maxBackoff = 2.minutes,
    randomFactor = 0.4
  ) { () =>
    KafkaConsumer.committablePartitionedSource(consumerSettings, Subscriptions.topics(Persistor.topic))
      .flatMapMerge(parallelism, _._2)
  }
    .mapAsync(parallelism) { msg =>
      var connection = connectionPool.get()
      logger.debug(s"Received record ${msg.record.value()}")
      Persistor
        .insert(connection.get, msg.record.value())
        .map(_ => msg.committableOffset)
    }
    .mapAsyncUnordered(parallelism)(_.commitScaladsl())
    .withAttributes(supervisionStrategy(resumingDecider))
    .runWith(Sink.ignore)
}
