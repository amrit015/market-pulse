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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import com.marketlabs.pulse.storage.model.dashboard.enums.AssetType
import com.marketlabs.pulse.ui.components.widgets.PutCallHorizontalBar
import com.marketlabs.pulse.ui.components.widgets.SpeedometerGauge
import com.marketlabs.pulse.ui.components.widgets.VixFullWidthCard
import com.marketlabs.pulse.ui.screens.dashboard.views.sheet.AssetDetailBottomSheet
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    marketState: MarketState?,
    assets: List<AssetOverview?>,
    scaffoldPadding: PaddingValues
) {
    val scrollState = rememberScrollState()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val paddingExtraLarge = dimensionResource(id = R.dimen.padding_extra_large)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    val isEquityOpen = marketState?.isEquityOpen == true
    val isFuturesOpen = marketState?.isFuturesOpen == true

    val sentimentAssets =
        assets.filter { it?.symbol == "^VIX" || it?.symbol == "FEAR_GREED" || it?.symbol == "PUT_CALL" }
    val futureAssets = assets.filter { it?.type == AssetType.FUTURE }

    val equitySortOrder = listOf("SPY", "DIA", "QQQ", "RSP", "IWM", "MAGS")
    val cryptoCommoditySortOrder = listOf("BTC-USD", "ETH-USD", "GC=F", "SI=F", "CL=F", "HG=F")

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

    var selectedAsset by remember { mutableStateOf<AssetOverview?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(
                top = statusBarHeight + paddingExtraLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingExtraLarge,
                start = paddingLarge,
                end = paddingLarge
            ),
        verticalArrangement = Arrangement.spacedBy(paddingExtraLarge)
    ) {

        // 💡 UPDATED: Integrated Market Status into the Technical Briefing
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                )

                // 💡 NEW: Inject the calculated Consensus Badge!
                SentimentConsensusBadge(sentimentAssets = sentimentAssets)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                val vixAsset = sentimentAssets.find { it?.symbol == "^VIX" }
                if (vixAsset != null) {
                    VixFullWidthCard(asset = vixAsset, onClick = { selectedAsset = vixAsset })
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
                            onClick = { selectedAsset = greedAsset }
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
                            onClick = { selectedAsset = putCallAsset }
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
                onAssetClick = { selectedAsset = it },
                columnNum = 3
            )
        }

        // --- SECTION 3: Equities ---
        if (equityAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_equities),
                items = equityAssets,
                onAssetClick = { selectedAsset = it },
                columnNum = 3
            )
        }

        // --- SECTION 4: Others ---
        if (otherAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_crypto),
                items = otherAssets,
                onAssetClick = { selectedAsset = it },
                columnNum = 3
            )
        }
    }

    if (selectedAsset != null) {
        AssetDetailBottomSheet(
            asset = selectedAsset!!,
            onDismiss = { selectedAsset = null }
        )
    }
}

@Composable
fun AssetSection(
    title: String,
    items: List<AssetOverview?>,
    onAssetClick: (AssetOverview) -> Unit,
    columnNum: Int = 2
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
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
                            onClick = { onAssetClick(it) }
                        )
                    }
                }

                // Dynamically fill any remaining empty spots in the grid row
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
    onClick: () -> Unit
) {
    val change = asset.changePercent ?: 0.0
    val isMathematicallyPositive = change >= 0
    val isGoodEvent = if (asset.isInverted == true) !isMathematicallyPositive else isMathematicallyPositive

    val baseColor = if (isGoodEvent) PulseStatusColors.BullishText else PulseStatusColors.BearishText
    val backgroundColor = if (isGoodEvent) PulseStatusColors.BullishBg else PulseStatusColors.BearishBg

    val cardTitle = if (asset.symbol == "FEAR_GREED") {
        stringResource(id = R.string.fear_greed_title)
    } else if (asset.symbol == "PUT_CALL") {
        stringResource(id = R.string.put_call_index_title)
    } else {
        asset.symbol.replace("=F", "")
    }
    val cardSubTitle = if (asset.symbol != "FEAR_GREED" && asset.symbol != "PUT_CALL") {
        asset.name
    } else {
        null
    }

    val livePriceTextSize = MaterialTheme.typography.titleSmall

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                )
            }

            cardSubTitle?.let {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            if (customVisual != null) {
                customVisual()
            } else {
                Text(
                    text = String.format("%.2f", asset.price),
                    style = livePriceTextSize.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val sign = if (isMathematicallyPositive) "+" else ""
                Text(
                    text = "$sign${String.format("%.2f", change)}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = baseColor
                )
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

    // Market Status Colors
    val badgeBgColor = if (isEquityOpen) PulseStatusColors.BullishBg else MaterialTheme.colorScheme.surfaceVariant
    val badgeTextColor = if (isEquityOpen) PulseStatusColors.BullishText else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        // Matches the unified design of the Market Outlook card
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            // HEADER: Title + Timestamp (Left) & Arrow (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_ai_engine),
                            contentDescription = "Analysis Engine",
                            tint = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

                        Text(
                            text = stringResource(id = R.string.dashboard_technical_briefing),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
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
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = dimensionResource(id = R.dimen.border_thin))
            Spacer(modifier = Modifier.height(paddingMedium))

            // BODY: The Summary Text
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            // FOOTER: Market Status Badge (Bottom Left)
            Spacer(modifier = Modifier.height(paddingLarge))
            Surface(
                color = badgeBgColor,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = paddingMedium, vertical = paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.icon_size_small))
                            .background(color = badgeTextColor, shape = CircleShape)
                    )
                    Text(
                        text = if (isEquityOpen) stringResource(id = R.string.dashboard_market_open)
                        else stringResource(id = R.string.dashboard_market_closed),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeTextColor,
                        modifier = Modifier.padding(start = paddingSmall)
                    )
                }
            }
        }
    }
}

@Composable
fun SentimentConsensusBadge(sentimentAssets: List<AssetOverview?>) {
    var score = 0
    var validAssets = 0

    // 1. Calculate the math consensus (Max +6, Min -6)
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

    val consensusText: String
    val bgColor: androidx.compose.ui.graphics.Color
    val textColor: androidx.compose.ui.graphics.Color

    // 2. Map the score to the 6 Market Phases
    // 2. Map the score to the 6 Market Phases
    when {
        score >= 4 -> {
            consensusText = "DISTRIBUTION PHASE"
            // 💡 CONTRARIAN FLIP: High greed. Smart money is selling. This is a WARNING.
            bgColor = PulseStatusColors.BearishBg
            textColor = PulseStatusColors.BearishText
        }
        score in 1..3 -> {
            consensusText = "HEALTHY UPTREND"
            // Normal, safe momentum.
            bgColor = PulseStatusColors.BullishBg
            textColor = PulseStatusColors.BullishText
        }
        score == 0 -> {
            consensusText = "SIDEWAYS RANGE"
            bgColor = MaterialTheme.colorScheme.surfaceVariant
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
        score in -2..-1 -> {
            consensusText = "DANGEROUS DOWNTREND"
            // Normal, dangerous selling pressure.
            bgColor = PulseStatusColors.BearishBg
            textColor = PulseStatusColors.BearishText
        }
        score in -4..-3 -> {
            consensusText = "ACCUMULATION PHASE"
            // 💡 CONTRARIAN FLIP: Deep fear. Smart money is buying. This is an OPPORTUNITY.
            bgColor = PulseStatusColors.BullishBg
            textColor = PulseStatusColors.BullishText
        }
        else -> {
            consensusText = "CRASH OPPORTUNITY"
            // CONTRARIAN FLIP: Absolute panic. The market is flashing a rare buying opportunity!
            bgColor = PulseStatusColors.BullishBg
            textColor = PulseStatusColors.BullishText
        }
    }

    // 3. Draw the Pill Badge
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // 💡 FIX 1: Move padding to the Row so it wraps BOTH the icon and the text!
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.padding_medium),
                vertical = dimensionResource(id = R.dimen.padding_small)
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_quant_engine),
                contentDescription = "Analysis Engine",
                tint = textColor,
                modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            Text(
                text = consensusText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor
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