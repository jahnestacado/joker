package com.jahnestacado.cmcproducer.model
import spray.json.DefaultJsonProtocol._

trait CMCFeedJsonProtocol {
  implicit val currencyQuoteFormat = jsonFormat7(CurrencyQuote)
  implicit val cryptoReportFormat = jsonFormat9(CryptoReport)
  implicit val feedsFormat = jsonFormat1(Feeds)
}
