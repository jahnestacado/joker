package com.jahnestacado.twitterproducer.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.{Keep, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.danielasfregola.twitter4s.entities.{Tweet => RawTweet}
import com.jahnestacado.model.Tweet
import com.jahnestacado.twitterproducer.Config
import com.jahnestacado.twitterproducer.model.TweetToAvroMapper
import org.apache.kafka.clients.producer._

class ProducerSourceQueue(config: Config)(implicit system: ActorSystem, mat: ActorMaterializer) {
  private val kafkaProducerConfig = config.kafkaProducer

  private val sourceQueue =
    Source.queue(kafkaProducerConfig.sourceQueueBuffer, OverflowStrategy.dropHead)
      .map((tweet: RawTweet) => {
        val avroTweet: Tweet = TweetToAvroMapper.mapTweet(tweet)

        // We do this hack in order to validate if the avro deserializer will succefully deserialize the object
        // Unfortunately the alpakka kafka producer swallows the deserialization exceptions and the stream won't resume
        // Now we deserialize prematurily using the validateAvroTweet method and in case the operation fails then
        // it will throw an exception which will cause the stream to resume
        validateAvroTweet(avroTweet)

        new ProducerRecord(kafkaProducerConfig.topic, avroTweet.id.toString, avroTweet)
      })

  private def validateAvroTweet(tweet: Tweet): Unit = kafkaProducerConfig.settings
    .valueSerializerOpt
    .foreach(tweetSerializer => tweetSerializer.serialize(kafkaProducerConfig.topic, tweet))

  def run(): SourceQueueWithComplete[RawTweet] = sourceQueue
    .toMat(Producer.plainSink(kafkaProducerConfig.settings))(Keep.left)
    .withAttributes(supervisionStrategy(resumingDecider))
    .run()

}





