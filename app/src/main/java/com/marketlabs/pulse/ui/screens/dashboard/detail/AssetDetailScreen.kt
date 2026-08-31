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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.glossary.DashboardGlossaryProvider
import com.marketlabs.pulse.storage.model.charts.ChartPoint
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.ui.components.charts.ChartRangePicker
import com.marketlabs.pulse.ui.components.charts.IntradayPeriodChart
import com.marketlabs.pulse.ui.components.charts.PeriodChart
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.enums.AssetType
import com.marketlabs.pulse.utils.verticalScrollbar

/**
 * Stateless content for the pushed asset-detail page -- moved out of the old
 * `AssetDetailBottomSheet` body verbatim (header, price row, period chart, technical breakdown,
 * SMA, glossary), only the `ModalBottomSheet` wrapper is gone. `showTechnicals` still hides the
 * technical/SMA/glossary sections for sentiment readings (Fear & Greed, Put/Call), which have no
 * such figures. The chart itself is hidden separately for futures (`asset.type == AssetType.FUTURE`)
 * -- see the chart block's own comment -- while still showing technicals/SMA for them, since those
 * figures are real for a futures contract.
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

    val showTechnicals = asset.symbol !in listOf("^VIX", "FEAR_GREED", "PUT_CALL")
    // VIX/Fear & Greed/Put-Call run on their own up/down logic that doesn't map to bullish-green/
    // bearish-red the way a price does (e.g. a rising VIX is conventionally bearish) -- their
    // charts use a fixed accent color instead of the usual direction-based read. Same set
    // showTechnicals already singles out for the same underlying reason.
    val useAccentColorForChart = !showTechnicals
    val currentPrice = String.format("%.2f", asset.price ?: 0.0)
    val previousClosePrice = String.format("%.2f", asset.previousClose ?: 0.0)

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

        if (!asset.description.isNullOrEmpty()) {
            Text(
                text = asset.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
                Text(
                    text = if (showTechnicals) "$$currentPrice" else currentPrice,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
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

            Text(
                text = stringResource(id = R.string.dashboard_technical_breakdown),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = paddingMedium)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ColoredMetricItem(
                    label = stringResource(id = R.string.dashboard_rsi),
                    value = "${asset.rsi ?: "--"} (${asset.rsiStatus ?: "N/A"})",
                    statusForColor = asset.rsiStatus ?: "",
                    paddingRight = paddingMedium
                )
                ColoredMetricItem(
                    label = stringResource(id = R.string.dashboard_macd),
                    value = asset.macdSignal ?: "N/A",
                    paddingRight = paddingMedium
                )
                ColoredMetricItem(
                    label = stringResource(id = R.string.dashboard_trend),
                    value = asset.technicalStatus ?: "N/A",
                    paddingRight = paddingMedium
                )
            }

            Spacer(modifier = Modifier.height(paddingLarge))

            Text(
                text = stringResource(id = R.string.dashboard_sma_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = paddingMedium)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem(
                    label = stringResource(id = R.string.dashboard_sma_20),
                    value = "$${asset.sma20 ?: "--"}",
                    paddingRight = paddingMedium
                )
                MetricItem(
                    label = stringResource(id = R.string.dashboard_sma_50),
                    value = "$${asset.sma50 ?: "--"}",
                    paddingRight = paddingMedium
                )
                MetricItem(
                    label = stringResource(id = R.string.dashboard_sma_200),
                    value = "$${asset.sma200 ?: "--"}",
                    paddingRight = paddingMedium
                )
            }

            Spacer(modifier = Modifier.height(paddingLarge))

            Text(
                text = stringResource(id = R.string.dashboard_technical_glossary_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = paddingMedium)
            )
            Column {
                DashboardGlossaryProvider.definitionFor(context, "RSI")?.let { GlossaryItem("RSI", it) }
                DashboardGlossaryProvider.definitionFor(context, "MACD")?.let { GlossaryItem("MACD", it) }
                DashboardGlossaryProvider.definitionFor(context, "Trend")?.let { GlossaryItem("Trend", it) }
                DashboardGlossaryProvider.definitionFor(context, "SMA")?.let { GlossaryItem("SMA", it) }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, paddingRight: Dp) {
    Column(modifier = Modifier.padding(end = paddingRight)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ColoredMetricItem(
    label: String,
    value: String,
    paddingRight: Dp,
    statusForColor: String = value
) {
    val pulseColors = LocalPulseColors.current
    val color = when (statusForColor.uppercase()) {
        "BULLISH", "EXTREME GREED", "GREED", "OVERSOLD" -> pulseColors.signalBullishText
        "BEARISH", "EXTREME FEAR", "FEAR", "OVERBOUGHT" -> pulseColors.signalBearishText
        else -> pulseColors.signalNeutralText
    }

    Column(modifier = Modifier.padding(end = paddingRight)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun GlossaryItem(term: String, definition: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                append("$term: ")
            }
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                append(definition)
            }
        },
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val mockAsset = AssetOverview(
    symbol = "SPY",
    name = "S&P 500",
    description = "Tracks the S&P 500 index, a broad benchmark of large-cap U.S. equities.",
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
