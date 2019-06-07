package com.jahnestacado.cmcproducer

import akka.kafka.ProducerSettings
import com.jahnestacado.cmc.model.CMCFeed
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroSerializer}
import org.apache.kafka.common.serialization._
import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

class Config() {
  private val cmcConfig = ConfigFactory.load().getConfig("coinmarketcap")
  private val kafkaProducerConfig = ConfigFactory.load().getConfig("kafka.producer")

  case class CMCConfig(token: String,
                       uri: String,
                       coinIds: String,
                       apiKeyHeader: String,
                       pullInterval: FiniteDuration
                      )

  case class KafkaProducer(
                            settings: ProducerSettings[String, CMCFeed],
                            topic: String
                          )


  private[this] val akkaKafkaProducerConfig = ConfigFactory.load().getConfig("akka.kafka.producer")
  private[this] val producerSettings: ProducerSettings[String, CMCFeed] = {
    val kafkaAvroSerDeConfig = Map[String, Any] {
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> kafkaProducerConfig.getString("schemaRegistryUrl")
    }
    val kafkaAvroSerializer = new KafkaAvroSerializer()
    kafkaAvroSerializer.configure(kafkaAvroSerDeConfig.asJava, false)
    val cmcFeedSerializer = kafkaAvroSerializer.asInstanceOf[Serializer[CMCFeed]]

    ProducerSettings(akkaKafkaProducerConfig, new StringSerializer, cmcFeedSerializer)
  }

  val kafkaProducer = KafkaProducer(
    settings = producerSettings,
    topic = kafkaProducerConfig.getString("topic"),
  )

  val cmc = CMCConfig(
    token = cmcConfig.getString("token"),
    uri = cmcConfig.getString("uri"),
    coinIds = cmcConfig.getString("coin-ids"),
    apiKeyHeader = cmcConfig.getString("api-key-header"),
    pullInterval = Duration.fromNanos(cmcConfig.getDuration("pull-interval").toNanos)
  )

}


