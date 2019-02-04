package com.jahnestacado.cmcproducer.model

case class Feeds(data: Map[String, CryptoReport])

case class CryptoReport(
                         id: Int,
                         slug: String,
                         circulating_supply: Long,
                         total_supply: Long,
                         max_supply: Option[Long],
                         num_market_pairs: Long,
                         cmc_rank: Int,
                         last_updated: String, // Switch to a date type?
                         quote: Map[String, CurrencyQuote],
                       )

case class CurrencyQuote(
                          price: Double,
                          volume_24h: Double,
                          percent_change_1h: Double,
                          percent_change_24h: Double,
                          percent_change_7d: Double,
                          market_cap: Double,
                          last_updated: String // Switch to a date type?
                        )


