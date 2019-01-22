package jahnestacado.postgresql.sink

import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.scaladsl.Consumer
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{RestartSource, Sink}
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer, KafkaAvroDeserializerConfig}
import jahnestacado.postgresql.sink.Main.system
import jahnestacado.postgresql.sink.rdbms.Persistor
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import org.postgresql.jdbc3.Jdbc3PoolingDataSource

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class KafkaConsumer[T](topic: String, connectionPool: Jdbc3PoolingDataSource, Persistor: Persistor[T])
                      (implicit executionContext: ExecutionContext, materializer: Materializer) {
  val bootstrapServers = "192.168.178.24:29092"
  val schemaRegistryUrl = "http://192.168.178.24:8085"

  // This is important in order to use the schema registry
  val kafkaAvroSerDeConfig: Map[String, Any] = Map(
    AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl,
    KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG -> "true"
  )
  implicit val consumerSettings: ConsumerSettings[String, T] = {
    val kafkaAvroDeserializer = new KafkaAvroDeserializer()
    kafkaAvroDeserializer.configure(kafkaAvroSerDeConfig.asJava, false)
    val deserializer = kafkaAvroDeserializer.asInstanceOf[Deserializer[T]]

    ConsumerSettings(system, new StringDeserializer, deserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("postgresql-sink")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  }
  val committerSettings = CommitterSettings(system)
  val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 5.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.3
  ) { () =>
    Consumer.committableSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(2) { msg =>
        val connection = connectionPool.getConnection()
        println("Record", msg.record.value())
        Persistor.insert(connection, msg.record.value())
          .map(_ => {
            connection.close()
            msg.committableOffset
          })
      }
      .batch(max = 5, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
        batch.updated(elem)
      }
      .mapAsync(2)(_.commitScaladsl())
  }
    .runWith(Sink.ignore)
}
