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
import com.marketlabs.pulse.ui.components.widgets.PutCallHorizontalBar
import com.marketlabs.pulse.ui.components.widgets.SpeedometerGauge
import com.marketlabs.pulse.ui.components.widgets.VixFullWidthCard
import com.marketlabs.pulse.ui.theme.PulseStatusColors // 💡 NEW: Imported the centralized colors

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

    val spyAsset = assets.find { it?.symbol == "SPY" }
    val isEquityOpen = marketState?.isEquityOpen == true
    val isFuturesOpen = marketState?.isFuturesOpen == true

    val sentimentAssets =
        assets.filter { it?.symbol == "^VIX" || it?.symbol == "FEAR_GREED" || it?.symbol == "PUT_CALL" }
    val futureAssets = assets.filter { it?.type == AssetType.FUTURE }

    val equitySortOrder = listOf("SPY", "RSP", "DIA", "QQQ", "IWM", "MAGS")
    val cryptoCommoditySortOrder = listOf("BTC-USD", "ETH-USD", "GC=F", "SI=F")

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
        // --- HEADER: Market Status Bar ---
        MarketStatusBar(isEquityOpen = isEquityOpen, spyAsset = spyAsset)

        if (sentimentAssets.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.dashboard_section_sentiment),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                )

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
                            showVerdict = false,
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
                            showVerdict = false,
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
                onAssetClick = { selectedAsset = it }
            )
        }

        // --- SECTION 4: Others ---
        if (otherAssets.isNotEmpty()) {
            AssetSection(
                title = stringResource(id = R.string.dashboard_section_crypto),
                items = otherAssets,
                onAssetClick = { selectedAsset = it }
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
fun MarketStatusBar(isEquityOpen: Boolean, spyAsset: AssetOverview?) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    val change = spyAsset?.changePercent ?: 0.0
    val isPositive = change >= 0

    // LEFT BADGE COLORS (Market Status)
    val leftBgColor = if (isEquityOpen) PulseStatusColors.BullishBg else MaterialTheme.colorScheme.surfaceVariant
    val leftTextColor = if (isEquityOpen) PulseStatusColors.BullishText else MaterialTheme.colorScheme.onSurfaceVariant

    // RIGHT BADGE COLORS (SPY Performance)
    val rightBgColor = if (isEquityOpen) {
        if (isPositive) PulseStatusColors.BullishBg else PulseStatusColors.BearishBg
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val rightTextColor = if (isEquityOpen) {
        if (isPositive) PulseStatusColors.BullishText else PulseStatusColors.BearishText
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT: Status Badge
        Surface(
            color = leftBgColor,
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = paddingMedium, vertical = paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_size_small))
                        .background(
                            color = leftTextColor,
                            shape = CircleShape
                        )
                )
                Text(
                    text = if (isEquityOpen) stringResource(id = R.string.dashboard_market_open)
                    else stringResource(id = R.string.dashboard_market_closed),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = leftTextColor,
                    modifier = Modifier.padding(start = paddingSmall)
                )
            }
        }

        // RIGHT: SPY Performance
        Surface(
            color = rightBgColor,
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = paddingMedium, vertical = paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spyAsset?.name ?: stringResource(id = R.string.dashboard_fallback_spy),
                    style = MaterialTheme.typography.labelMedium,
                    // Slightly fade the ticker name to make the numbers pop more
                    color = rightTextColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = paddingSmall)
                )

                if (isEquityOpen) {
                    val sign = if (isPositive) "+" else ""
                    Text(
                        text = "$sign${String.format("%.2f", change)}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = rightTextColor
                    )
                } else {
                    val price = spyAsset?.price ?: 0.0
                    Text(
                        text = "$${String.format("%.2f", price)}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = rightTextColor
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
    customVisual: @Composable (() -> Unit)? = null,
    showVerdict: Boolean = true,
    onClick: () -> Unit
) {
    val change = asset.changePercent ?: 0.0
    val isMathematicallyPositive = change >= 0
    val isGoodEvent = if (asset.isInverted == true) !isMathematicallyPositive else isMathematicallyPositive

    // 💡 USES CENTRALIZED DYNAMIC COLORS
    val baseColor = if (isGoodEvent) PulseStatusColors.BullishText else PulseStatusColors.BearishText
    val backgroundColor = if (isGoodEvent) PulseStatusColors.BullishBg else PulseStatusColors.BearishBg

    val cardTitle = if (asset.symbol == "FEAR_GREED") {
        stringResource(id = R.string.fear_greed_title)
    } else if (asset.symbol == "PUT_CALL") {
        stringResource(id = R.string.put_call_index_title)
    } else {
        asset.symbol.replace("=F", "")
    }

    val isFutures = asset.type == AssetType.FUTURE
    val livePriceTextSize = if (isFutures) MaterialTheme.typography.titleSmall else MaterialTheme.typography.headlineSmall

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor), // 👈 Dynamic BG applied directly
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

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            if (customVisual != null) {
                customVisual()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
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

            if (showVerdict && !isFutures) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

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