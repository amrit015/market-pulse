package com.marketlabs.pulse.ui.settings

import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Flat data class per this repo convention. No `isLoading`/`isRefreshing`/`errorMessage` — unlike
 * every network-backed domain's UiState, there is no failure mode here: `ThemeRepository.selectedTheme`
 * is a DataStore `Flow` that always emits (falling back to the default preset internally), so those
 * fields would never carry real information.
 */
data class SettingsUiState(
    val selectedTheme: MarketPulseTheme = MarketPulseTheme.LILAC,
    val availablePresets: List<MarketPulseTheme> = MarketPulseTheme.entries - DISABLED_PRESETS
)

/**
 * Aqua and Teal are disabled in the picker (design call, not a bug) but kept in [MarketPulseTheme]
 * so a user already persisted on one of them still renders correctly rather than falling back.
 */
private val DISABLED_PRESETS = setOf(MarketPulseTheme.AQUA, MarketPulseTheme.TEAL)
