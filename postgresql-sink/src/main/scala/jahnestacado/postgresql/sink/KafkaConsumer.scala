package jahnestacado.postgresql.sink

import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.scaladsl.Consumer
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{RestartSource, Sink}
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer, KafkaAvroDeserializerConfig}
import jahnestacado.postgresql.sink.Main.system
import jahnestacado.postgresql.sink.rdbms.{ConnectionPool, Persistor}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class KafkaConsumer[T](topic: String, connectionPool: ConnectionPool, Persistor: Persistor[T], config: Config)
                      (implicit executionContext: ExecutionContext, materializer: Materializer) {

  val consumerSettings: ConsumerSettings[String, T] = config.kafkaConsumer.getConsumerSettings[T]
  val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 30.seconds,
    maxBackoff = 2.minutes,
    randomFactor = 0.4
  ) { () =>
    Consumer.committableSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(2) { msg =>
        var connection = connectionPool.get()
        println("Record", msg.record.value())

        Persistor.insert(connection, msg.record.value())
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
