package jahnestacado.postgresql.sink.kafka

import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.scaladsl.{Consumer => KafkaConsumer}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{RestartSource, Sink}
import com.typesafe.scalalogging.LazyLogging
import jahnestacado.postgresql.sink.Config
import jahnestacado.postgresql.sink.rdbms.{ConnectionPool, Persistor}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Consumer[T](connectionPool: ConnectionPool, config: Config, Persistor: Persistor[T])
                 (implicit executionContext: ExecutionContext, materializer: Materializer) extends LazyLogging{

  val consumerSettings: ConsumerSettings[String, T] = config.kafkaConsumer.getConsumerSettings[T]
  val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 30.seconds,
    maxBackoff = 2.minutes,
    randomFactor = 0.4
  ) { () =>
    KafkaConsumer.committableSource(consumerSettings, Subscriptions.topics(Persistor.topic))
      .mapAsync(2) { msg =>
        var connection = connectionPool.get()
        logger.debug(s"Received record ${msg.record.value()}")
        Persistor.insert(connection.get, msg.record.value())
          .map(_ => {
            msg.committableOffset
          })
      }
      .batch(max = 10, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
        batch.updated(elem)
      }

      .mapAsync(2)(_.commitScaladsl())
  }

    .runWith(Sink.ignore)
}
