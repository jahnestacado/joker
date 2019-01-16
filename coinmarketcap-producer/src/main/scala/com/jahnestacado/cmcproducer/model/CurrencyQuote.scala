package com.jahnestacado.cmcproducer.model

case class CurrencyQuote(
  price: Double,
  volume_24h : Double,
  percent_change_1h: Double,
  percent_change_24h : Double,
  percent_change_7d: Double,
  market_cap: Double,
  last_updated: String // Switch to a date type?
)
