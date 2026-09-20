package com.marketlabs.pulse.ui.screens.dashboard.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.glossary.AssetDescriptionProvider
import com.marketlabs.pulse.core.glossary.DashboardGlossaryProvider
import com.marketlabs.pulse.storage.model.charts.ChartPoint
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.ui.components.DisclaimerFooter
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.bottomSheet.GlossaryEntry
import com.marketlabs.pulse.ui.components.bottomSheet.StockAnalysisGlossaryBottomSheet
import com.marketlabs.pulse.ui.components.charts.ChartRangePicker
import com.marketlabs.pulse.ui.components.charts.IntradayPeriodChart
import com.marketlabs.pulse.ui.components.charts.PeriodChart
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.components.widgets.DirectionalChangePill
import com.marketlabs.pulse.ui.components.widgets.animateFlashColor
import com.marketlabs.pulse.ui.screens.stocks.detail.DataCardTitleWithInfo
import com.marketlabs.pulse.ui.screens.stocks.detail.StatGrid
import com.marketlabs.pulse.ui.screens.stocks.detail.StatItem
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.PulseColors
import com.marketlabs.pulse.utils.enums.AssetType
import com.marketlabs.pulse.utils.verticalScrollbar
import kotlin.math.abs

/**
 * Stateless content for the pushed asset-detail page -- moved out of the old
 * `AssetDetailBottomSheet` body verbatim originally, then restyled 2026-09 onto the same
 * `PulseCard`/`StatGrid`/`DataCardTitleWithInfo` shapes Stock Detail's equivalent sections
 * (`HeadlineMetricsStrip`) already use, rather than the loose `Text`/`Row` blocks the bottom-sheet
 * move had left untouched. `showTechnicals` still hides the technical/SMA sections for sentiment
 * readings (Fear & Greed, Put/Call), which have no such figures. The chart itself is hidden
 * separately for futures (`asset.type == AssetType.FUTURE`) -- see the chart block's own comment --
 * while still showing technicals/SMA for them, since those figures are real for a futures contract.
 */
@Composable
fun AssetDetailScreen(
    asset: AssetOverview,
    chartSeries: ChartSeries?,
    selectedChartRange: ChartRange,
    isChartLoading: Boolean,
    intradaySeries: IntradaySeries?,
    availableChartRanges: List<ChartRange>,
    onChartRangeSelected: (ChartRange) -> Unit,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val pulseColors = LocalPulseColors.current

    val showTechnicals = asset.symbol !in listOf("^VIX", "FEAR_GREED", "PUT_CALL")
    // VIX/Fear & Greed/Put-Call run on their own up/down logic that doesn't map to bullish-green/
    // bearish-red the way a price does (e.g. a rising VIX is conventionally bearish) -- their
    // charts use a fixed accent color instead of the usual direction-based read. Same set
    // showTechnicals already singles out for the same underlying reason, so their price row stays
    // undirected too (plain text, no pill) rather than guessing a wrong-reading color/arrow.
    val useAccentColorForChart = !showTechnicals
    val currentPrice = String.format("%.2f", asset.price ?: 0.0)
    val previousClosePrice = String.format("%.2f", asset.previousClose ?: 0.0)

    val changePercent = asset.changePercent
    val direction = if (showTechnicals && changePercent != null) {
        when {
            changePercent > 0 -> ChangeDirection.UP
            changePercent < 0 -> ChangeDirection.DOWN
            else -> ChangeDirection.FLAT
        }
    } else {
        null
    }
    val priceTextColor = when (direction) {
        ChangeDirection.UP -> pulseColors.signalBullishText
        ChangeDirection.DOWN -> pulseColors.signalBearishText
        ChangeDirection.FLAT -> pulseColors.signalNeutralText
        null -> MaterialTheme.colorScheme.onSurface
    }
    val pillColor = when (direction) {
        ChangeDirection.UP -> pulseColors.signalBullishPill
        ChangeDirection.DOWN -> pulseColors.signalBearishPill
        else -> pulseColors.signalNeutralPill
    }

    var showTechnicalGlossary by remember { mutableStateOf(false) }
    var showSmaGlossary by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScrollbar(state = scrollState, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            .verticalScroll(scrollState)
            .padding(
                start = paddingLarge,
                end = paddingLarge,
                top = scaffoldPadding.calculateTopPadding() + paddingLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (showTechnicals) {
                Text(
                    text = asset.symbol.replace("=F", ""),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = asset.name ?: "",
                style = if (showTechnicals) {
                    MaterialTheme.typography.bodyLarge
                } else {
                    MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(paddingLarge))

        val description = AssetDescriptionProvider.descriptionFor(context, asset.symbol)
        if (!description.isNullOrEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(paddingLarge))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(end = paddingMedium)) {
                Text(
                    text = stringResource(id = R.string.dashboard_current_price),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (showTechnicals) "$$currentPrice" else currentPrice,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = animateFlashColor(
                            value = asset.price,
                            flashColor = priceTextColor,
                            restingColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (direction != null && changePercent != null) {
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                        DirectionalChangePill(
                            changeText = "${String.format("%.2f", abs(changePercent))}%",
                            direction = direction,
                            pillColor = pillColor,
                            contentColor = priceTextColor
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(id = R.string.dashboard_prev_close),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (showTechnicals) "$$previousClosePrice" else previousClosePrice,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Period chart (5D/1M/6M/YTD/1Y) -- every dashboard asset class has a real `market_charts`
        // doc (unlike the intraday sparkline, which only exists for the ~23-symbol live-price
        // set), so this renders for VIX/commodities/sentiment too, not just the equity-like assets
        // that keep their technicals below. `PeriodChart` handles the loading/empty states itself
        // at a fixed height -- see its own doc comment. Futures (ES=F/YM=F/NQ=F) are excluded
        // entirely by product decision -- no chart at all for them, not even the period chart
        // every other asset class keeps.
        if (asset.type != AssetType.FUTURE) {
            Spacer(modifier = Modifier.height(paddingLarge))
            if (selectedChartRange == ChartRange.ONE_DAY) {
                IntradayPeriodChart(
                    points = intradaySeries?.points.orEmpty(),
                    previousClose = intradaySeries?.previousClose,
                    date = intradaySeries?.date,
                    isLoading = isChartLoading,
                    useAccentColor = useAccentColorForChart,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PeriodChart(
                    points = chartSeries?.points.orEmpty(),
                    isLoading = isChartLoading,
                    currentPrice = asset.price,
                    useAccentColor = useAccentColorForChart,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(paddingMedium))
            ChartRangePicker(
                selectedRange = selectedChartRange,
                onRangeSelected = onChartRangeSelected,
                availableRanges = availableChartRanges
            )
        }

        // Hide everything below this point for Sentiment/VIX metrics.
        if (showTechnicals) {
            Spacer(modifier = Modifier.height(paddingLarge))

            // 💡 Whichever SMA sits closest to the current price is highlighted in the theme's
            // primary color -- the one moving-average price is actually hugging right now, at a
            // glance, rather than making the reader compare three numbers themselves.
            val smaDistances = listOfNotNull(
                asset.sma20?.let { SmaPeriod.TWENTY to abs((asset.price ?: 0.0) - it) },
                asset.sma50?.let { SmaPeriod.FIFTY to abs((asset.price ?: 0.0) - it) },
                asset.sma200?.let { SmaPeriod.TWO_HUNDRED to abs((asset.price ?: 0.0) - it) }
            )
            val nearestSma = if (asset.price != null) smaDistances.minByOrNull { it.second }?.first else null

            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(paddingLarge)) {
                    DataCardTitleWithInfo(
                        title = stringResource(id = R.string.dashboard_sma_title),
                        onInfoClick = { showSmaGlossary = true }
                    )
                    Spacer(modifier = Modifier.height(paddingLarge))
                    StatGrid(
                        stats = listOf(
                            StatItem(
                                value = "$${asset.sma20 ?: "--"}",
                                label = stringResource(id = R.string.dashboard_sma_20),
                                valueColor = if (nearestSma == SmaPeriod.TWENTY) MaterialTheme.colorScheme.primary else null
                            ),
                            StatItem(
                                value = "$${asset.sma50 ?: "--"}",
                                label = stringResource(id = R.string.dashboard_sma_50),
                                valueColor = if (nearestSma == SmaPeriod.FIFTY) MaterialTheme.colorScheme.primary else null
                            ),
                            StatItem(
                                value = "$${asset.sma200 ?: "--"}",
                                label = stringResource(id = R.string.dashboard_sma_200),
                                valueColor = if (nearestSma == SmaPeriod.TWO_HUNDRED) MaterialTheme.colorScheme.primary else null
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(paddingLarge))

            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(paddingLarge)) {
                    DataCardTitleWithInfo(
                        title = stringResource(id = R.string.dashboard_technical_breakdown),
                        onInfoClick = { showTechnicalGlossary = true }
                    )
                    Spacer(modifier = Modifier.height(paddingLarge))
                    StatGrid(
                        stats = listOfNotNull(
                            StatItem(
                                value = "${asset.rsi ?: "--"} (${asset.rsiStatus ?: "N/A"})",
                                label = stringResource(id = R.string.dashboard_rsi),
                                valueColor = signalColorFor(pulseColors, asset.rsiStatus ?: "")
                            ),
                            StatItem(
                                value = asset.macdSignal ?: "N/A",
                                label = stringResource(id = R.string.dashboard_macd),
                                valueColor = signalColorFor(pulseColors, asset.macdSignal ?: "")
                            ),
                            // 💡 Hidden entirely, not shown as "N/A" -- unlike RSI/MACD (which the
                            // backend basically always populates), Trend genuinely has no reading
                            // for some symbols, and an always-empty "Trend: N/A" cell read as
                            // broken data rather than "this doesn't apply here."
                            asset.technicalStatus?.let {
                                StatItem(
                                    value = it,
                                    label = stringResource(id = R.string.dashboard_trend),
                                    valueColor = signalColorFor(pulseColors, it)
                                )
                            }
                        )
                    )
                }
            }
        }

        // 💡 Same padding_extra_large gap GlossaryDetailScreen/MetricDetailScreen/Dashboard's own
        // footer already use before this text -- this screen had none, reading as visibly tighter
        // against the content above than every other screen's footer.
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
        DisclaimerFooter()
    }

    if (showTechnicalGlossary) {
        val title = stringResource(id = R.string.dashboard_technical_breakdown)
        val rsiLabel = stringResource(id = R.string.dashboard_rsi)
        val macdLabel = stringResource(id = R.string.dashboard_macd)
        val trendLabel = stringResource(id = R.string.dashboard_trend)
        val entries = listOfNotNull(
            DashboardGlossaryProvider.definitionFor(context, "RSI")?.let { GlossaryEntry(label = rsiLabel, term = "RSI", definitionOverride = it) },
            DashboardGlossaryProvider.definitionFor(context, "MACD")?.let { GlossaryEntry(label = macdLabel, term = "MACD", definitionOverride = it) },
            // 💡 Only when the Trend stat itself is actually showing -- see that StatItem's own
            // comment on why a symbol with no technicalStatus hides the cell entirely.
            if (asset.technicalStatus != null) {
                DashboardGlossaryProvider.definitionFor(context, "Trend")?.let { GlossaryEntry(label = trendLabel, term = "Trend", definitionOverride = it) }
            } else {
                null
            }
        )
        StockAnalysisGlossaryBottomSheet(title = title, entries = entries, onDismiss = { showTechnicalGlossary = false })
    }

    if (showSmaGlossary) {
        val title = stringResource(id = R.string.dashboard_sma_title)
        val entries = listOfNotNull(
            DashboardGlossaryProvider.definitionFor(context, "SMA")?.let { GlossaryEntry(label = title, term = "SMA", definitionOverride = it) }
        )
        StockAnalysisGlossaryBottomSheet(title = title, entries = entries, onDismiss = { showSmaGlossary = false })
    }
}

/** RSI/MACD/Trend's shared bullish/bearish/neutral read -- same classification `SpeedometerGauge`/`VixFullWidthCard` use for their own status strings. */
private enum class SmaPeriod { TWENTY, FIFTY, TWO_HUNDRED }

private fun signalColorFor(pulseColors: PulseColors, status: String): Color = when (status.uppercase()) {
    "BULLISH", "EXTREME GREED", "GREED", "OVERSOLD" -> pulseColors.signalBullishText
    "BEARISH", "EXTREME FEAR", "FEAR", "OVERBOUGHT" -> pulseColors.signalBearishText
    else -> pulseColors.signalNeutralText
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val mockAsset = AssetOverview(
    symbol = "SPY",
    name = "S&P 500",
    type = AssetType.EQUITY,
    price = 764.25,
    previousClose = 765.68,
    changePercent = -0.19,
    rsi = 54.2,
    rsiStatus = "NEUTRAL",
    macdSignal = "BULLISH",
    technicalStatus = "UPTREND",
    sma20 = 758.10,
    sma50 = 742.30,
    sma200 = 690.50
)

private val mockChartPoints = listOf(
    ChartPoint("2026-08-18", 758.0), ChartPoint("2026-08-19", 761.5), ChartPoint("2026-08-20", 759.0),
    ChartPoint("2026-08-21", 763.0), ChartPoint("2026-08-22", 766.5), ChartPoint("2026-08-24", 764.25)
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewAssetDetailScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        AssetDetailScreen(
            asset = mockAsset,
            chartSeries = ChartSeries(symbol = "SPY", range = ChartRange.FIVE_DAY, points = mockChartPoints, lastSyncedTimestamp = 0L),
            selectedChartRange = ChartRange.FIVE_DAY,
            isChartLoading = false,
            intradaySeries = null,
            availableChartRanges = ChartRange.entries,
            onChartRangeSelected = {},
            scaffoldPadding = PaddingValues()
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewAssetDetailScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        AssetDetailScreen(
            asset = mockAsset,
            chartSeries = ChartSeries(symbol = "SPY", range = ChartRange.FIVE_DAY, points = mockChartPoints, lastSyncedTimestamp = 0L),
            selectedChartRange = ChartRange.FIVE_DAY,
            isChartLoading = false,
            intradaySeries = null,
            availableChartRanges = ChartRange.entries,
            onChartRangeSelected = {},
            scaffoldPadding = PaddingValues()
        )
    }
}
