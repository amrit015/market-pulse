package com.marketlabs.pulse.ui.screens.indicators.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.glossary.MetricGlossaryProvider
import com.marketlabs.pulse.core.indicators.IndicatorsRepository
import com.marketlabs.pulse.core.indicators.MetricHistoryRepository
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.ui.screens.indicators.detail.MetricDetailViewModel.Companion.ARG_METRIC_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the pushed metric-detail page, `SavedStateHandle`-scoped by metric id -- same shape as
 * `AssetDetailViewModel`, the precedent for turning a tap into its own nav-graph-scoped ViewModel
 * instance. `metric` cross-references `IndicatorsRepository`'s already-running stream (the same
 * one the Indicators tab reads) rather than a separate fetch -- `MarketIndicators` already holds
 * every metric across all 4 pillars, so this page shows whatever the tapped card last showed and
 * updates live if the Indicators tab's own listener is still active. `glossaryEntry` is a plain
 * synchronous lookup into `MetricGlossaryProvider`'s in-memory bundle, same as
 * `IndicatorsViewModel.glossaryEntryFor` used to provide for the now-deleted `IndicatorDetailSheet`.
 * `historyPoints` is the one genuinely new fetch this page needs -- `MetricHistoryRepository`'s
 * on-demand read, fetched in `onStart()` same as every other detail screen's period chart.
 */
@HiltViewModel
class MetricDetailViewModel @Inject constructor(
    private val indicatorsRepository: IndicatorsRepository,
    private val glossaryProvider: MetricGlossaryProvider,
    private val metricHistoryRepository: MetricHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val metricId: String = checkNotNull(savedStateHandle[ARG_METRIC_ID]) {
        "MetricDetailViewModel requires a non-null \"$ARG_METRIC_ID\" nav argument"
    }

    private val _isHistoryLoading = MutableStateFlow(false)

    private val matchingMetric: Flow<DomainUnifiedMetric?> = indicatorsRepository.getIndicatorsStream()
        .map { data -> data?.findMetric(metricId) }

    val uiState: StateFlow<MetricDetailUiState> = combine(
        matchingMetric,
        metricHistoryRepository.getHistoryStream(metricId),
        _isHistoryLoading
    ) { metric, historySeries, isHistoryLoading ->
        MetricDetailUiState(
            metricId = metricId,
            metric = metric,
            glossaryEntry = glossaryProvider.get(metricId),
            historyPoints = historySeries?.points.orEmpty(),
            isHistoryLoading = isHistoryLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MetricDetailUiState(metricId = metricId)
    )

    /** Called by the UI when the screen becomes visible. */
    fun onStart() {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            // Deliberately silent on failure -- the chart is a supporting element on this page,
            // not its main content, same reasoning as StockDetailViewModel's chart fetch.
            metricHistoryRepository.refreshHistory(metricId)
            _isHistoryLoading.value = false
        }
    }

    /** No shared listener to stop -- `indicatorsRepository`'s stream is owned by the Indicators tab, not this page. */
    fun onStop() = Unit

    companion object {
        /** Must match the nav argument name in the `metricDetail/{metricId}` route. */
        const val ARG_METRIC_ID = "metricId"
    }
}

private fun MarketIndicators.findMetric(metricId: String): DomainUnifiedMetric? =
    listOfNotNull(tacticalMomentum, systemicRisk, valuation, macroVitals)
        .flatMap { it.metrics }
        .firstOrNull { it.id == metricId }
