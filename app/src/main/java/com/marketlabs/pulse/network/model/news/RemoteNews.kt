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

/**
 * One entry from `/news/history` -- an archived day's worth of stories. The backend response
 * also carries `id` (the archive's `YYYY-MM-DD` doc name), `story_count`, `timestamp`, and
 * `last_updated`, but nothing here needs a day-level label or "as of" time -- each story inside
 * `stories` already carries its own `timestamp`, which is all the merge in
 * RemoteNewsDataSourceImpl needs. `last_updated` is deliberately left unmodeled per this repo's
 * `last_updated`/`timestamp` pairing rule (see CLAUDE.md), same as `NetworkMarketNews` above.
 */
@JsonClass(generateAdapter = true)
data class NetworkNewsHistoryDay(
    @Json(name = "stories") val stories: List<NetworkNewsArticle>? = null
)