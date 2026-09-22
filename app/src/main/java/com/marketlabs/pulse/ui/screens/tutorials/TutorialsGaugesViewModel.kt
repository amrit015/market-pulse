package com.marketlabs.pulse.ui.screens.tutorials

import com.marketlabs.pulse.ui.components.tutorials.Mechanism
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.marketlabs.pulse.core.glossary.MetricGlossaryProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * `uiState` is a plain `val`, not a `StateFlow` --
 * `MetricGlossaryProvider.getAll()` is a synchronous read of an already-in-memory bundle (see that
 * provider's own doc comment: loaded once, cached for the process lifetime), so there's nothing
 * asynchronous here to stream. Resolved once at construction time, same as `MetricDetailViewModel`'s
 * own synchronous `glossaryProvider.get(metricId)` lookup, just for every entry `TutorialsGaugesCatalog`
 * lists instead of one.
 */
@HiltViewModel
class TutorialsGaugesViewModel @Inject constructor(
    glossaryProvider: MetricGlossaryProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** The mechanisms whose indicators this screen lists: the route's `group` when opened from a
     * screen's "Show More" (the whole screen's indicators), else just its `mechanism` argument. */
    val mechanisms: List<Mechanism> = run {
        val group = savedStateHandle.get<String>("group")
            ?.split(",")?.mapNotNull { Mechanism.fromRouteKey(it) }.orEmpty()
        group.ifEmpty { listOf(Mechanism.fromRouteKey(savedStateHandle.get<String>("mechanism")) ?: Mechanism.TACTICAL_MOMENTUM) }
    }

    val uiState: TutorialsGaugesUiState = run {
        val allEntries = glossaryProvider.getAll()
        val categories = TutorialsGaugesCatalog.categories.filter { it.mechanism in mechanisms }.mapNotNull { category ->
            val gauges = category.metricIds.mapNotNull { metricId ->
                val entry = allEntries[metricId] ?: return@mapNotNull null
                val titleRes = TutorialsGaugesCatalog.titleResFor(metricId) ?: return@mapNotNull null
                TutorialsGaugeUi(titleRes = titleRes, whatItIs = entry.whatItIs)
            }
            if (gauges.isEmpty()) null else TutorialsGaugeCategoryUi(titleRes = category.titleRes, gauges = gauges)
        }
        TutorialsGaugesUiState(categories = categories)
    }
}
