package com.jahnestacado.cmcproducer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.jahnestacado.cmcproducer.kafka.Producer
import com.jahnestacado.cmcproducer.model.{CryptoReport, CurrencyQuote, Feeds}
import com.typesafe.scalalogging.LazyLogging
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


object Main extends App with LazyLogging {
  implicit val system = ActorSystem("coinmarketcapproducer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val config: Config = new Config()
  val producer: Producer = new Producer(config)

  val httpRequest = HttpRequest(
    uri = config.cmc.uri + config.cmc.coinIds.mkString(","),
    headers = scala.collection.immutable.Seq(RawHeader(config.cmc.apiKeyHeader, config.cmc.token)
    ))

  implicit val currencyQuoteFormat = jsonFormat7(CurrencyQuote)
  implicit val cryptoReportFormat = jsonFormat9(CryptoReport)
  implicit val feedsFormat = jsonFormat1(Feeds)

  system.scheduler.schedule(1.seconds, config.cmc.pullInterval) {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(httpRequest)

    responseFuture.onComplete {
      case Success(res: HttpResponse) => {
        Unmarshal(res.entity).to[String].map(f = (json: String) => {
          val feeds = json.parseJson.convertTo[Feeds]
          feeds.data.values.foreach { feed =>
            logger.debug(s"Sending feed record $feed")
            Try(producer.send(feed)) match {
              case Failure(ex) =>
                logger.error(s"An error occured while sending feed record. ${ex.getMessage}")
              case _ => // don't care
            }
          }
        })
      }
      case Failure(err) => println(err)
    }
  }


}
