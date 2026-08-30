package com.marketlabs.pulse.ui.screens.dashboard.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.intraday.DashboardIntradayEligibility
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import com.marketlabs.pulse.storage.model.news.NewsArticle
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.bottomSheet.MarketGlossaryBottomSheet
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.components.widgets.DirectionalChangePill
import com.marketlabs.pulse.ui.components.widgets.PutCallHorizontalBar
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.components.widgets.SparklineChart
import com.marketlabs.pulse.ui.components.widgets.SpeedometerGauge
import com.marketlabs.pulse.ui.components.widgets.VixFullWidthCard
import com.marketlabs.pulse.ui.screens.news.views.NewsPreviewSection
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.enums.AssetType
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// News preview section below added with Claude Code assistance.

@Composable
fun DashboardScreen(
    marketState: MarketState?,
    assets: List<AssetOverview?>,
    scaffoldPadding: PaddingValues,
    newsArticles: List<NewsArticle>,
    onNewsArticleClick: (String) -> Unit,
    onNavigateToNews: () -> Unit,
    onAssetClick: (String) -> Unit = {},
    getIntradayStream: (String) -> Flow<IntradaySeries?> = { emptyFlow() }
) {
    val scrollState = rememberScrollState()

    val paddingExtraLarge = dimensionResource(id = R.dimen.padding_extra_large)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    val isEquityOpen = marketState?.isEquityOpen == true
    val isFuturesOpen = marketState?.isFuturesOpen == true

    val sentimentAssets =
        assets.filter { it?.symbol == "^VIX" || it?.symbol == "FEAR_GREED" || it?.symbol == "PUT_CALL" }

    val equitySortOrder = listOf("SPY", "DIA", "QQQ", "RSP", "IWM", "MAGS")
    val cryptoCommoditySortOrder = listOf("BTC-USD", "ETH-USD", "GC=F", "SI=F", "CL=F", "HG=F")
    val futureSortOrder = listOf("ES=F", "YM=F", "NQ=F")

    // Fallback filter ensures we safely catch sectors even if AssetType enum doesn't explicitly have SECTOR defined yet
    val sectorSymbols =
        listOf("XLK", "XLC", "XLY", "XLF", "XLI", "XLE", "XLV", "XLP", "XLU", "XLB", "XLRE")

    val futureAssets = assets
        .filter { it?.type == AssetType.FUTURE }
        .sortedBy { asset ->
            val index = futureSortOrder.indexOf(asset?.symbol)
            if (index == -1) Int.MAX_VALUE else index
        }

    val equityAssets = assets
        .filter { it?.type == AssetType.EQUITY }
        .sortedBy { asset ->
            val index = equitySortOrder.indexOf(asset?.symbol)
            if (index == -1) Int.MAX_VALUE else index
        }

    val otherAssets = assets
        .filter { it?.type == AssetType.COMMODITY || it?.type == AssetType.CRYPTO }
        .sortedBy { asset ->
            val index = cryptoCommoditySortOrder.indexOf(asset?.symbol)
            if (index == -1) Int.MAX_VALUE else index
        }

    val sectorAssets = assets
        .filter { it?.type?.name == "SECTOR" || it?.symbol in sectorSymbols }

    var selectedRegimeForGlossary by remember { mutableStateOf<String?>(null) }

    // 💡 Top padding uses `scaffoldPadding`'s top component (the Scaffold's own measurement of
    // the top bar's real rendered height) instead of the raw status bar inset alone -- the raw
    // inset only accounts for the system status bar, not the app's own top bar sitting below it,
    // so content used to start underneath the top bar rather than below it.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(
                top = scaffoldPadding.calculateTopPadding() + paddingExtraLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingExtraLarge,
                start = paddingLarge,
                end = paddingLarge
            ),
        verticalArrangement = Arrangement.spacedBy(paddingExtraLarge)
    ) {

        TechnicalSummaryCard(
            summaryText = marketState?.technicalSummary,
            timestamp = marketState?.technicalSummaryTimestamp,
            isEquityOpen = isEquityOpen
        )

        if (sentimentAssets.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.dashboard_section_sentiment),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = LocalPulseColors.current.accentPrimary,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                )

                val vixAsset = sentimentAssets.find { it?.symbol == "^VIX" }
                if (vixAsset != null) {
                    VixFullWidthCard(asset = vixAsset, onClick = { onAssetClick(vixAsset.symbol) })
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                }

                val greedAsset = sentimentAssets.find { it?.symbol == "FEAR_GREED" }
                val putCallAsset = sentimentAssets.find { it?.symbol == "PUT_CALL" }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
                ) {
                    if (greedAsset != null) {
                        AssetCard(
                            asset = greedAsset,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            customVisual = {
                                SpeedometerGauge(
                                    score = greedAsset.price ?: 50.0,
                                    previousScore = greedAsset.previousClose,
                                    status = greedAsset.rsiStatus
                                )
                            },
                            onClick = { onAssetClick(greedAsset.symbol) }
                        )
                    }

                    if (putCallAsset != null) {
                        AssetCard(
                            asset = putCallAsset,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            customVisual = {
                                PutCallHorizontalBar(
                                    ratio = putCallAsset.price ?: 1.0,
                                    change = putCallAsset.changePercent,
                                    status = putCallAsset.rsiStatus
                                )
                            },
                            onClick = { onAssetClick(putCallAsset.symbol) }
                        )
                    } else if (greedAsset != null) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // --- SECTION 2: Futures ---
        if (!isEquityOpen && isFuturesOpen && futureAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_futures),
                items = futureAssets,
                onAssetClick = { onAssetClick(it.symbol) },
                getIntradayStream = getIntradayStream,
                columnNum = 3
            )
        }

        // --- SECTION 3: Equities ---
        if (equityAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_equities),
                items = equityAssets,
                onAssetClick = { onAssetClick(it.symbol) },
                getIntradayStream = getIntradayStream,
                columnNum = 3
            )
        }

        // --- SECTION 3: Others - Crypto and Commodities ---
        if (otherAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_crypto),
                items = otherAssets,
                onAssetClick = { onAssetClick(it.symbol) },
                getIntradayStream = getIntradayStream,
                columnNum = 3
            )
        }

        // --- SECTION 4: Sector Rotation Heatmap ---
        if (sectorAssets.isNotEmpty()) {
            SectorHeatmapSection(
                title = "Sector Rotation", // Consider moving to strings.xml later!
                items = sectorAssets,
                onAssetClick = { onAssetClick(it.symbol) }
            )
        }

        // --- SECTION: Latest News preview (bottom of the Dashboard; chevron/card taps navigate to News) ---
        if (newsArticles.isNotEmpty()) {
            NewsPreviewSection(
                articles = newsArticles,
                onArticleClick = onNewsArticleClick,
                onSeeAllClick = onNavigateToNews
            )
        }
    }

    if (selectedRegimeForGlossary != null) {
        MarketGlossaryBottomSheet(
            currentRegime = selectedRegimeForGlossary,
            onDismiss = { selectedRegimeForGlossary = null }
        )
    }
}

@Composable
fun SectorHeatmapSection(
    title: String,
    items: List<AssetOverview?>,
    onAssetClick: (AssetOverview) -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = LocalPulseColors.current.accentPrimary,
            modifier = Modifier.padding(bottom = paddingMedium)
        )

        // 💡 Fixed Layout Grid (Muscle Memory)
        // Grouped logically: Offensive (Row 1), Cyclical (Row 2), Defensive (Row 3), Misc (Row 4)
        val fixedLayout = listOf(
            listOf("XLK", "XLC", "XLY"),
            listOf("XLF", "XLI", "XLE"),
            listOf("XLV", "XLP", "XLU"),
            listOf("XLB", "XLRE", null)
        )

        fixedLayout.forEach { rowSymbols ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max), // This forces the row to match its tallest child
                horizontalArrangement = Arrangement.spacedBy(paddingSmall)
            ) {
                rowSymbols.forEach { symbol ->
                    if (symbol == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val asset = items.find { it?.symbol == symbol }
                        if (asset != null) {
                            SectorBlock(
                                asset = asset,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(), // 💡 CRITICAL: Forces shorter blocks to stretch to match the tallest block in the row
                                onClick = { onAssetClick(asset) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(paddingSmall))
        }
    }
}

@Composable
fun SectorBlock(
    asset: AssetOverview,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val change = asset.changePercent ?: 0.0
    val pulseColors = LocalPulseColors.current

    val bgColor: Color
    val textColor: Color

    // 💡 The heatmap tile is the one place in this app where a signal color owns the entire
    // background rather than just text/pill accents -- full-saturation signal color as background,
    // white text on top for contrast. The old two-tier intensity scale (full-saturation only past
    // +-1% change, a softer background below that) is gone -- every non-flat tile uses the same
    // full-saturation treatment now, uniformly, regardless of how large the move is.
    //
    // 💡 `sectorHeatmapBullish`/`sectorHeatmapBearish`, not `signalBullishPill`/`signalBearishPill`
    // directly -- a full-tile background needs a different source token per mode (see PulseColors'
    // doc comment on these two fields), not one shared family across both.
    when {
        change > 0.0 -> {
            bgColor = pulseColors.sectorHeatmapBullish
            textColor = Color.White
        }
        change < 0.0 -> {
            bgColor = pulseColors.sectorHeatmapBearish
            textColor = Color.White
        }
        else -> {
            bgColor = MaterialTheme.colorScheme.surfaceVariant
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
        modifier = modifier
            // 💡 ADDED: A minimum height so short names (e.g. "Energy") don't look squished
            .defaultMinSize(minHeight = 72.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_small)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = asset.name ?: asset.symbol,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, // 💡 NEW: Centers multi-line text nicely
                maxLines = 3, // 💡 NEW: Prevents extreme overflow just in case
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))

            val sign = if (change > 0) "+" else ""
            val formattedChange = String.format(Locale.US, "%.2f", change)
            Text(
                text = "$sign$formattedChange%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

@Composable
fun AssetSection(
    title: String,
    items: List<AssetOverview?>,
    onAssetClick: (AssetOverview) -> Unit,
    columnNum: Int = 2,
    getIntradayStream: (String) -> Flow<IntradaySeries?> = { emptyFlow() }
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = LocalPulseColors.current.accentPrimary,
            modifier = Modifier.padding(bottom = paddingMedium)
        )

        items.chunked(columnNum).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(paddingMedium)
            ) {
                rowItems.forEach { item ->
                    item?.let {
                        AssetCard(
                            asset = it,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            intradayStream = getIntradayStream(it.symbol),
                            onClick = { onAssetClick(it) }
                        )
                    }
                }

                if (rowItems.size < columnNum) {
                    val emptySpots = columnNum - rowItems.size
                    repeat(emptySpots) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(paddingMedium))
        }
    }
}

@Composable
fun AssetCard(
    asset: AssetOverview,
    modifier: Modifier = Modifier,
    customVisual: @Composable (() -> Unit)? = null,
    intradayStream: Flow<IntradaySeries?> = emptyFlow(),
    onClick: () -> Unit
) {
    val isSentimentAsset = asset.symbol == "FEAR_GREED" || asset.symbol == "PUT_CALL"
    val pulseColors = LocalPulseColors.current

    val baseColor: Color
    val pillColor: Color
    // 💡 Price cards are uniform/non-directional now -- the card container no longer flips between
    // a bullish-tinted and bearish-tinted background. Direction lives only in baseColor/pillColor
    // (the directional pill below), so a grid of six down-days does not read as visually alarming.
    //
    // Sentiment cards (Fear & Greed, Put/Call) used to get a distinct NEUTRAL card style (plain
    // white/elevated surface) instead of the DATA style every other price card uses, on the theory
    // that a computed reading shouldn't look like raw data -- retired once that distinction stopped
    // being wanted; every price/reading card shares the same DATA style now.

    // 💡 NEW: Contrarian Logic for Sentiment Indicators
    if (isSentimentAsset && asset.rsiStatus != null) {
        when (asset.rsiStatus.uppercase()) {
            "EXTREME FEAR", "FEAR", "OVERSOLD" -> {
                baseColor = pulseColors.signalBullishText
                pillColor = pulseColors.signalBullishPill
            }
            "EXTREME GREED", "GREED", "OVERBOUGHT" -> {
                baseColor = pulseColors.signalBearishText
                pillColor = pulseColors.signalBearishPill
            }
            else -> {
                baseColor = MaterialTheme.colorScheme.onSurfaceVariant
                pillColor = MaterialTheme.colorScheme.surfaceVariant
            }
        }
    } else {
        // 💡 Standard Logic for Equities, Futures, and Crypto
        val change = asset.changePercent ?: 0.0
        val isMathematicallyPositive = change >= 0
        val isGoodEvent =
            if (asset.isInverted == true) !isMathematicallyPositive else isMathematicallyPositive

        baseColor = if (isGoodEvent) pulseColors.signalBullishText else pulseColors.signalBearishText
        pillColor = if (isGoodEvent) pulseColors.signalBullishPill else pulseColors.signalBearishPill
    }

    val cardTitle = when (asset.symbol) {
        "FEAR_GREED" -> {
            stringResource(id = R.string.fear_greed_title)
        }

        "PUT_CALL" -> {
            stringResource(id = R.string.put_call_index_title)
        }

        else -> {
            asset.symbol.replace("=F", "")
        }
    }

    val cardSubTitle = if (asset.symbol != "FEAR_GREED" && asset.symbol != "PUT_CALL") {
        asset.name
    } else {
        null
    }

    val livePriceTextSize = MaterialTheme.typography.titleSmall

    // 💡 Only the branch below that actually renders a `SparklineChart` (plain equities/ETFs,
    // crypto, gold/silver -- `customVisual` is null and the symbol is intraday-eligible) uses
    // `DATA_SPARKLINE`, which freezes this card to its pre-redesign accent-tinted look. Every other
    // asset card here -- VIX/Fear & Greed/Put-Call (which supply `customVisual`) and futures/oil/
    // copper (not intraday-eligible, no sparkline) -- has no sparkline to protect, so it picks up
    // `DATA`'s new flat white/greyish-dark card + shadow look like every other DATA card in the app.
    val hasSparkline = customVisual == null && DashboardIntradayEligibility.isEligible(asset.symbol)
    val cardStyle = if (hasSparkline) PulseCardStyle.DATA_SPARKLINE else PulseCardStyle.DATA

    PulseCard(
        style = cardStyle,
        modifier = modifier,
        onClick = onClick
    ) {
        // 💡 Horizontal padding moved off this Column and onto each child individually (instead
        // of the usual single `.padding(padding_large)` every side) so the sparkline below can
        // sit flush with the card's left/right edges -- a small chart reads noticeably better
        // full-bleed than inset, and the card's rounded corners already clip it back into shape
        // at the bottom. Every other child still gets the same horizontal inset it always had.
        //
        // 💡 Bottom padding moved off this Column too (top-only now) -- a card ending in a
        // sparkline applies its own bottom padding conditionally below (see the `else` branch),
        // since that card wants the sparkline flush with the card's bottom edge, same reasoning
        // as the horizontal edges above. Every other ending (customVisual, or the no-sparkline
        // fallback) still gets the same padding_large gap it always had, just added explicitly at
        // its own tail instead of inherited from here.
        // 💡 `fillMaxHeight()` so this Column actually occupies the full, row-matched card height
        // (siblings in the same row can be taller, e.g. one with a longer subtitle -- see the
        // `Row(Modifier.height(IntrinsicSize.Max))` call sites below) rather than sizing itself to
        // its own natural content height and leaving dead space below. The sparkline is the one
        // child that actually stretches into that extra room (`weight(1f)` below); everything
        // else keeps its natural size.
        val horizontalContentPadding = dimensionResource(id = R.dimen.padding_large)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = dimensionResource(id = R.dimen.padding_large))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalContentPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cardTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = asset.rsiStatus,
                    tint = LocalPulseColors.current.accentPrimary,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                )
            }

            cardSubTitle?.let {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = horizontalContentPadding)
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            if (customVisual != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = horizontalContentPadding)
                        .padding(bottom = dimensionResource(id = R.dimen.padding_large))
                ) {
                    customVisual()
                }
            } else {
                Text(
                    text = String.format("%.2f", asset.price),
                    style = livePriceTextSize.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = horizontalContentPadding)
                )

                val changeForDisplay = asset.changePercent ?: 0.0
                // 💡 Triangle direction always follows the raw numeric sign of the move itself, not
                // whether that move is "good" or "bad" -- pillColor/baseColor already carry the
                // good/bad read (including the isInverted flip above), so the triangle and the
                // pill's color can legitimately disagree, e.g. a rising VIX shows an up triangle on
                // a bearish-colored pill. The triangle already states the sign, so the text itself
                // is unsigned (magnitude only); an exact 0% reading gets the neutral tone and a flat
                // bar instead of either triangle, since it did not actually rise or fall.
                val isFlat = changeForDisplay == 0.0
                val changeDirection = when {
                    isFlat -> ChangeDirection.FLAT
                    changeForDisplay > 0 -> ChangeDirection.UP
                    else -> ChangeDirection.DOWN
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                DirectionalChangePill(
                    changeText = "${String.format("%.2f", abs(changeForDisplay))}%",
                    direction = changeDirection,
                    pillColor = if (isFlat) pulseColors.signalNeutralPill else pillColor,
                    contentColor = if (isFlat) pulseColors.signalNeutralText else baseColor,
                    modifier = Modifier.padding(horizontal = horizontalContentPadding)
                )

                // Live intraday sparkline -- only for the 23 symbols the backend actually polls
                // bars for (plain equities/ETFs, crypto, gold/silver; not futures/oil/copper,
                // which fall through to this same default branch but have no live feed). VIX and
                // sentiment never reach this branch at all (their own cards above use
                // `customVisual` or a dedicated `VixFullWidthCard`), so no separate exclusion is
                // needed for those here.
                if (DashboardIntradayEligibility.isEligible(asset.symbol)) {
                    val intradaySeries by intradayStream.collectAsStateWithLifecycle(initialValue = null)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                    // No bottom padding here -- this is the card's last element, and it should sit
                    // flush with the card's bottom edge (edge-to-edge on every side now, not just
                    // left/right), same as the sparkline's own edge-to-edge width above.
                    //
                    // 💡 `weight(1f)` instead of a fixed `height(...)` -- when a taller sibling in
                    // the same row (e.g. one with a longer subtitle) stretches this card beyond its
                    // own natural size, the sparkline is what grows to fill that extra room instead
                    // of leaving blank space between it and the card's now-lower bottom edge.
                    // `heightIn(min = ...)` keeps it at today's size as a floor -- it only ever
                    // grows from there, never shrinks below it.
                    SparklineChart(
                        points = intradaySeries?.points.orEmpty(),
                        previousClose = intradaySeries?.previousClose ?: asset.previousClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .heightIn(min = dimensionResource(id = R.dimen.dashboard_asset_sparkline_height))
                    )
                } else {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                }
            }
        }
    }
}

@Composable
fun TechnicalSummaryCard(summaryText: String?, timestamp: Long?, isEquityOpen: Boolean) {
    if (summaryText.isNullOrBlank() || timestamp == null) return

    var isExpanded by remember { mutableStateOf(false) }
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

    val paddingSmall = dimensionResource(id = R.dimen.padding_small)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    val pulseColors = LocalPulseColors.current
    val badgeBgColor =
        if (isEquityOpen) pulseColors.signalBullishPill else MaterialTheme.colorScheme.surfaceVariant
    val badgeTextColor =
        if (isEquityOpen) pulseColors.signalBullishText else MaterialTheme.colorScheme.onSurfaceVariant

    // 💡 This is the AI-generated "Technical Briefing" card -- interpreted, editorial content, not
    // raw market data. SYNTHESIS style, the same darker background as Indicators' AI Executive
    // Briefing and Summary's VerdictCard -- these three are this app's "AI briefing/verdict" hero
    // cards, one per screen, and share the same treatment for that reason. This is narrower than
    // SYNTHESIS used to be applied (news articles, list-style AI content, and per-symbol preview
    // cards all moved to the plain DATA background since only a screen's single leading AI
    // conclusion keeps the darker tint now). Contrast this with UnifiedScoreHeaderCard, whose
    // colors are all passed in by the caller as real signal colors (bullish/bearish/neutral pillar
    // scores) -- that one stays signal-colored on purpose, since it is showing raw computed data,
    // not an AI's interpretation of it.
    // 💡 `animateContentSize()` sits on the inner `Column` below, not on `PulseCard`'s own outer
    // `modifier` -- `PulseCard` draws its shadow via `Modifier.shadow`, and `animateContentSize()`
    // clips whatever it wraps to its own animated rectangle each frame. Placed outside the shadow
    // (on the Card's own modifier), that rectangular clip cut straight through the shadow's rounded
    // corners, leaving a flat greyish sliver poking out past the bottom edge instead of a clean
    // rounded shadow.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(paddingLarge).animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val textStyle =
                            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_ai_sparkle_filled),
                            contentDescription = "Analysis Engine",
                            tint = pulseColors.accentPrimary,
                            modifier = Modifier.size(iconSize)
                        )

                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

                        Text(
                            text = stringResource(id = R.string.dashboard_technical_briefing),
                            style = textStyle,
                            color = pulseColors.accentPrimary
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.analyzed_at, format.format(date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(id = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = paddingMedium)
                )
            }

            Spacer(modifier = Modifier.height(paddingMedium))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            Spacer(modifier = Modifier.height(paddingMedium))

            // Changed to MarkdownText to support bold blocks and line endings natively
            MarkdownText(
                markdown = summaryText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = if (isExpanded) Int.MAX_VALUE else 3
            )

            Spacer(modifier = Modifier.height(paddingLarge))
            SignalPill(
                text = if (isEquityOpen) stringResource(id = R.string.dashboard_market_open)
                else stringResource(id = R.string.dashboard_market_closed),
                pillColor = badgeBgColor,
                contentColor = badgeTextColor,
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.icon_size_small))
                            .background(color = badgeTextColor, shape = CircleShape)
                    )
                }
            )
        }
    }
}

@Composable
fun SentimentConsensusBadge(sentimentAssets: List<AssetOverview?>, onClick: (String) -> Unit) {
    var score = 0
    var validAssets = 0

    sentimentAssets.forEach { asset ->
        val status = asset?.rsiStatus?.uppercase()
        if (status != null) {
            validAssets++
            when (status) {
                "EXTREME GREED", "BULLISH" -> score += 2
                "GREED" -> score += 1
                "EXTREME FEAR", "BEARISH" -> score -= 2
                "FEAR" -> score -= 1
            }
        }
    }

    if (validAssets == 0) return

    val pulseColors = LocalPulseColors.current
    val consensusText: String
    val bgColor: Color
    val textColor: Color

    when {
        score >= 4 -> {
            consensusText = "DISTRIBUTION PHASE"
            bgColor = pulseColors.signalBearishPill
            textColor = pulseColors.signalBearishText
        }

        score in 1..3 -> {
            consensusText = "HEALTHY UPTREND"
            bgColor = pulseColors.signalBullishPill
            textColor = pulseColors.signalBullishText
        }

        score == 0 -> {
            consensusText = "SIDEWAYS RANGE"
            bgColor = MaterialTheme.colorScheme.surfaceVariant
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        }

        score in -2..-1 -> {
            consensusText = "DANGEROUS DOWNTREND"
            bgColor = pulseColors.signalBearishPill
            textColor = pulseColors.signalBearishText
        }

        score in -4..-3 -> {
            consensusText = "ACCUMULATION PHASE"
            bgColor = pulseColors.signalBullishPill
            textColor = pulseColors.signalBullishText
        }

        else -> {
            consensusText = "CRASH OPPORTUNITY"
            bgColor = pulseColors.signalBullishPill
            textColor = pulseColors.signalBullishText
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill)),
        modifier = Modifier.clickable { onClick(consensusText) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.padding_medium),
                vertical = dimensionResource(id = R.dimen.padding_medium)
            )
        ) {
            val textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

            Icon(
                painter = painterResource(id = R.drawable.ic_engine_quant),
                contentDescription = "Analysis Engine",
                tint = textColor,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            Text(
                text = consensusText,
                style = textStyle,
                color = textColor
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = "View Glossary",
                tint = textColor,
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewTechnicalSummaryCard() {
    MaterialTheme {
        val mockSummary = """
            Equities are showing resilience, with SPY testing its 20-day SMA. Tech continues to lead while small caps lag, indicating a concentrated rally.
            
            Commodities are mixed. Gold is catching a safe-haven bid while Copper pulls back slightly, pointing to mixed global economic signals.
            
            Overall sentiment remains neutral to slightly bullish, as the Fear & Greed index hovers near 55 and Put/Call ratios normalize.
        """.trimIndent()

        Column(modifier = Modifier.padding(16.dp)) {
            TechnicalSummaryCard(
                summaryText = mockSummary,
                timestamp = System.currentTimeMillis(),
                isEquityOpen = true
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewSectorHeatmapSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        // Mocking a diverse set of sector performances to visualize the full heatmap gradient
        val mockAssets = listOf(
            AssetOverview(
                symbol = "XLK",
                name = "Technology",
                type = AssetType.SECTOR,
                changePercent = 1.8
            ),    // Extreme Strength
            AssetOverview(
                symbol = "XLC",
                name = "Communication",
                type = AssetType.SECTOR,
                changePercent = 0.5
            ),   // Mild Strength
            AssetOverview(
                symbol = "XLY",
                name = "Consumer Disc",
                type = AssetType.SECTOR,
                changePercent = 0.0
            ),   // Flat
            AssetOverview(
                symbol = "XLF",
                name = "Financials",
                type = AssetType.SECTOR,
                changePercent = -0.4
            ),     // Mild Weakness
            AssetOverview(
                symbol = "XLI",
                name = "Industrials",
                type = AssetType.SECTOR,
                changePercent = -1.5
            ),    // Extreme Weakness
            AssetOverview(
                symbol = "XLE",
                name = "Energy",
                type = AssetType.SECTOR,
                changePercent = 2.1
            ),
            AssetOverview(
                symbol = "XLV",
                name = "Healthcare",
                type = AssetType.SECTOR,
                changePercent = -0.1
            ),
            AssetOverview(
                symbol = "XLP",
                name = "Consumer Staples",
                type = AssetType.SECTOR,
                changePercent = -0.8
            ),
            AssetOverview(
                symbol = "XLU",
                name = "Utilities",
                type = AssetType.SECTOR,
                changePercent = -1.2
            ),
            AssetOverview(
                symbol = "XLB",
                name = "Materials",
                type = AssetType.SECTOR,
                changePercent = 0.2
            ),
            AssetOverview(
                symbol = "XLRE",
                name = "Real Estate",
                type = AssetType.SECTOR,
                changePercent = 1.1
            )
        )

        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            SectorHeatmapSection(
                title = "Sector Rotation",
                items = mockAssets,
                onAssetClick = {}
            )
        }
    }
}