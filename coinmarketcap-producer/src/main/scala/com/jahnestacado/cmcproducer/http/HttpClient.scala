package com.jahnestacado.cmcproducer.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}

import scala.concurrent.Future

trait HttpClient {
  def request(uri: String, headers: Seq[HttpHeader])(implicit system: ActorSystem): Future[HttpResponse] = {
    val httpRequest = HttpRequest(
      uri = uri,
      headers = headers.toList
    )

    Http().singleRequest(httpRequest)
  }
}
