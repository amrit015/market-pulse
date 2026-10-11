package com.marketlabs.pulse.data.privacy

import kotlinx.coroutines.flow.Flow

/**
 * Whether the user lets the app send anonymous usage analytics. A thin wrapper over a local
 * DataStore value (same reasoning as `LegalRepository`), with no Remote/Room tiers. Crash
 * reporting is deliberately not governed by this setting.
 */
interface AnalyticsConsentRepository {

    /** Emits `true` until the user turns sharing off (opt-out model). */
    val enabled: Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)
}
