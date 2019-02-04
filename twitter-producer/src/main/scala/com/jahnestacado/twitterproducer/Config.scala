package com.jahnestacado.twitterproducer

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer

import scala.collection.JavaConverters._

class Config() {
  private val twitterConfig = ConfigFactory.load().getConfig("twitter")
  private val kafkaProducerConfig = ConfigFactory.load().getConfig("kafka.producer")

  case class TwitterConfig(
                            apiKey: String,
                            apiSecret: String,
                            accessKey: String,
                            accessSecret: String,
                            tracks: List[String],
                          )

  case class KafkaProducer(
                            props: Properties,
                            topic: String,
                          )

  val twitter = TwitterConfig(
    apiKey = twitterConfig.getString("consumer.key"),
    apiSecret = twitterConfig.getString("consumer.secret"),
    accessKey = twitterConfig.getString("access.key"),
    accessSecret = twitterConfig.getString("access.secret"),
    tracks = twitterConfig.getStringList("tracks").asScala.toList,
  )

  private val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerConfig.getString("bootstrapServers"))
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[io.confluent.kafka.serializers.KafkaAvroSerializer].getName)
  props.put(ProducerConfig.ACKS_CONFIG, kafkaProducerConfig.getString("acks"))
  props.put("schema.registry.url", kafkaProducerConfig.getString("schemaRegistryUrl"))

  val kafkaProducer = KafkaProducer(
    props = props,
    topic = kafkaProducerConfig.getString("topic"),
  )

}


