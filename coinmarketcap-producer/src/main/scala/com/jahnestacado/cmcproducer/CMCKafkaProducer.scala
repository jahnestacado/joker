package com.jahnestacado.coinmarketcapproducer

import java.util.Properties

import com.jahnestacado.cmc.model.CMCFeed
import com.jahnestacado.cmcproducer.model.CryptoReport
import com.jahnestacado.cmcproducer.{CMCToAvroMapper, Config}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.util.{Failure, Try}

class CMCKafkaProducer(config: Config) extends LazyLogging{
  private val shemaRegistryUrl: String = config.kafkaProducer.schemaRegistryUrl
  private val topicName: String = config.kafkaProducer.topic
  private val boostrapServers: String =  config.kafkaProducer.bootstrapServers
  private val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[io.confluent.kafka.serializers.KafkaAvroSerializer].getName)
  props.put(ProducerConfig.ACKS_CONFIG, "1")
  props.put("schema.registry.url", shemaRegistryUrl)

  private val producer = new KafkaProducer[String, CMCFeed](props)

  def send(feed: CryptoReport): Unit = {
    val avroFeed = CMCToAvroMapper.mapFeed(feed)
    val record : ProducerRecord[String, CMCFeed] =  new ProducerRecord(topicName, avroFeed)
    Try(producer.send(record)) match {
      case Failure(ex) =>
        throw ex
      case _ => // don't care
    }
  }

}

