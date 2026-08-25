package com.marketlabs.pulse.ui.screens.dashboard.detail

import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries

/**
 * Drives the pushed asset-detail page (replaces the old `AssetDetailBottomSheet` -- every
 * dashboard tile tap now pushes a real destination instead of opening a sheet, matching
 * `StockDetailUiState`'s shape). `asset` cross-references the same live `DashboardRepository`
 * stream the Overview tab itself reads, filtered to this one symbol -- there's no separate
 * "asset detail" backend fetch, this domain has no data of its own beyond what the tile already
 * had. `chartSeries`/`selectedChartRange` are this screen's own on-demand `ChartsRepository` read,
 * same pattern `StockDetailUiState` uses for the Technicals tab's period chart.
 *
 * `intradaySeries` backs the `ChartRange.ONE_DAY` option -- only populated (and only offered, via
 * `availableChartRanges`) for the ~23-symbol set `DashboardIntradayEligibility` allows; VIX,
 * futures, and sentiment tiles have no live tick feed behind `/intraday/:symbol`, so their picker
 * never shows 1D at all rather than showing it and rendering permanently empty.
 *
 * `availableChartRanges` also filters out any of 6M/YTD/1Y that the asset's actual history can't
 * distinguish from a shorter range (`ChartRange.isCoveredByHistory`, learned via a background
 * `ONE_YEAR` fetch in `AssetDetailViewModel.prefetchHistoryCoverage()`) -- both filters apply
 * together, not just the intraday one.
 */
data class AssetDetailUiState(
    val symbol: String,
    val asset: AssetOverview? = null,
    val chartSeries: ChartSeries? = null,
    val selectedChartRange: ChartRange = ChartRange.FIVE_DAY,
    val isChartLoading: Boolean = false,
    val intradaySeries: IntradaySeries? = null,
    val availableChartRanges: List<ChartRange> = ChartRange.entries
)
