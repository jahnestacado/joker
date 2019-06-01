package jahnestacado.postgresql.sink

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer, KafkaAvroDeserializerConfig}
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

class Config()(implicit system: ActorSystem) {
  private val kafkaConsumerConfig = ConfigFactory.load().getConfig("kafka.consumer")
  private val postgresqlConfig = ConfigFactory.load().getConfig("postgresql")

  case class BackoffStrategyConfig(min: FiniteDuration, max: FiniteDuration, randomFactor: Double)

  case class KafkaConsumer(
                            schemaRegistryUrl: String,
                            group: String,
                            numPartitions: Int,
                            backoffStrategy: BackoffStrategyConfig
                          ) {

    // This is important in order to use the schema registry
    private val kafkaAvroSerDeConfig: Map[String, Any] = Map(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> kafkaConsumerConfig.getString("schemaRegistryUrl"),
      KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG -> "true"
    )

    def getConsumerSettings[T]: ConsumerSettings[String, T] = {
      val kafkaAvroDeserializer = new KafkaAvroDeserializer()
      kafkaAvroDeserializer.configure(kafkaAvroSerDeConfig.asJava, false)
      val deserializer = kafkaAvroDeserializer.asInstanceOf[Deserializer[T]]

      ConsumerSettings(system, new StringDeserializer, deserializer)
        .withGroupId(group)
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


  private val backoffStrategyConfig = kafkaConsumerConfig.getConfig("backoffStrategy")
  private val backoffStrategy = BackoffStrategyConfig(
    min = Duration.fromNanos(backoffStrategyConfig.getDuration("min").toNanos),
    max = Duration.fromNanos(backoffStrategyConfig.getDuration("max").toNanos),
    randomFactor = backoffStrategyConfig.getDouble("randomFactor"),
  )

  val kafkaConsumer = KafkaConsumer(
    schemaRegistryUrl = kafkaConsumerConfig.getString("schemaRegistryUrl"),
    group = kafkaConsumerConfig.getString("group"),
    numPartitions = kafkaConsumerConfig.getInt("numPartitions"),
    backoffStrategy = backoffStrategy
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


