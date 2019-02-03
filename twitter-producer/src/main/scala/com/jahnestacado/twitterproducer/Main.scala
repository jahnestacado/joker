package com.jahnestacado.twitterproducer

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.jahnestacado.twitterproducer.kafka.Producer
import com.typesafe.scalalogging.LazyLogging

object Main extends App with LazyLogging {

  val config: Config = new Config()
  val client = TwitterStreamingClient()
  val tracks = config.twitter.tracks

  val producer = new Producer(config)
  client.filterStatuses(tracks = tracks, stall_warnings = true) {
    case tweet: Tweet => {
      producer.send(tweet)
    }
  }

}



