package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.posture.DomainDarkPoolIndex
import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import com.marketlabs.pulse.storage.model.posture.DomainNaaimExposure
import com.marketlabs.pulse.storage.model.posture.DomainNetLiquidity
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InstitutionalPostureSection(postureData: DomainMarketPosture) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(paddingMedium)
    ) {
        // --- SECTION HEADER ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            val textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

            Icon(
                painter = painterResource(id = R.drawable.ic_engine_quant), // You can use a different icon if preferred
                contentDescription = "Institutional Posture",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            Text(
                text = "Institutional Posture", // Add to strings.xml: stringResource(id = R.string.section_institutional_posture)
                style = textStyle,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val date = postureData.timestamp?.let { Date(it) } ?: Date()
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = "Updated: ${format.format(date)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = paddingSmall)
        )

        // --- DISCLAIMER BANNER ---
        PostureDisclaimerCard()

        Spacer(modifier = Modifier.height(paddingSmall))

        // --- METRIC CARDS ---
        postureData.naaimExposure?.let { NaaimExposureCard(it) }
        postureData.darkPoolIndex?.let { DarkPoolCard(it) }
        postureData.netLiquidity?.let { NetLiquidityCard(it) }
    }
}

@Composable
private fun PostureDisclaimerCard() {
    var expanded by remember { mutableStateOf(false) }

    // 💡 SYNTHESIS style -- a disclaimer about externally-sourced research data, editorial content
    // rather than a raw reading. Replaces the old `surfaceVariant.copy(alpha = 0.5f)` leftover from
    // before this app had its own token system.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "External Research Data",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = "Toggle Disclaimer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                Text(
                    text = "These metrics track external Wall Street positioning and global fiat liquidity. They are provided for contextual research and are not used by the Pulse AI to calculate our proprietary market regime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NaaimExposureCard(naaim: DomainNaaimExposure) {
    // 💡 SYNTHESIS style -- externally-sourced institutional positioning data, presented with
    // AI-written context, not a raw price. Replaces the old `secondaryContainer.copy(alpha = 0.4f)`
    // leftover from before this app had its own token system.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Manager Exposure (NAAIM)",
                        // 💡 titleSmall (15sp, semi-bold), not titleMedium.Bold (17sp, bold) -- the
                        // consistent title tier every curated/AI-content card title uses now,
                        // separate from DATA-style cards (Equities, VIX, Indicators), which keep
                        // their bold 17sp title.
                        style = MaterialTheme.typography.titleSmall,
                        // 💡 Card titles are always onSurface (dark-on-light/white-on-dark) across
                        // this app -- same treatment as Equities/AI/News cards.
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // 💡 Explainer subtitle, always onSurface -- was `onSurfaceVariant`, reserved
                    // for genuine metadata like dates, not descriptive content.
                    Text(
                        text = naaim.description ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 💡 Real `signal.*.pill`/`signal.*.text` token pairs now, not a translucent
                // (`.copy(alpha = 0.15f)`) version of the text color standing in for a background --
                // the same pill token every other signal badge in the app uses.
                val pulseColors = LocalPulseColors.current
                val (statusPillColor, statusTextColor) = when (naaim.status?.uppercase()) {
                    "BULLISH", "EXTREME GREED (LEVERAGED)" -> pulseColors.signalBullishPill to pulseColors.signalBullishText
                    "BEARISH", "EXTREME FEAR (HEDGED)" -> pulseColors.signalBearishPill to pulseColors.signalBearishText
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                SignalPill(
                    text = naaim.status ?: "UNKNOWN",
                    pillColor = statusPillColor,
                    contentColor = statusTextColor,
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            // 💡 FIX: Safely check for null before doing math or drawing the progress bar
            if (naaim.value != null) {
                val exposureValue = naaim.value.toFloat()
                val fillPercentage = (exposureValue / 150f).coerceIn(0f, 1f)

                Text(
                    text = "${String.format(Locale.US, "%.1f", exposureValue)}%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = fillPercentage)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(100f / 150f)
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                .align(Alignment.CenterEnd)
                        )
                    }
                }
            } else {
                // 💡 FIX: What to show when the backend fails to scrape the data
                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                // Draw a completely empty, greyed-out bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cash (0%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Fully Invested (100%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Leveraged (150%+)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DarkPoolCard(dix: DomainDarkPoolIndex) {
    // 💡 SYNTHESIS style -- see NaaimExposureCard above.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Pool Index (DIX)",
                        // 💡 titleSmall (15sp, semi-bold) -- see NaaimExposureCard above.
                        style = MaterialTheme.typography.titleSmall,
                        // 💡 Card titles are always onSurface -- see NaaimExposureCard above.
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // 💡 Explainer subtitle, always onSurface -- see NaaimExposureCard above.
                    Text(
                        text = dix.description ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val isBullish = (dix.value ?: 0.0) >= 45.0
                // 💡 Real `signal.*.pill`/`signal.*.text` token pair -- see NaaimExposureCard above.
                val pulseColors = LocalPulseColors.current
                val (statusPillColor, statusTextColor) = if (isBullish) {
                    pulseColors.signalBullishPill to pulseColors.signalBullishText
                } else {
                    MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                SignalPill(
                    text = dix.status ?: "UNKNOWN",
                    pillColor = statusPillColor,
                    contentColor = statusTextColor,
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            Text(
                text = "${String.format(Locale.US, "%.1f", dix.value ?: 0.0)}%",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            dix.date?.let {
                Text(
                    text = "As of $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NetLiquidityCard(liquidity: DomainNetLiquidity) {
    // 💡 SYNTHESIS style -- see NaaimExposureCard above.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Global Net Liquidity",
                        // 💡 titleSmall (15sp, semi-bold) -- see NaaimExposureCard above.
                        style = MaterialTheme.typography.titleSmall,
                        // 💡 Card titles are always onSurface -- see NaaimExposureCard above.
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // 💡 Explainer subtitle, always onSurface -- see NaaimExposureCard above.
                    Text(
                        text = liquidity.description ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 💡 NEW: The Dynamic Liquidity Status Badge
                // 💡 Real `signal.*.pill`/`signal.*.text` token pair -- see NaaimExposureCard above.
                val pulseColors = LocalPulseColors.current
                val (statusPillColor, statusTextColor) = when (liquidity.status?.uppercase()) {
                    "EXPANDING" -> pulseColors.signalBullishPill to pulseColors.signalBullishText // Expanding liquidity acts as a tailwind (Green)
                    "DRAINING" -> pulseColors.signalBearishPill to pulseColors.signalBearishText // Contracting liquidity acts as a headwind (Red)
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                SignalPill(
                    text = liquidity.status ?: "UNKNOWN",
                    pillColor = statusPillColor,
                    contentColor = statusTextColor,
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            Text(
                text = "$${String.format(Locale.US, "%.2f", liquidity.value ?: 0.0)}T",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            // Equation Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LiquidityComponentCol("Fed Assets", liquidity.assetsT, isPositive = true)
                Text("-", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LiquidityComponentCol("TGA", liquidity.tgaT, isPositive = false)
                Text("-", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LiquidityComponentCol("Reverse Repo", liquidity.rrpT, isPositive = false)
            }
        }
    }
}

@Composable
private fun LiquidityComponentCol(label: String, valueT: Double?, isPositive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$${String.format(Locale.US, "%.2f", valueT ?: 0.0)}T",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isPositive) LocalPulseColors.current.signalBullishText else LocalPulseColors.current.signalBearishText
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================
val paddingSmall = 8.dp // Helper for file scope

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewInstitutionalPosture() {
    MaterialTheme {
        val mockData = DomainMarketPosture(
            naaimExposure = DomainNaaimExposure(85.4, "BULLISH", "Tracks the average equity exposure..."),
            darkPoolIndex = DomainDarkPoolIndex(46.2, "2026-06-26", "ACCUMULATION (BULLISH)", "Measures dark pool volume..."),
            netLiquidity = DomainNetLiquidity(6.24, "UNKNOWN", 7.32, 0.65, 0.43, "2026-06-25", "Calculates actual fiat liquidity..."),
            timestamp = System.currentTimeMillis()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            InstitutionalPostureSection(mockData)
        }
    }
}