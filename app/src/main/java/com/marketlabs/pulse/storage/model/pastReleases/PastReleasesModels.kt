package com.marketlabs.pulse.storage.model.pastReleases

/**
 * The app's "Past Releases" list -- one slot per standard economic release, always the most
 * recent confirmed reading for that release, not a time series.
 */
data class PastReleases(
    val lastSyncedTimestamp: Long? = null,
    val releases: Map<String, PastRelease>? = null
)

/**
 * A single standard release's latest confirmed reading. [id] is the release's stable key
 * (e.g. "cpi_mm") -- carried over from the response map's key, not a field the backend sends
 * on the value itself.
 */
data class PastRelease(
    val id: String,
    val label: String? = null,
    val feedTitle: String? = null,
    val date: String? = null,
    val estimate: String? = null,
    val previous: String? = null,
    val actual: String? = null,
    val actualConfirmed: Boolean? = null,
    val postReleaseImpact: String? = null,
    val resolvedAt: Long? = null
)
