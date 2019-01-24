package com.jahnestacado.twitterproducer

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging

object Main extends App with LazyLogging {
  LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger].setLevel(Level.TRACE)

  val config: Config = new Config()
  val client = TwitterStreamingClient()
  val tracks = config.twitter.tracks
  println("tracks", tracks)
  val producer = new TweetKafkaProducer(config)
  val t0 = System.currentTimeMillis()
  client.filterStatuses(tracks = tracks, stall_warnings = true) {
    case tweet: Tweet => {
      println(s"Received tweet-- ${System.currentTimeMillis() - t0}")
      producer.send(tweet)
    }
  }

}



