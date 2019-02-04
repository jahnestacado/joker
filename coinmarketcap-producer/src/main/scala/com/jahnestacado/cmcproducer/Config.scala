package com.jahnestacado.cmcproducer

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

class Config() {
  private val cmcConfig = ConfigFactory.load().getConfig("coinmarketcap")
  private val kafkaProducerConfig = ConfigFactory.load().getConfig("kafka.producer")

  case class CMCConfig(token: String,
                       uri: String,
                       coinIds: List[String],
                       apiKeyHeader: String,
                       pullInterval: FiniteDuration
                      )

  case class KafkaProducer(
                            props: Properties,
                            topic: String
                          )


  private val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerConfig.getString("bootstrapServers"))
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[io.confluent.kafka.serializers.KafkaAvroSerializer].getName)
  props.put(ProducerConfig.ACKS_CONFIG, kafkaProducerConfig.getString("acks"))
  props.put("schema.registry.url", kafkaProducerConfig.getString("schemaRegistryUrl"))

  val cmc = CMCConfig(
    token = cmcConfig.getString("token"),
    uri = cmcConfig.getString("uri"),
    coinIds = cmcConfig.getStringList("coin-ids").asScala.toList,
    apiKeyHeader = cmcConfig.getString("api-key-header"),
    pullInterval = Duration.fromNanos(cmcConfig.getDuration("pull-interval").toNanos)
  )

  val kafkaProducer = KafkaProducer(
    props = props,
    topic = kafkaProducerConfig.getString("topic"),
  )

}


