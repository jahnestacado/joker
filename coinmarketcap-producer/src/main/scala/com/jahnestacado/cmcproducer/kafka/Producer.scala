package com.jahnestacado.cmcproducer.kafka

import com.jahnestacado.cmc.model.CMCFeed
import com.jahnestacado.cmcproducer.Config
import com.jahnestacado.cmcproducer.model.{CMCToAvroMapper, CryptoReport}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.util.{Failure, Try}

class Producer(config: Config) extends LazyLogging {
  private val producer = new KafkaProducer[String, CMCFeed](config.kafkaProducer.props)

  def send(feed: CryptoReport): Unit = {
    val avroFeed = CMCToAvroMapper.mapFeed(feed)
    val record: ProducerRecord[String, CMCFeed] = new ProducerRecord(config.kafkaProducer.topic, avroFeed)
    Try(producer.send(record)) match {
      case Failure(ex) =>
        throw ex
      case _ => // don't care
    }
  }

}

