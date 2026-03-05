package com.marketlabs.pulse.network.model.news

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkMarketNews(
    @Json(name = "source_count") val sourceCount: Int? = null,
    @Json(name = "stories") val stories: List<NetworkNewsArticle>? = null,
    @Json(name = "timestamp") val lastUpdated: Long? = null
)

@JsonClass(generateAdapter = true)
data class NetworkNewsArticle(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "source") val source: String? = null,
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "sentiment") val sentiment: String? = null,
    @Json(name = "impact_summary") val impactSummary: String? = null,
    @Json(name = "tags") val tags: List<String>? = null,
    @Json(name = "ai_enriched") val aiEnriched: Boolean? = null
)