package com.marketlabs.pulse.ui.screens.dashboard.views

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import com.marketlabs.pulse.storage.model.dashboard.enums.AssetType
import com.marketlabs.pulse.ui.screens.dashboard.views.sheet.AssetDetailBottomSheet
import com.marketlabs.pulse.ui.screens.dashboard.views.widgets.PutCallHorizontalBar
import com.marketlabs.pulse.ui.screens.dashboard.views.widgets.SpeedometerGauge
import com.marketlabs.pulse.ui.screens.dashboard.views.widgets.VixFullWidthCard
import com.marketlabs.pulse.ui.theme.ColorBearish
import com.marketlabs.pulse.ui.theme.ColorBullish
import com.marketlabs.pulse.ui.theme.ColorNeutral

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

    // Group the assets based on UI logic
    val spyAsset = assets.find { it?.symbol == "SPY" }
    val isEquityOpen = marketState?.isEquityOpen == true
    val isFuturesOpen = marketState?.isFuturesOpen == true

    // Categorize everything
    val sentimentAssets =
        assets.filter { it?.symbol == "^VIX" || it?.symbol == "FEAR_GREED" || it?.symbol == "PUT_CALL" }
    val futureAssets = assets.filter { it?.type == AssetType.FUTURE }

    // 💡 NEW: Define strict sorting orders
    val equitySortOrder = listOf("SPY", "RSP", "DIA", "QQQ", "IWM", "MAGS")
    val cryptoCommoditySortOrder = listOf("BTC-USD", "ETH-USD", "GC=F", "SI=F")

    // 💡 NEW: Extract AND Sort Equities
    val equityAssets = assets
        .filter { it?.type == AssetType.EQUITY }
        .sortedBy { asset ->
            val index = equitySortOrder.indexOf(asset?.symbol)
            if (index == -1) Int.MAX_VALUE else index // Put unlisted items at the bottom
        }

    // 💡 NEW: Extract AND Sort Crypto & Commodities
    val otherAssets = assets
        .filter { it?.type == AssetType.COMMODITY || it?.type == AssetType.CRYPTO }
        .sortedBy { asset ->
            val index = cryptoCommoditySortOrder.indexOf(asset?.symbol)
            if (index == -1) Int.MAX_VALUE else index // Put unlisted items at the bottom
        }

    // 💡 NEW: State to track which asset is currently selected for the Bottom Sheet
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
        // --- HEADER: Market Status Bar ---
        MarketStatusBar(isEquityOpen = isEquityOpen, spyAsset = spyAsset)

        if (sentimentAssets.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.dashboard_section_sentiment),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                )

                // 1. Full-Width VIX Card
                val vixAsset = sentimentAssets.find { it?.symbol == "^VIX" }
                if (vixAsset != null) {
                    VixFullWidthCard(asset = vixAsset, onClick = { selectedAsset = vixAsset })
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                }

                // 2. 2x2 Grid placement for Fear & Greed AND Put/Call
                val greedAsset = sentimentAssets.find { it?.symbol == "FEAR_GREED" }
                val putCallAsset = sentimentAssets.find { it?.symbol == "PUT_CALL" }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max), // 💡 1. Forces Row to find the tallest child
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
                ) {
                    if (greedAsset != null) {
                        AssetCard(
                            asset = greedAsset,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(), // 💡 2. Forces this card to stretch to the Row's height
                            customVisual = {
                                SpeedometerGauge(
                                    score = greedAsset.price ?: 50.0,
                                    change = greedAsset.changePercent,
                                    status = greedAsset.rsiStatus
                                )
                            },
                            showVerdict = false,
                            onClick = { selectedAsset = greedAsset }
                        )
                    }

                    if (putCallAsset != null) {
                        AssetCard(
                            asset = putCallAsset,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(), // 💡 2. Forces this card to stretch to the Row's height
                            customVisual = {
                                PutCallHorizontalBar(
                                    ratio = putCallAsset.price ?: 1.0,
                                    change = putCallAsset.changePercent,
                                    status = putCallAsset.rsiStatus
                                )
                            },
                            showVerdict = false,
                            onClick = { selectedAsset = putCallAsset }
                        )
                    } else if (greedAsset != null) {
                        // Invisible slot keeps it 50% width if Put/Call data is still loading
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // --- SECTION 2: Equities (ALWAYS SHOWN NOW) ---
        if (equityAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_equities),
                items = equityAssets,
                onAssetClick = { selectedAsset = it } // pass the click action
            )
        }

        // --- SECTION 3: Futures ---
        if (!isEquityOpen && isFuturesOpen && futureAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_futures),
                items = futureAssets,
                onAssetClick = { selectedAsset = it } // pass the click action
            )
        }

        // --- SECTION 4: Others (Crypto/Commodities) ---
        if (otherAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_crypto),
                items = otherAssets,
                onAssetClick = { selectedAsset = it } // pass the click action
            )
        }
    }

    // 💡 NEW: Trigger the Bottom Sheet when an asset is selected
    if (selectedAsset != null) {
        AssetDetailBottomSheet(
            asset = selectedAsset!!,
            onDismiss = { selectedAsset = null }
        )
    }
}

@Composable
fun MarketStatusBar(isEquityOpen: Boolean, spyAsset: AssetOverview?) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingMedium, vertical = paddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Status Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_size_small))
                        .background(
                            color = if (isEquityOpen) ColorBullish else ColorNeutral,
                            shape = CircleShape
                        )
                )
                Text(
                    text = if (isEquityOpen) stringResource(id = R.string.dashboard_market_open)
                    else stringResource(id = R.string.dashboard_market_closed),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = paddingSmall)
                )
            }

            // RIGHT: SPY Performance
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = spyAsset?.name ?: stringResource(id = R.string.dashboard_fallback_spy),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = paddingSmall)
                )

                // 💡 NEW: Toggle between showing Percentage vs Absolute Price based on market state
                if (isEquityOpen) {
                    val change = spyAsset?.changePercent ?: 0.0
                    val changeColor = if (change >= 0) ColorBullish else ColorBearish
                    val sign = if (change >= 0) "+" else ""

                    Text(
                        text = "$sign${String.format("%.2f", change)}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = changeColor
                    )
                } else {
                    val price = spyAsset?.price
                    Text(
                        text = "$${String.format("%.2f", price)}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AssetSection(
    title: String,
    items: List<AssetOverview?>,
    onAssetClick: (AssetOverview) -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = paddingMedium)
        )

        // Chunk into pairs of 2 for a nice grid look
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max), // Forces Row to find the tallest child
                horizontalArrangement = Arrangement.spacedBy(paddingMedium)
            ) {
                rowItems.forEach { item ->
                    item?.let {
                        AssetCard(
                            asset = it,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(), // Forces this card to stretch to the Row's height
                            onClick = { onAssetClick(it) }
                        )
                    }
                }

                // If there's an odd number of items, fill the empty space
                if (rowItems.size == 1) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
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
    customVisual: @Composable (() -> Unit)? = null, // 💡 NEW: Optional slot for custom charts
    showVerdict: Boolean = true, // 💡 NEW: Controls the visibility of the AI text
    onClick: () -> Unit
) {
    // 💡 LOGIC: Determine if the asset's current move is "Good" or "Bad"
    val change = asset.changePercent ?: 0.0
    val isMathematicallyPositive = change >= 0

    // If the asset is inverted (like VIX), a drop is GOOD (Green) and a rise is BAD (Red).
    val isGoodEvent =
        if (asset.isInverted == true) !isMathematicallyPositive else isMathematicallyPositive

    // Calculate the subtle background tint
    val baseColor = if (isGoodEvent) ColorBullish else ColorBearish
    val backgroundColor = baseColor.copy(alpha = 0.10f)

    val cardTitle = if (asset.symbol == "FEAR_GREED") {
        stringResource(id = R.string.fear_greed_title)
    } else if (asset.symbol == "PUT_CALL") {
        stringResource(id = R.string.put_call_index_title)
    } else {
        asset.symbol.replace("=F", "")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            // Header: Symbol and AI Status Icon
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

                // Optional AI Status Icon (e.g., BULLISH, OVERBOUGHT)
                if (asset.rsiStatus != null) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info), // PLACEHOLDER: Replace with your actual icon
                        contentDescription = asset.rsiStatus,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            // 💡 NEW LOGIC: If a custom visual is passed (like a Donut or Speedometer), show it!
            // Otherwise, fallback to the standard text-based Live Price and Percentage.
            if (customVisual != null) {
                customVisual()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            } else {
                // Live Price
                Text(
                    text = String.format("%.2f", asset.price),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Live Percentage
                val sign = if (isMathematicallyPositive) "+" else ""
                Text(
                    text = "$sign${String.format("%.2f", change)}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = baseColor
                )
            }

            // 💡 NEW: Wrap the Verdict in the visibility flag
            if (showVerdict) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                // AI Verdict Snippet
                Text(
                    text = asset.aiVerdict ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3
                )
            }
        }
    }
}