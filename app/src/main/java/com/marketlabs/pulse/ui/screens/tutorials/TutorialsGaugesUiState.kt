package com.marketlabs.pulse.ui.screens.tutorials

import androidx.annotation.StringRes

/** Flat data classes per this repo convention -- no `isLoading`/`errorMessage`, same reasoning as
 * `SettingsUiState`: the underlying source (`IndicatorArticlesProvider`'s in-memory bundle) is a
 * synchronous local-asset read with no failure mode worth surfacing. */
data class TutorialsGaugesUiState(
    val categories: List<TutorialsGaugeCategoryUi> = emptyList()
)

data class TutorialsGaugeCategoryUi(
    @param:StringRes val titleRes: Int,
    @param:StringRes val introRes: Int,
    val gauges: List<TutorialsGaugeUi>
)

/** [metricId] drives navigation into that indicator's full `indicator_articles.json` article --
 * this screen no longer renders a definition inline (see `TutorialsGaugesScreen`'s own doc comment). */
data class TutorialsGaugeUi(
    val metricId: String,
    @param:StringRes val titleRes: Int,
    val subtitle: String
)
