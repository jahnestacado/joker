package com.jahnestacado.cmcproducer

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Properties

class Config() {
  private val cmcConfig = ConfigFactory.load().getConfig("coinmarketcap")
  private val kafkaProducerConfig = ConfigFactory.load().getConfig("kafka-producer")

  case class CMCConfig(token: String,
                       uri: String,
                       coinIds: List[String],
                       apiKeyHeader: String,
                       pullInterval: FiniteDuration
                      )

  case class KafkaProducer(
                            bootstrapServers: String,
                            schemaRegistryUrl: String,
                            topic: String
                          )

  val cmc = CMCConfig(
    token = Properties.envOrElse("CMC_TOKEN", cmcConfig.getString("token")),
    uri = Properties.envOrElse("CMC_URL", cmcConfig.getString("uri")),
    coinIds = if (!Properties.envOrElse("CMC_COIN_IDS", "").isEmpty) {
      sys.env("CMC_COIN_IDS").split(",").toList
    } else cmcConfig.getStringList("coin-ids").asScala.toList,
    apiKeyHeader = cmcConfig.getString("api-key-header"),
    pullInterval = Duration.fromNanos(cmcConfig.getDuration("pull-interval").toNanos)
  )

  val kafkaProducer = KafkaProducer(
    bootstrapServers = Properties.envOrElse("KAFKA_PRODUCER_BOOTSTRAP_SERVERS", kafkaProducerConfig.getString("bootstrapServers")),
    schemaRegistryUrl = Properties.envOrElse("KAFKA_PRODUCER_SCHEMA_REGISTRY_URL", kafkaProducerConfig.getString("schemaRegistryUrl")),
    topic = Properties.envOrElse("KAFKA_PRODUCER_TOPIC", kafkaProducerConfig.getString("topic")),
  )

}


