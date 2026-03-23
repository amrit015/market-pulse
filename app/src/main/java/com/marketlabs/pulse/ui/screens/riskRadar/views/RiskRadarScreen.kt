package com.marketlabs.pulse.ui.screens.riskRadar.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.riskRadar.Gauge
import com.marketlabs.pulse.storage.model.riskRadar.RiskGauges
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.model.riskRadar.enums.RiskStatus
import com.marketlabs.pulse.storage.model.riskRadar.enums.RiskTrend
import com.marketlabs.pulse.ui.components.widgets.ScoreGauge
import com.marketlabs.pulse.ui.screens.riskRadar.GaugeDefinition
import com.marketlabs.pulse.ui.screens.riskRadar.GaugeDictionary
import com.marketlabs.pulse.ui.screens.riskRadar.views.widgets.GaugeEducationalBottomSheet
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SelectedGaugeState(
    val title: String,
    val gauge: Gauge,
    val definition: GaugeDefinition
)

@Composable
fun RiskRadarScreen(
    data: RiskRadar,
    scaffoldPadding: PaddingValues
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingXLarge = dimensionResource(id = R.dimen.padding_xlarge)

    var selectedGaugeState by remember { mutableStateOf<SelectedGaugeState?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = statusBarHeight + paddingLarge,
            bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
            start = paddingLarge,
            end = paddingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(paddingXLarge)
    ) {
        item { RadarHeaderSection(data.lastUpdated) }

        item {
            ScoreBoardCard(
                data = data,
                onClick = { selectedGaugeState = it }
            )
        }

        item {
            Text(
                text = stringResource(id = R.string.radar_systemic_plumbing).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        data.gauges?.let { gauges ->
            item {
                SystemicPlumbingCard(
                    gauges = gauges,
                    onClick = { selectedGaugeState = it }
                )
            }
        }
    }

    selectedGaugeState?.let { state ->
        GaugeEducationalBottomSheet(
            selectedState = state,
            onDismiss = { selectedGaugeState = null }
        )
    }
}

@Composable
private fun RadarHeaderSection(timestamp: Long) {
    Column {
        Text(
            text = stringResource(id = R.string.radar_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = stringResource(id = R.string.analyzed_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
        )
    }
}

@Composable
private fun ScoreBoardCard(
    data: RiskRadar,
    onClick: (SelectedGaugeState) -> Unit
) {
    var hasOverflow by remember { mutableStateOf(false) }

    val safeStatus = data.status ?: RiskStatus.UNKNOWN
    val safeTrend = data.trend ?: RiskTrend.UNKNOWN

    val score = data.score ?: 0
    val previousScore = data.previousScore ?: score

    val vulnerabilityTitle = stringResource(id = R.string.radar_title_vulnerability)
    val trendTitle = stringResource(id = R.string.radar_title_trend)

    val statusColor = when (safeStatus) {
        RiskStatus.SAFE -> PulseStatusColors.BullishText
        RiskStatus.STABLE -> PulseStatusColors.NeutralText
        RiskStatus.CAUTION -> PulseStatusColors.WarningText
        RiskStatus.DANGER -> PulseStatusColors.BearishText
        RiskStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusBgColor = when (safeStatus) {
        RiskStatus.SAFE -> PulseStatusColors.BullishBg
        RiskStatus.STABLE -> PulseStatusColors.NeutralBg
        RiskStatus.CAUTION -> PulseStatusColors.WarningBg
        RiskStatus.DANGER -> PulseStatusColors.BearishBg
        RiskStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
    }

    val interpretationText = when (safeStatus) {
        RiskStatus.SAFE -> stringResource(id = R.string.radar_action_safe)
        RiskStatus.STABLE -> stringResource(id = R.string.radar_action_stable)
        RiskStatus.CAUTION -> stringResource(id = R.string.radar_action_caution)
        RiskStatus.DANGER -> stringResource(id = R.string.radar_action_danger)
        RiskStatus.UNKNOWN -> stringResource(id = R.string.radar_data_unavailable)
    }

    val trendIconRes = when (safeTrend) {
        RiskTrend.ACCELERATING -> R.drawable.ic_trending_up
        RiskTrend.COOLING -> R.drawable.ic_trending_down
        else -> R.drawable.ic_trending_flat
    }

    val trendColor = when (safeTrend) {
        RiskTrend.ACCELERATING -> PulseStatusColors.BearishText
        RiskTrend.COOLING -> PulseStatusColors.BullishText
        RiskTrend.STABLE -> PulseStatusColors.NeutralText
        RiskTrend.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = statusBgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_extra_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val overallDummyGauge = Gauge(null, score, safeStatus.name)
                onClick(
                    SelectedGaugeState(
                        vulnerabilityTitle,
                        overallDummyGauge,
                        GaugeDictionary.overallScore
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_extra_large))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vulnerabilityTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreGauge(
                    score = score,
                    previousScore = previousScore,
                    isHigherBetter = false, // Lower is better for Risk
                    statusText = safeStatus.name,
                    ringColor = statusColor
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.5f
                        )
                    ),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val trendDummyGauge = Gauge(null, previousScore, safeTrend.name)
                            onClick(
                                SelectedGaugeState(
                                    trendTitle,
                                    trendDummyGauge,
                                    GaugeDictionary.trend
                                )
                            )
                        }
                ) {
                    // 💡 FIXED: Increased internal padding for breathing room
                    Column(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = trendTitle,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron_forward),
                                contentDescription = "Details",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = trendIconRes),
                                contentDescription = "Trend Status",
                                tint = trendColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = safeTrend.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = trendColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // only make it clickable if it overflows or is already expanded
                    .then(
                        if (hasOverflow || expanded) {
                            Modifier.clickable { expanded = !expanded }
                        } else Modifier
                    )
                    .padding(top = dimensionResource(R.dimen.padding_medium)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = interpretationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).animateContentSize(),
                    onTextLayout = { textLayoutResult ->
                        // Check if the text was truncated when it's collapsed
                        if (!expanded) {
                            hasOverflow = textLayoutResult.hasVisualOverflow
                        }
                    }
                )
                // Only show the arrow if it overflows the 2 lines, or if it's currently open
                if (hasOverflow || expanded) {
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    Icon(
                        painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemicGaugeRow(
    title: String,
    gauge: Gauge?,
    definition: GaugeDefinition,
    onClick: (SelectedGaugeState) -> Unit
) {
    if (gauge == null) return

    val safeScore = gauge.riskScore ?: 0

    val gaugeColor = when {
        safeScore >= 80 -> PulseStatusColors.BearishText
        safeScore >= 60 -> PulseStatusColors.WarningText
        safeScore >= 40 -> PulseStatusColors.NeutralText
        else -> PulseStatusColors.BullishText
    }

    val gaugeBgColor = when {
        safeScore >= 80 -> PulseStatusColors.BearishBg
        safeScore >= 60 -> PulseStatusColors.WarningBg
        safeScore >= 40 -> PulseStatusColors.NeutralBg
        else -> PulseStatusColors.BullishBg
    }

    val formattedRawValue = gauge.value?.let {
        String.format(Locale.US, "%.2f", it)
    } ?: "--"

    Card(
        colors = CardDefaults.cardColors(containerColor = gaugeBgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(SelectedGaugeState(title, gauge, definition)) }
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
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.radar_raw_value, formattedRawValue),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                    Text(
                        text = gauge.label ?: stringResource(id = R.string.radar_data_unavailable),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = gaugeColor,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = stringResource(id = R.string.radar_score_out_of_100, safeScore),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                    LinearProgressIndicator(
                        progress = { safeScore / 100f },
                        modifier = Modifier
                            .width(100.dp)
                            .height(dimensionResource(id = R.dimen.progress_bar_height)),
                        color = gaugeColor,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}

@Composable
fun SystemicPlumbingCard(
    gauges: RiskGauges,
    onClick: (SelectedGaugeState) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        modifier = Modifier.fillMaxWidth()
    ) {
        SystemicGaugeRow(
            stringResource(id = R.string.gauge_recession),
            gauges.recession,
            GaugeDictionary.recession,
            onClick
        )
        SystemicGaugeRow(
            stringResource(id = R.string.gauge_foundation),
            gauges.foundation,
            GaugeDictionary.foundation,
            onClick
        )
        SystemicGaugeRow(
            stringResource(id = R.string.gauge_credit),
            gauges.canary,
            GaugeDictionary.canary,
            onClick
        )
        SystemicGaugeRow(
            stringResource(id = R.string.gauge_rotation),
            gauges.rotation,
            GaugeDictionary.rotation,
            onClick
        )
        SystemicGaugeRow(
            stringResource(id = R.string.gauge_growth),
            gauges.growthFear,
            GaugeDictionary.growthFear,
            onClick
        )
    }
}
