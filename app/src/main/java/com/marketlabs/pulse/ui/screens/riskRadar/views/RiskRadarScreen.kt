package com.marketlabs.pulse.ui.screens.riskRadar.views

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.riskRadar.Gauge
import com.marketlabs.pulse.storage.model.riskRadar.RiskGauges
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.model.riskRadar.enums.RiskStatus
import com.marketlabs.pulse.storage.model.riskRadar.enums.RiskTrend
import com.marketlabs.pulse.ui.screens.riskRadar.GaugeDefinition
import com.marketlabs.pulse.ui.screens.riskRadar.GaugeDictionary
import com.marketlabs.pulse.ui.screens.riskRadar.views.widgets.GaugeEducationalBottomSheet
import com.marketlabs.pulse.ui.theme.PulseStatusColors // 💡 NEW: Imported the centralized colors
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
    val safeStatus = data.status ?: RiskStatus.UNKNOWN
    val safeTrend = data.trend ?: RiskTrend.UNKNOWN

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

    val trendBgColor = when (safeTrend) {
        RiskTrend.ACCELERATING -> PulseStatusColors.BearishBg
        RiskTrend.COOLING -> PulseStatusColors.BullishBg
        RiskTrend.STABLE -> PulseStatusColors.NeutralBg
        RiskTrend.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            // --- INNER CARD 1: Vulnerability ---
            Card(
                colors = CardDefaults.cardColors(containerColor = statusBgColor),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        val overallDummyGauge = Gauge(null, data.score ?: 0, safeStatus.name)
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
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = vulnerabilityTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_forward),
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        VulnerabilityScoreChart(
                            score = data.score ?: 0,
                            statusText = safeStatus.name,
                            statusColor = statusColor
                        )
                    }
                }
            }

            // --- INNER CARD 2: Trend (Momentum Shift) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = trendBgColor),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        val trendDummyGauge = Gauge(null, data.previousScore ?: 0, safeTrend.name)
                        onClick(
                            SelectedGaugeState(
                                trendTitle,
                                trendDummyGauge,
                                GaugeDictionary.trend
                            )
                        )
                    }
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
                            text = trendTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_forward),
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val current = data.score ?: 0
                        val prev = data.previousScore ?: 0
                        val delta = current - prev
                        val sign = if (delta > 0) "+" else ""

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = trendIconRes),
                                contentDescription = "Trend",
                                tint = trendColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                            Text(
                                text = "$sign$delta pts",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = trendColor
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                        Text(
                            text = "Previous: $prev",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                        Text(
                            text = safeTrend.name,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = trendColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

        Text(
            text = stringResource(id = R.string.radar_what_this_means).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
        )

        Text(
            text = interpretationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

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
fun VulnerabilityScoreChart(score: Int, statusText: String, statusColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24f
                val sweepAngle = (score / 100f) * 360f

                val inset = strokeWidth / 2
                val arcSize = androidx.compose.ui.geometry.Size(
                    width = size.width - strokeWidth,
                    height = size.height - strokeWidth
                )

                // Faint Background Track
                drawArc(
                    color = statusColor.copy(alpha = 0.15f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Foreground Progress Arc
                drawArc(
                    color = statusColor,
                    startAngle = 270f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = statusColor
        )
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