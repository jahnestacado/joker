package jahnestacado.postgresql.sink

import akka.kafka.ConsumerSettings
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer, KafkaAvroDeserializerConfig}
import jahnestacado.postgresql.sink.Main.system
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}

import scala.collection.JavaConverters._


class Config() {
  private val kafkaConsumerConfig = ConfigFactory.load().getConfig("kafka-consumer")
  private val postgresqlConfig = ConfigFactory.load().getConfig("postgresql")

  case class KafkaConsumer(
                            bootstrapServers: String,
                            schemaRegistryUrl: String,
                            group: String,
                          ) {
    // This is important in order to use the schema registry
    private val kafkaAvroSerDeConfig: Map[String, Any] = Map(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl,
      KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG -> "true"
    )

    def getConsumerSettings[T]: ConsumerSettings[String, T] = {
      val kafkaAvroDeserializer = new KafkaAvroDeserializer()
      kafkaAvroDeserializer.configure(kafkaAvroSerDeConfig.asJava, false)
      val deserializer = kafkaAvroDeserializer.asInstanceOf[Deserializer[T]]

      ConsumerSettings(system, new StringDeserializer, deserializer)
        .withBootstrapServers(bootstrapServers)
        .withGroupId(group)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
  }

  case class Postgresql(
                         host: String,
                         port: Int,
                         user: String,
                         password: String,
                         database: String,
                         maxConnections: Int
                       )


  val kafkaConsumer = KafkaConsumer(
    bootstrapServers = kafkaConsumerConfig.getString("bootstrapServers"),
    schemaRegistryUrl = kafkaConsumerConfig.getString("schemaRegistryUrl"),
    group = kafkaConsumerConfig.getString("group")
  )

  val postgresql = Postgresql(
    host = postgresqlConfig.getString("host"),
    port = postgresqlConfig.getString("port").toInt,
    user = postgresqlConfig.getString("user"),
    password = postgresqlConfig.getString("password"),
    database = postgresqlConfig.getString("database"),
    maxConnections = postgresqlConfig.getString("max-connections").toInt,
  )


}


