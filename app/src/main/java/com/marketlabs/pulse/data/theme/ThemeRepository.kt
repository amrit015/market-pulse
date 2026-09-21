package com.marketlabs.pulse.data.theme

import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlinx.coroutines.flow.Flow

/**
 * 💡 THOUGHT PROCESS:
 * Lighter than a full three-tier domain repository (no Remote/Local split, no Room caching) —
 * talking straight to its store is acceptable and already precedented for a domain that
 * doesn't need the full split. Still kept as an `Impl → Interface` pair per this repo's DI
 * convention, even for something this small,
 * so `ThemeModule.kt`'s `@Provides` binding stays uniform with every other domain's module.
 */
interface ThemeRepository {

    /** Emits PLUM (light-mode device) or LILAC (dark-mode device) until the user has ever picked one; persisted after. */
    val selectedTheme: Flow<MarketPulseTheme>

    suspend fun setTheme(theme: MarketPulseTheme)
}
