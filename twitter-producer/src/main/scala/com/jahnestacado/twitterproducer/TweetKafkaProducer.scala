package com.jahnestacado.twitterproducer

import java.util.Properties

import com.danielasfregola.twitter4s.entities.{Tweet => TweetOrig}
import com.jahnestacado.model.Tweet
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer

class TweetKafkaProducer(config: Config) {
  private val shemaRegistryUrl: String = config.kafkaProducer.schemaRegistryUrl
  private val topicName: String = config.kafkaProducer.topic
  private val boostrapServers: String = config.kafkaProducer.bootstrapServers
  private val props = new Properties()

  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[io.confluent.kafka.serializers.KafkaAvroSerializer].getName)
  props.put(ProducerConfig.ACKS_CONFIG, config.kafkaProducer.acks)
  props.put("schema.registry.url", shemaRegistryUrl)

  private val producer = new KafkaProducer[String, Tweet](props)

  class OnDone extends Callback {

    override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
      println(s"onCompletion - ${metadata}, ${exception.getMessage}")
      println(exception)
      println("")
    }

  }

  def send(tweet: TweetOrig): Unit = {
    val avroTweet = TweetToAvroMapper.mapTweet(tweet)
    val record: ProducerRecord[String, Tweet] = new ProducerRecord(topicName, avroTweet)
    producer.send(record, new OnDone)
  }


}

