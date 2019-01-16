package com.jahnestacado.cmcproducer

import com.jahnestacado.cmc.model.CMCFeed
import com.jahnestacado.cmcproducer.model.CryptoReport


object CMCToAvroMapper {
  val currency =  "USD"
  def mapFeed(report: CryptoReport): CMCFeed = {
    val feed: CMCFeed = new CMCFeed(
      id = report.id,
      name = report.slug,
      circulating_supply = report.circulating_supply,
      total_supply = report.total_supply,
      max_supply = report.max_supply,
      num_market_pairs = report.num_market_pairs,
      cmc_rank = report.cmc_rank,
      last_updated = report.last_updated,
      currency = currency, //get this from report
      price = report.quote.get(currency).get.price,
      volume_24h = report.quote.get(currency).get.volume_24h,
      percent_change_1h = report.quote.get(currency).get.percent_change_1h,
      percent_change_7h = report.quote.get(currency).get.percent_change_7d,
      percent_change_24h = report.quote.get(currency).get.percent_change_24h,
      market_cap = report.quote.get(currency).get.market_cap,
    )

    return feed
  }
}
