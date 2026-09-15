package com.marketlabs.pulse.data.favorites

import kotlinx.coroutines.flow.Flow

/**
 * Lighter than a full three-tier domain repository (no Remote/Local split, no Room caching) --
 * same shape `ThemeRepository`/`InsightsUiStateRepository` use for exactly this reason: a plain
 * local device preference with nothing to sync (yet). Backs the Analysis tab's star toggle on each
 * `StockPreviewCard`.
 *
 * Deliberately storage-agnostic at this interface -- every caller (`StockAnalysisViewModel`,
 * `StockDetailViewModel`) only ever sees `Flow<Set<String>>` + a toggle, never anything
 * DataStore-shaped. Favoriting is local-only today because there's no signed-in account to scope it
 * to; once one exists, swapping [FavoriteStocksRepositoryImpl] for an account-backed implementation
 * (reading/writing a per-user document instead of a local file) needs no change on either caller --
 * whichever implementation is bound in `FavoriteStocksModule` is the only thing that changes.
 */
interface FavoriteStocksRepository {
    /** Emits an empty set until the reader has ever favorited a symbol; persisted after. */
    val favoriteSymbols: Flow<Set<String>>

    /** Adds [symbol] to the favorite set if absent, removes it otherwise. */
    suspend fun toggleFavorite(symbol: String)
}
