package com.jahnestacado.cmcproducer.model

case class CryptoReport(
  id: Int,
  slug: String,
  circulating_supply: Long,
  total_supply : Long,
  max_supply : Option[Long],
  num_market_pairs : Long,
  cmc_rank : Int,
  last_updated: String, // Switch to a date type?
  quote: Map[String, CurrencyQuote],
)
