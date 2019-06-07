package com.jahnestacado.twitterproducer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.SourceQueueWithComplete
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{Tweet => RawTweet}
import com.jahnestacado.twitterproducer.kafka.ProducerSourceQueue
import com.typesafe.scalalogging.LazyLogging

object Main extends App with LazyLogging {

  {
    val config: Config = new Config()
    implicit val system = ActorSystem("twitter-producer")
    implicit val mat = ActorMaterializer()
    val producerSourceQueue: SourceQueueWithComplete[RawTweet] = new ProducerSourceQueue(config).run()
    val keywords: List[String] = config.twitter.keywords

    val client: TwitterStreamingClient = TwitterStreamingClient()
    client.filterStatuses(tracks = keywords, stall_warnings = true)({
      case tweet: RawTweet =>
        producerSourceQueue.offer(tweet)
    }, {
      case ex: Throwable => logger.error(ex.getMessage)
    })
  }

}



