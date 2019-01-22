package com.jahnestacado.twitterproducer

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging

object Main extends App with LazyLogging {
  val config: Config = new Config()
  val client = TwitterStreamingClient()
  val tracks = config.twitter.tracks
  println("tracks", tracks)
  val producer = new TweetKafkaProducer(config)

  client.filterStatuses(tracks = tracks, stall_warnings = true) {
    case tweet: Tweet => {
      println("Received tweet", tweet.text)
      producer.send(tweet)
    }
  }

}



