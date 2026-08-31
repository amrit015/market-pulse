package com.marketlabs.pulse.data.insights

import kotlinx.coroutines.flow.Flow

/**
 * Lighter than a full three-tier domain repository (no Remote/Local split, no Room caching) --
 * same shape `ThemeRepository` uses for exactly this reason (`docs/architecture/overview.md` authorizes it
 * for a domain that doesn't need the full split). Backs the Positioning/Posture first-time
 * explainer's persisted dismissal state (2026-08-27 interpretive-layer spec) -- there's no
 * "seen it once" precedent anywhere else in the app to extend, so this introduces one new
 * DataStore instance using the same technology/idiom `ThemeRepository` already established,
 * not a second storage mechanism.
 */
interface InsightsUiStateRepository {
    /** Emits `false` until the user has ever dismissed the Positioning intro card; persisted after. */
    val isPositioningIntroDismissed: Flow<Boolean>

    /** Emits `false` until the user has ever dismissed the Posture intro card; persisted after. */
    val isPostureIntroDismissed: Flow<Boolean>

    suspend fun dismissPositioningIntro()
    suspend fun dismissPostureIntro()
}
