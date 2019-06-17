package com.jahnestacado.twitterproducer

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.jahnestacado.twitterproducer.kafka.ProducerStream
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main extends App with LazyLogging {

  {
    implicit val system = ActorSystem("twitter-producer")
    implicit val mat = ActorMaterializer()
    implicit val context = system.dispatcher

    val producerStreamFuture: Future[Done] = ProducerStream.run()
    producerStreamFuture.onComplete({
      case Success(_) => logger.info("ProducerStream completed")
      case Failure(ex) => logger.info(s"ProducerStream completed with failure. Cause: ${ex.getCause}. Message: ${ex.getMessage}")
    })

  }

}



