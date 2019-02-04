package com.jahnestacado.twitterproducer.kafka

import com.danielasfregola.twitter4s.entities.{Tweet => TweetOrig}
import com.jahnestacado.model.Tweet
import com.jahnestacado.twitterproducer.Config
import com.jahnestacado.twitterproducer.model.TweetToAvroMapper
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._

import scala.util.{Failure, Try}

class Producer(config: Config) extends LazyLogging {
  private val producer: KafkaProducer[String, Tweet] = new KafkaProducer[String, Tweet](config.kafkaProducer.props)

  def send(tweet: TweetOrig): Unit = {
    val avroTweet = TweetToAvroMapper.mapTweet(tweet)
    val record: ProducerRecord[String, Tweet] = new ProducerRecord(config.kafkaProducer.topic, avroTweet)
    logger.debug(s"Sending record ${record.value()}")
    Try(producer.send(record)) match {
      case Failure(ex) =>
        logger.error(s"An error occured while sending record. ${ex.getMessage}")
    }
  }
}



