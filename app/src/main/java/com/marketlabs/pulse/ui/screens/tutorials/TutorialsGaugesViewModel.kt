package com.marketlabs.pulse.ui.screens.tutorials

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.marketlabs.pulse.core.learn.IndicatorArticlesProvider
import com.marketlabs.pulse.ui.components.tutorials.Mechanism
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * `uiState` is a plain `val`, not a `StateFlow` --
 * `IndicatorArticlesProvider.get()` is a synchronous read of an already-in-memory bundle (same
 * reasoning as `MetricDetailViewModel`'s own synchronous `glossaryProvider.get(metricId)` lookup,
 * just for every entry `TutorialsGaugesCatalog` lists instead of one). Resolved once at
 * construction time.
 */
@HiltViewModel
class TutorialsGaugesViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** The one mechanism whose indicators this screen lists -- always just the route's `mechanism`
     * argument, never a combined multi-mechanism list, even when the deck screen that opened this
     * was itself showing several mechanisms via a "Show More" group. */
    val mechanism: Mechanism = Mechanism.fromRouteKey(savedStateHandle.get<String>("mechanism")) ?: Mechanism.TACTICAL_MOMENTUM

    val uiState: TutorialsGaugesUiState = run {
        val articles = IndicatorArticlesProvider.get(context)
        val categories = TutorialsGaugesCatalog.categories.filter { it.mechanism == mechanism }.mapNotNull { category ->
            val gauges = category.metricIds.mapNotNull { metricId ->
                val article = articles[metricId] ?: return@mapNotNull null
                val titleRes = TutorialsGaugesCatalog.titleResFor(metricId) ?: return@mapNotNull null
                TutorialsGaugeUi(metricId = metricId, titleRes = titleRes, subtitle = article.subtitle)
            }
            if (gauges.isEmpty()) null else TutorialsGaugeCategoryUi(titleRes = category.titleRes, introRes = category.introRes, gauges = gauges)
        }
        TutorialsGaugesUiState(categories = categories)
    }
}
