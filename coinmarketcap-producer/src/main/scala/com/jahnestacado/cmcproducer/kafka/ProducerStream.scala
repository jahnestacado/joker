package com.jahnestacado.cmcproducer.kafka

import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.kafka.scaladsl.Producer
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.{ActorMaterializer, Attributes}
import com.jahnestacado.cmc.model.CMCFeed
import com.jahnestacado.cmcproducer.Config
import com.jahnestacado.cmcproducer.Main.request
import com.jahnestacado.cmcproducer.model.{CMCFeedJsonProtocol, CMCToAvroMapper, CryptoReport, Feeds}
import org.apache.kafka.clients.producer.ProducerRecord
import spray.json._

import scala.concurrent.duration._

class ProducerStream(config: Config)(implicit system: ActorSystem, mat: ActorMaterializer) extends CMCFeedJsonProtocol{

  private val uri: String = config.cmc.uri + config.cmc.coinIds
  private val headers: Seq[RawHeader] = Seq(RawHeader(config.cmc.apiKeyHeader, config.cmc.token))
  private val parallelism: Int = 1
  private val streamLogConfig: Attributes = Attributes.logLevels(
    onElement = Attributes.LogLevels.Info,
    onFinish = Attributes.LogLevels.Error
  )

  private val source: Source[ProducerRecord[String, CMCFeed], Cancellable] = Source
    .tick(1.second, config.cmc.pullInterval, "pull")
    .mapAsync(parallelism)(_ => request(uri, headers))
    .mapAsync(parallelism)(res => Unmarshal(res.entity).to[String])
    .flatMapConcat(json =>
  Source.fromIterator[CryptoReport](() => json.parseJson.convertTo[Feeds].data.values.toIterator)
  )
    .log("Sending CMCFeed to Kafka -----> ", _.toJson)
    .map[ProducerRecord[String, CMCFeed]](feed => {
    val avroFeed = CMCToAvroMapper.mapFeed(feed)
    new ProducerRecord(config.kafkaProducer.topic, avroFeed)
  })
    .withAttributes(supervisionStrategy(resumingDecider))
    .addAttributes(streamLogConfig)

  def run(): Cancellable = source
    .toMat(Producer.plainSink(config.kafkaProducer.settings))(Keep.left)
    .run()

}


