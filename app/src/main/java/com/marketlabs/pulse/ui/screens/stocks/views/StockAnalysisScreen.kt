package com.marketlabs.pulse.ui.screens.stocks.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.storage.model.stocks.StockPreview
import com.marketlabs.pulse.ui.common.UiError
import com.marketlabs.pulse.ui.components.DisclaimerFooter
import com.marketlabs.pulse.ui.components.PulseLoadingIndicator
import com.marketlabs.pulse.ui.screens.stocks.components.StockPreviewCard
import com.marketlabs.pulse.ui.screens.stocks.isIndexOrEtf
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * The Analysis tab's own page tabs -- Favorites (locally starred symbols), Stocks, Indices/ETF, in
 * that display order. Favoriting a symbol doesn't remove it from Stocks/Indices-ETF; Favorites is
 * a cross-cutting view over the same tracked list, not a separate bucket a symbol moves into.
 * Mirrors `InsightsTab`'s shape exactly (`labelRes` enum, `PulseTabRow` + swipeable
 * `HorizontalPager`, one `LazyColumn` + `LazyListState` per tab so scroll position survives
 * switching tabs and back). `STOCKS` -- not the first-listed `FAVORITES` -- is the default landed
 * on; `StockAnalysisViewModel` initializes `_selectedTabIndex` to `STOCKS.ordinal` rather than a
 * bare `0` for exactly this reason (display order and default tab are deliberately independent).
 */
enum class StockAnalysisTab(val labelRes: Int) {
    FAVORITES(R.string.stock_analysis_tab_favorites),
    STOCKS(R.string.stock_analysis_tab_stocks),
    INDICES_ETF(R.string.stock_analysis_tab_indices_etf)
}

/**
 * The Analysis tab's loaded-data content -- one page per `StockAnalysisTab`, each with its own
 * header ("N TRACKED · ANALYZED AS OF ...", `N` scoped to that tab's own filtered list, not the
 * overall tracked count) followed by one `StockPreviewCard` per symbol in it. Mirrors `NewsScreen`'s
 * role exactly: the Route owns the loading/error/empty state machine (same split
 * `IndicatorsRoute`/`NewsRoute` already use), this composable only ever renders the successful,
 * data-present case.
 */
@Composable
fun StockAnalysisScreen(
    previews: List<StockPreview>,
    analyzedAsOf: String?,
    isEquityOpen: Boolean,
    pagerState: PagerState,
    favoriteSymbols: Set<String>,
    clickedDeepDiveSymbols: Set<String> = emptySet(),
    onCardClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDeepDiveClick: (String) -> Unit = {},
    onTechnicalSetupClick: (String) -> Unit = {},
    scaffoldPadding: PaddingValues,
    getIntradayStream: (String) -> Flow<IntradaySeries?> = { emptyFlow() },
    modifier: Modifier = Modifier
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    // 💡 `top` is just breathing room under the pinned PulseTabRow now, not `scaffoldPadding`'s top
    // component -- the global top bar's own inset is already consumed once by that pinned tab row
    // (rendered above this screen in `StockAnalysisRoute`), so adding it again here would double
    // the gap between the top bar and the tab row's own content. Same fix `InsightsScreen` already
    // applies for the identical reason.
    val contentPadding = PaddingValues(
        top = paddingLarge,
        bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
        start = paddingLarge,
        end = paddingLarge
    )
    val lazyListStates = remember { List(StockAnalysisTab.entries.size) { LazyListState() } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val tab = StockAnalysisTab.entries[page]
            val tabPreviews = when (tab) {
                StockAnalysisTab.FAVORITES -> previews.filter { it.symbol in favoriteSymbols }
                StockAnalysisTab.STOCKS -> previews.filterNot { it.isIndexOrEtf() }
                StockAnalysisTab.INDICES_ETF -> previews.filter { it.isIndexOrEtf() }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListStates[tab.ordinal],
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(paddingLarge)
            ) {
                item {
                    StockAnalysisHeader(trackedCount = tabPreviews.size, analyzedAsOf = analyzedAsOf)
                }
                if (tabPreviews.isEmpty()) {
                    item { StockAnalysisTabEmptyState(tab) }
                }
                items(tabPreviews, key = { it.symbol }) { preview ->
                    val isFavorite = preview.symbol in favoriteSymbols
                    val hasDeepDive = preview.deepAnalysisDate != null
                    val isDeepDiveFlashing = isFavorite && hasDeepDive && (preview.symbol !in clickedDeepDiveSymbols)

                    StockPreviewCard(
                        preview = preview,
                        onClick = { onCardClick(preview.symbol) },
                        isEquityOpen = isEquityOpen,
                        isFavorite = isFavorite,
                        onFavoriteClick = { onToggleFavorite(preview.symbol) },
                        onTechnicalSetupClick = { preview.technicalSetup?.let(onTechnicalSetupClick) },
                        isDeepDiveFlashing = isDeepDiveFlashing,
                        onDeepDiveClick = { onDeepDiveClick(preview.symbol) },
                        intradayStream = getIntradayStream(preview.symbol),
                        modifier = Modifier.animateItem()
                    )
                }
                item { DisclaimerFooter(showAiDisclosure = true) }
            }
        }
    }
}

@Composable
private fun StockAnalysisHeader(trackedCount: Int, analyzedAsOf: String?) {
    Column {
        if (analyzedAsOf != null) {
            Text(
                text = stringResource(
                    id = R.string.stock_analysis_subtitle,
                    trackedCount,
                    stringResource(id = R.string.analyzed_at, analyzedAsOf)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = LocalPulseColors.current.onSurfaceMuted,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
            )
        }
    }
}

/**
 * Same "nothing here" treatment `InsightsTabEmptyState` uses for its own per-tab empty case --
 * centered, muted, `LazyItemScope` receiver so `fillParentMaxSize()` sizes to the LazyColumn's own
 * viewport instead of shrink-wrapping to this one item.
 */
@Composable
private fun LazyItemScope.StockAnalysisTabEmptyState(tab: StockAnalysisTab) {
    val messageRes = when (tab) {
        StockAnalysisTab.FAVORITES -> R.string.stock_analysis_favorites_empty_message
        StockAnalysisTab.STOCKS -> R.string.stock_analysis_empty_message
        StockAnalysisTab.INDICES_ETF -> R.string.stock_analysis_indices_etf_empty_message
    }
    Box(
        modifier = Modifier.fillParentMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        )
    }
}

@Composable
private fun errorMessageFor(error: UiError): String = when (error) {
    is UiError.Network -> stringResource(id = R.string.stock_analysis_error_network)
    is UiError.Server -> stringResource(id = R.string.stock_analysis_error_server)
    is UiError.Unknown -> stringResource(id = R.string.stock_analysis_error_unknown)
}

@Composable
fun StockAnalysisErrorState(error: UiError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessageFor(error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large))
        ) {
            Text(stringResource(id = R.string.action_retry))
        }
    }
}

@Composable
fun StockAnalysisEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.stock_analysis_empty_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val mockPreviews = listOf(
    StockPreview(symbol = "AMZN", lastSyncedTimestamp = 0L, name = "Amazon.com, Inc.", price = 274.48, changePercent = 0.82, plainRead = "Amazon's stock sits at \$274.48, trading 16.04% above its 200-day average."),
    StockPreview(symbol = "NVDA", lastSyncedTimestamp = 0L, name = "NVIDIA Corporation", price = 178.42, changePercent = 2.31, plainRead = "NVIDIA trades at \$178.42 on 2.1x average volume as it clears its prior high.")
)

@Preview(name = "Loaded — Light", showBackground = true)
@Composable
private fun PreviewStockAnalysisScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        StockAnalysisScreen(
            previews = mockPreviews,
            analyzedAsOf = "Aug 07, 6:15 PM",
            isEquityOpen = true,
            pagerState = rememberPagerState { StockAnalysisTab.entries.size },
            favoriteSymbols = setOf("NVDA"),
            onCardClick = {},
            onToggleFavorite = {},
            scaffoldPadding = PaddingValues()
        )
    }
}

@Preview(name = "Loaded — Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewStockAnalysisScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        StockAnalysisScreen(
            previews = mockPreviews,
            analyzedAsOf = "Aug 07, 6:15 PM",
            isEquityOpen = true,
            pagerState = rememberPagerState { StockAnalysisTab.entries.size },
            favoriteSymbols = setOf("NVDA"),
            onCardClick = {},
            onToggleFavorite = {},
            scaffoldPadding = PaddingValues()
        )
    }
}

@Preview(name = "Loading — Light", showBackground = true)
@Composable
private fun PreviewStockAnalysisLoadingLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PulseLoadingIndicator()
    }
}

@Preview(name = "Loading — Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewStockAnalysisLoadingDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PulseLoadingIndicator()
    }
}

@Preview(name = "Error — Light", showBackground = true)
@Composable
private fun PreviewStockAnalysisErrorLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        StockAnalysisErrorState(error = UiError.Network(), onRetry = {})
    }
}

@Preview(name = "Error — Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewStockAnalysisErrorDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        StockAnalysisErrorState(error = UiError.Network(), onRetry = {})
    }
}

@Preview(name = "Empty — Light", showBackground = true)
@Composable
private fun PreviewStockAnalysisEmptyLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        StockAnalysisEmptyState()
    }
}

@Preview(name = "Empty — Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewStockAnalysisEmptyDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        StockAnalysisEmptyState()
    }
}
