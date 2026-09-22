package com.marketlabs.pulse.ui.settings

/**
 * Flat data class per this repo convention. No loading/error fields — same reason as
 * `SettingsUiState`: the toggles come from a DataStore `Flow` that always emits. Both default off
 * because pushes are opt-in.
 */
data class NotificationsUiState(
    val dailySummaryEnabled: Boolean = false,
    val stockAnalysisEnabled: Boolean = false
) {
    val anyEnabled: Boolean get() = dailySummaryEnabled || stockAnalysisEnabled
}
