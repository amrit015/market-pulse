package com.marketlabs.pulse.ui.settings

/**
 * Flat data class per this repo convention. No loading/error fields: the switch comes from a
 * DataStore `Flow` that always emits. Defaults on to match the stored default.
 */
data class DataSyncUiState(
    val analyticsEnabled: Boolean = true
)
