package com.jahnestacado.cmcproducer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.jahnestacado.cmcproducer.http.HttpClient
import com.jahnestacado.cmcproducer.kafka.ProducerStream
import com.jahnestacado.cmcproducer.model.CMCFeedJsonProtocol
import com.typesafe.scalalogging.LazyLogging

object Main extends App
  with CMCFeedJsonProtocol
  with HttpClient
  with LazyLogging {
  implicit val system = ActorSystem("coinmarketcapproducer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val producerStream = new ProducerStream()
  producerStream.run();
}
