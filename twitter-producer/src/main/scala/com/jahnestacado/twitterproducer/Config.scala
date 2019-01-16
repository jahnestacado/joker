package com.jahnestacado.twitterproducer

import com.typesafe.config.ConfigFactory
import scala.util.Properties
import scala.collection.JavaConverters._

class Config() {
  private val twitterConfig = ConfigFactory.load().getConfig("twitter")
  private val kafkaProducerConfig = ConfigFactory.load().getConfig("kafka-producer")

  case class TwitterConfig(
                            apiKey: String,
                            apiSecret: String,
                            accessKey: String,
                            accessSecret: String,
                            tracks: List[String]
                          )

  case class KafkaProducer(
                            bootstrapServers: String,
                            schemaRegistryUrl: String,
                            topic: String,
                            acks: String
                          )

  val twitter = TwitterConfig(
    apiKey = Properties.envOrElse("TWITTER_API_KEY", twitterConfig.getString("consumer.key")),
    apiSecret = Properties.envOrElse("TWITTER_API_SECRET", twitterConfig.getString("consumer.secret")),
    accessKey = Properties.envOrElse("TWITTER_ACCESS_KEY", twitterConfig.getString("access.key")),
    accessSecret = Properties.envOrElse("TWITTER_ACCESS_SECRET", twitterConfig.getString("access.secret")),
    tracks = if (!Properties.envOrElse("TWITTER_TRACKS", "").isEmpty) {
      sys.env("TWITTER_TRACKS").split(",").toList
    } else twitterConfig.getStringList("tracks").asScala.toList
  )

  val kafkaProducer = KafkaProducer(
    bootstrapServers = Properties.envOrElse("KAFKA_PRODUCER_BOOTSTRAP_SERVERS", kafkaProducerConfig.getString("bootstrapServers")),
    schemaRegistryUrl = Properties.envOrElse("KAFKA_PRODUCER_SCHEMA_REGISTRY_URL", kafkaProducerConfig.getString("schemaRegistryUrl")),
    topic = Properties.envOrElse("KAFKA_PRODUCER_TOPIC", kafkaProducerConfig.getString("topic")),
    acks = Properties.envOrElse("KAFKA_PRODUCER_ACKS_CONFIG", kafkaProducerConfig.getString("acks")),
  )

}


