package com.jahnestacado.twitterproducer.kafka

import java.util.Properties

import com.danielasfregola.twitter4s.entities.{Tweet => TweetOrig}
import com.jahnestacado.model.Tweet
import com.jahnestacado.twitterproducer.{Config, TweetToAvroMapper}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer

import scala.util.{Failure, Try}

class Producer(config: Config) extends LazyLogging {
  private val shemaRegistryUrl: String = config.kafkaProducer.schemaRegistryUrl
  private val topicName: String = config.kafkaProducer.topic
  private val boostrapServers: String = config.kafkaProducer.bootstrapServers

  private val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[io.confluent.kafka.serializers.KafkaAvroSerializer].getName)
  props.put(ProducerConfig.ACKS_CONFIG, config.kafkaProducer.acks)
  props.put("schema.registry.url", shemaRegistryUrl)

  private val producer: KafkaProducer[String, Tweet] = new KafkaProducer[String, Tweet](props)

  def send(tweet: TweetOrig): Unit = {
    val avroTweet = TweetToAvroMapper.mapTweet(tweet)
    val record: ProducerRecord[String, Tweet] = new ProducerRecord(topicName, avroTweet)
    logger.debug(s"Sending record ${record.value()}")
    Try(producer.send(record)) match {
      case Failure(ex) =>
        logger.error(s"An error occured while sending record. ${ex.getMessage}")
    }
  }
}



