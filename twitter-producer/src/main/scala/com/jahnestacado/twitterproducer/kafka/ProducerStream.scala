package com.jahnestacado.twitterproducer.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{RestartSource, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.{Done, NotUsed}
import com.danielasfregola.twitter4s.entities.{Tweet => RawTweet}
import com.danielasfregola.twitter4s.{TwitterStreamingClient, entities}
import com.jahnestacado.model.Tweet
import com.jahnestacado.twitterproducer.Config
import com.jahnestacado.twitterproducer.model.TweetToAvroMapper
import org.apache.kafka.clients.producer.ProducerRecord
import scala.concurrent.Future
import scala.concurrent.duration._

class ProducerStream private(config: Config)(implicit system: ActorSystem, mat: ActorMaterializer) {

  import ProducerStream._

  private val sourceQueue: Source[ProducerRecord[String, Tweet], SourceQueueWithComplete[entities.Tweet]] =
    Source.queue(kafkaProducerConfig.sourceQueueBuffer, OverflowStrategy.dropHead)
      .map((tweet: RawTweet) => {
        val avroTweet: Tweet = TweetToAvroMapper.mapTweet(tweet)

        // We do this hack in order to validate if the avro deserializer will succefully deserialize the object
        // Unfortunately the alpakka kafka producer swallows the deserialization exceptions and the stream won't resume
        // Now we deserialize prematurely using the validateAvroTweet method and in case the operation fails then
        // it will throw an exception which will cause the stream to resume
        validateAvroTweet(avroTweet)

        println(tweet.id)
        new ProducerRecord(kafkaProducerConfig.topic, avroTweet.id.toString, avroTweet)
      })

  private def validateAvroTweet(tweet: Tweet): Unit = kafkaProducerConfig.settings
    .valueSerializerOpt
    .foreach(tweetSerializer => tweetSerializer.serialize(kafkaProducerConfig.topic, tweet))

  private def init() = {
    val client: TwitterStreamingClient = TwitterStreamingClient()
    val prematerializedSourceQueue: (SourceQueueWithComplete[RawTweet], Source[ProducerRecord[String, Tweet], NotUsed]) = sourceQueue
      .idleTimeout(kafkaProducerConfig.streamTimeout)
      .preMaterialize()

    client.filterStatuses(tracks = keywords, stall_warnings = true)({
      case tweet: RawTweet =>
        prematerializedSourceQueue._1.offer(tweet)
    }, {
      case ex: Throwable =>
        throw ex
    })

    prematerializedSourceQueue._2
  }

}

object ProducerStream {
  val config: Config = new Config()
  val kafkaProducerConfig = config.kafkaProducer
  val keywords: List[String] = config.twitter.keywords

  def run()(implicit system: ActorSystem, mat: ActorMaterializer): Future[Done] = {
    RestartSource.withBackoff(
      minBackoff = 3.seconds,
      maxBackoff = 30.seconds,
      randomFactor = 0.3
    ) { () =>
      val producerStream: Source[ProducerRecord[String, Tweet], NotUsed] = new ProducerStream(config).init()


      producerStream
    }
      .runWith(Producer.plainSink(config.kafkaProducer.settings))
  }

}





