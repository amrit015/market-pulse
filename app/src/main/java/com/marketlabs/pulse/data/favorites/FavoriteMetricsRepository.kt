package com.marketlabs.pulse.data.favorites

import kotlinx.coroutines.flow.Flow

/**
 * Lighter than a full three-tier domain repository -- same shape `FavoriteStocksRepository` uses
 * for exactly this reason (a plain local device preference with nothing to sync yet), and the same
 * storage-agnostic interface boundary: no caller sees anything DataStore-shaped, only
 * `Flow<Set<String>>` + a toggle, so swapping [FavoriteMetricsRepositoryImpl] for an account-backed
 * implementation later needs no change to `IndicatorsViewModel`/`MetricDetailViewModel` -- see
 * `FavoriteStocksRepository`'s own doc comment for the full reasoning.
 */
interface FavoriteMetricsRepository {
    /** Emits an empty set until the reader has ever favorited a metric; persisted after. */
    val favoriteMetricIds: Flow<Set<String>>

    /** Adds [metricId] to the favorite set if absent, removes it otherwise. */
    suspend fun toggleFavorite(metricId: String)
}
