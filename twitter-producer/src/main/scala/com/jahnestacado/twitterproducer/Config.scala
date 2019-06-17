package com.jahnestacado.twitterproducer

import akka.kafka.ProducerSettings
import com.jahnestacado.model.Tweet
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroSerializer}
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

class Config() {
  private val twitterConfig = ConfigFactory.load().getConfig("twitter")
  private val kafkaProducerConfig = ConfigFactory.load().getConfig("kafka.producer")

  case class TwitterConfig(
                            apiKey: String,
                            apiSecret: String,
                            accessKey: String,
                            accessSecret: String,
                            keywords: List[String],
                          )

  case class KafkaProducer(
                            settings: ProducerSettings[String, Tweet],
                            topic: String,
                            sourceQueueBuffer: Int,
                            streamTimeout: FiniteDuration
                          )

  private[this] val akkaKafkaProducerConfig = ConfigFactory.load().getConfig("akka.kafka.producer")
  private[this] val producerSettings: ProducerSettings[String, Tweet] = {
    val kafkaAvroSerDeConfig = Map[String, Any] {
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> kafkaProducerConfig.getString("schemaRegistryUrl")
    }
    val kafkaAvroSerializer = new KafkaAvroSerializer()
    kafkaAvroSerializer.configure(kafkaAvroSerDeConfig.asJava, false)
    val tweetSerializer = kafkaAvroSerializer.asInstanceOf[Serializer[Tweet]]

    ProducerSettings(akkaKafkaProducerConfig, new StringSerializer, tweetSerializer)
  }

  val kafkaProducer = KafkaProducer(
    settings = producerSettings,
    topic = kafkaProducerConfig.getString("topic"),
    sourceQueueBuffer = kafkaProducerConfig.getInt("source-queue-buffer"),
    streamTimeout = Duration.fromNanos(kafkaProducerConfig.getDuration("stream-timeout").toNanos)
  )

  val twitter = TwitterConfig(
    apiKey = twitterConfig.getString("consumer.key"),
    apiSecret = twitterConfig.getString("consumer.secret"),
    accessKey = twitterConfig.getString("access.key"),
    accessSecret = twitterConfig.getString("access.secret"),
    keywords = twitterConfig.getString("keywords").split(",").toList,
  )

}


