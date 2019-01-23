package jahnestacado.postgresql.sink

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.util.Properties

class Config() {
  private val kafkaConsumerConfig = ConfigFactory.load().getConfig("kafka-consumer")
  private val postgresqlConfig = ConfigFactory.load().getConfig("postgresql")

  case class KafkaConsumer(
                            bootstrapServers: String,
                            schemaRegistryUrl: String,
                            group: String,
                          )

  case class Postgresql(
                         host: String,
                         port: Int,
                         user: String,
                         password: String,
                         database: String,
                         maxConnections: Int
                       )


  val kafkaConsumer = KafkaConsumer(
    bootstrapServers = Properties.envOrElse("KAFKA_CONSUMER_BOOTSTRAP_SERVERS", kafkaConsumerConfig.getString("bootstrapServers")),
    schemaRegistryUrl = Properties.envOrElse("KAFKA_SCHEMA_REGISTRY_URL", kafkaConsumerConfig.getString("schemaRegistryUrl")),
    group = Properties.envOrElse("KAFKA_CONSUMER_GROUP", kafkaConsumerConfig.getString("group"))
  )

  val postgresql = Postgresql(
    host = Properties.envOrElse("POSTGRESQL_HOST", postgresqlConfig.getString("host")),
    port = Properties.envOrElse("POSTGRESQL_PORT", postgresqlConfig.getString("port")).toInt,
    user = Properties.envOrElse("POSTGRESQL_USER", postgresqlConfig.getString("user")),
    password = Properties.envOrElse("POSTGRESQL_PASSWORD", postgresqlConfig.getString("password")),
    database = Properties.envOrElse("POSTGRESQL_DATABASE", postgresqlConfig.getString("database")),
    maxConnections = Properties.envOrElse("POSTGRESQL_MAX_CONNECTIONS", postgresqlConfig.getString("max-connections")).toInt,
  )

}


