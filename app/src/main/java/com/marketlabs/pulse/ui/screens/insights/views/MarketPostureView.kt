package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = naaim.description ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val statusColor = when (naaim.status?.uppercase()) {
                    "BULLISH", "EXTREME GREED (LEVERAGED)" -> LocalPulseColors.current.signalBullishText
                    "BEARISH", "EXTREME FEAR (HEDGED)" -> LocalPulseColors.current.signalBearishText
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip)),
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small))
                ) {
                    Text(
                        text = naaim.status ?: "UNKNOWN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(id = R.dimen.padding_medium),
                            vertical = dimensionResource(id = R.dimen.padding_tiny)
                        )
                    )
                }
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dix.description ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isBullish = (dix.value ?: 0.0) >= 45.0
                val statusColor = if (isBullish) LocalPulseColors.current.signalBullishText else MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip)),
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small))
                ) {
                    Text(
                        text = dix.status ?: "UNKNOWN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(id = R.dimen.padding_medium),
                            vertical = dimensionResource(id = R.dimen.padding_tiny)
                        )
                    )
                }
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = liquidity.description ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 💡 NEW: The Dynamic Liquidity Status Badge
                val statusColor = when (liquidity.status?.uppercase()) {
                    "EXPANDING" -> LocalPulseColors.current.signalBullishText // Expanding liquidity acts as a tailwind (Green)
                    "DRAINING" -> LocalPulseColors.current.signalBearishText // Contracting liquidity acts as a headwind (Red)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip)),
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small))
                ) {
                    Text(
                        text = liquidity.status ?: "UNKNOWN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(id = R.dimen.padding_medium),
                            vertical = dimensionResource(id = R.dimen.padding_tiny)
                        )
                    )
                }
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