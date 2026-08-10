package com.marketlabs.pulse.ui.components.bottomSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.utils.verticalScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailBottomSheet(
    asset: AssetOverview,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    // 💡 NEW: Condition to hide technicals for Sentiment & VIX
    val showTechnicals = asset.symbol !in listOf("^VIX", "FEAR_GREED", "PUT_CALL")

    val currentPrice = String.format("%.2f", asset.price ?: 0.0)
    val previousClosePrice = String.format("%.2f", asset.previousClose ?: 0.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // 💡 Bottom sheets sit above the base background, at the elevated step of the surface
        // ramp, so they read as a distinct layer floating over the screen rather than blending in.
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        val scrollState = rememberScrollState() // 💡 Add this
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollbar(          // Attach  visual scrollbar
                    state = scrollState,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                .verticalScroll(scrollState)
                .padding(start = paddingLarge, end = paddingLarge, bottom = paddingLarge * 2)
        ) {
            // Header
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showTechnicals)
                    Text(
                        text = asset.symbol.replace("=F", ""),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                Text(
                    text = asset.name ?: "",
                    style = if (showTechnicals) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
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
                // Current Price Display
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(end = paddingMedium)
                ) {
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

                // Previous Close Display
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

            // Hide everything below this point for Sentiment/VIX metrics
            if (showTechnicals) {
                Spacer(modifier = Modifier.height(paddingLarge))

                // SECTION 1: Technical Breakdown
                Text(
                    text = stringResource(id = R.string.dashboard_technical_breakdown),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = paddingMedium)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 💡 RSI is now a ColoredMetricItem using the new statusForColor parameter!
                    ColoredMetricItem(
                        label = stringResource(id = R.string.dashboard_rsi),
                        value = "${asset.rsi ?: "--"} (${asset.rsiStatus ?: "N/A"})",
                        statusForColor = asset.rsiStatus
                            ?: "", // Pass the raw status ("OVERBOUGHT") for color logic
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

                // SECTION 2: Simple Moving Averages
                Text(
                    text = stringResource(id = R.string.dashboard_sma_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = paddingMedium)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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

                // 💡 SECTION 3: Glossary
                Text(
                    text = stringResource(id = R.string.dashboard_technical_glossary_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = paddingMedium)
                )
                Column {
                    GlossaryItem("RSI", stringResource(id = R.string.dashboard_glossary_rsi_def))
                    GlossaryItem("MACD", stringResource(id = R.string.dashboard_glossary_macd_def))
                    GlossaryItem(
                        "Trend",
                        stringResource(id = R.string.dashboard_glossary_trend_def)
                    )
                    GlossaryItem("SMA", stringResource(id = R.string.dashboard_glossary_sma_def))
                }
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

// 💡 NEW: Added optional `statusForColor` parameter that defaults to `value`
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

// 💡 NEW: Helper composable for the glossary using AnnotatedString to mix Bold and Normal text
@Composable
fun GlossaryItem(term: String, definition: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
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