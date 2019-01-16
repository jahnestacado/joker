package com.jahnestacado.cmcproducer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.jahnestacado.cmcproducer.model.{CryptoReport, CurrencyQuote, Root}
import com.jahnestacado.coinmarketcapproducer.CMCKafkaProducer
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object Main extends App {
  implicit val system = ActorSystem("coinmarketcapproducer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val config: Config = new Config()
  val producer: CMCKafkaProducer = new CMCKafkaProducer(config)

  val httpRequest = HttpRequest(
    uri = config.cmc.uri + config.cmc.coinIds.mkString(","),
    headers = scala.collection.immutable.Seq(RawHeader(config.cmc.apiKeyHeader, config.cmc.token)
    ))

  system.scheduler.schedule(1.seconds, config.cmc.pullInterval) {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(httpRequest)

    responseFuture.onComplete {
      case Success(res: HttpResponse) => {
        implicit val currencyQuoteFormat = jsonFormat7(CurrencyQuote)
        implicit val cryptoReportFormat = jsonFormat9(CryptoReport)
        implicit val rootFormat = jsonFormat1(Root)
        Unmarshal(res.entity).to[String].map(f = (json: String) => {
          val feeds = json.parseJson.convertTo[Root]
          feeds.data.values.foreach(producer.send(_))

        })
      }
      case Failure(err) => println(err)
    }
  }


}
