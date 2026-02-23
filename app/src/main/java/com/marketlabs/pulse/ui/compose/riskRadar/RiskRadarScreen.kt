package com.marketlabs.pulse.ui.compose.riskRadar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.riskRadar.Gauge
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.model.riskRadar.enums.RiskStatus
import com.marketlabs.pulse.storage.model.riskRadar.enums.RiskTrend
import com.marketlabs.pulse.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * State holder for the currently selected gauge.
 * This dictates what information the Bottom Sheet displays when opened.
 */
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

    // Tracks which element was clicked to populate the educational bottom sheet
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
        // 1. Header showing last updated time
        item { RadarHeaderSection(data.lastSyncedTimestamp) }

        // 2. The Main Dashboard Scoreboard
        item {
            ScoreBoardCard(
                data = data,
                onClick = { selectedGaugeState = it }
            )
        }

        // 3. Section Title for Individual Metrics
        item {
            Text(
                text = stringResource(id = R.string.radar_systemic_plumbing).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 4. Render all the individual gauges if data exists
        data.gauges?.let { gauges ->
            item {
                GaugeCard(
                    title = stringResource(id = R.string.gauge_recession),
                    gauge = gauges.recession,
                    definition = GaugeDictionary.recession,
                    onClick = { selectedGaugeState = it }
                )
            }
            item {
                GaugeCard(
                    title = stringResource(id = R.string.gauge_foundation),
                    gauge = gauges.foundation,
                    definition = GaugeDictionary.foundation,
                    onClick = { selectedGaugeState = it }
                )
            }
            item {
                GaugeCard(
                    title = stringResource(id = R.string.gauge_credit),
                    gauge = gauges.canary,
                    definition = GaugeDictionary.canary,
                    onClick = { selectedGaugeState = it }
                )
            }
            item {
                GaugeCard(
                    title = stringResource(id = R.string.gauge_rotation),
                    gauge = gauges.rotation,
                    definition = GaugeDictionary.rotation,
                    onClick = { selectedGaugeState = it }
                )
            }
            item {
                GaugeCard(
                    title = stringResource(id = R.string.gauge_growth),
                    gauge = gauges.growthFear,
                    definition = GaugeDictionary.growthFear,
                    onClick = { selectedGaugeState = it }
                )
            }
        }
    }

    // Safely render the bottom sheet only when an item has been clicked
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
            text = stringResource(id = R.string.pulse_updated_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    // Assign colors based on exact Risk Status
    val statusColor = when (safeStatus) {
        RiskStatus.SAFE -> SuccessGreen
        RiskStatus.STABLE -> PulseGold
        RiskStatus.CAUTION -> PulseOrange
        RiskStatus.DANGER -> AlertRed
        RiskStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // Assign text interpretation based on Risk Status
    val interpretationText = when (safeStatus) {
        RiskStatus.SAFE -> stringResource(id = R.string.radar_action_safe)
        RiskStatus.STABLE -> stringResource(id = R.string.radar_action_stable)
        RiskStatus.CAUTION -> stringResource(id = R.string.radar_action_caution)
        RiskStatus.DANGER -> stringResource(id = R.string.radar_action_danger)
        RiskStatus.UNKNOWN -> stringResource(id = R.string.radar_data_unavailable)
    }

    // Trend Visuals
    val trendIconRes = when (safeTrend) {
        RiskTrend.ACCELERATING -> R.drawable.ic_trending_up
        RiskTrend.COOLING -> R.drawable.ic_trending_down
        else -> R.drawable.ic_trending_flat
    }

    val trendColor = when (safeTrend) {
        RiskTrend.ACCELERATING -> AlertRed
        RiskTrend.COOLING -> SuccessGreen
        RiskTrend.STABLE -> PulseLightGray
        RiskTrend.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_xlarge)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ==========================================
            // TOP: OVERALL SCORE SECTION
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val overallDummyGauge = Gauge(null, data.score ?: 0, safeStatus.name)
                        onClick(SelectedGaugeState(
                            title = vulnerabilityTitle,
                            gauge = overallDummyGauge,
                            definition = GaugeDictionary.overallScore
                        ))
                    }
                    .padding(vertical = dimensionResource(id = R.dimen.padding_tiny))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.radar_title_vulnerability).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = stringResource(id = R.string.radar_content_desc_info),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium))
                    )
                }

                Text(
                    text = data.score?.toString() ?: "--",
                    style = MaterialTheme.typography.headlineLarge,
                    color = statusColor
                )

                Text(
                    text = safeStatus.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            // ==========================================
            // MIDDLE: TREND SECTION (Mini-Scoreboard)
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val trendDummyGauge = Gauge(null, data.previousScore ?: 0, safeTrend.name)
                        onClick(SelectedGaugeState(
                            title = trendTitle,
                            gauge = trendDummyGauge,
                            definition = GaugeDictionary.trend
                        ))
                    }
                    .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            ) {
                // Trend Header (Matches Vulnerability header but smaller font)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.radar_title_trend).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = stringResource(id = R.string.radar_content_desc_info),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium))
                    )
                }

                // Trend Value + Arrow Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
                ) {
                    Text(
                        text = safeTrend.name,
                        style = MaterialTheme.typography.titleMedium, // Scaled down from the main score
                        color = trendColor
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    Icon(
                        painter = painterResource(id = trendIconRes),
                        contentDescription = "Trend Icon",
                        tint = trendColor,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large))
                    )

                }

                // Previous Score Label
                val prevScoreText = data.previousScore?.toString() ?: "--"
                Text(
                    text = stringResource(id = R.string.radar_prev_score, prevScoreText),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            // ==========================================
            // BOTTOM: ACTION PLAN SECTION
            // ==========================================
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
        }
    }
}

@Composable
private fun GaugeCard(
    title: String,
    gauge: Gauge?,
    definition: GaugeDefinition,
    onClick: (SelectedGaugeState) -> Unit
) {
    if (gauge == null) return

    val safeScore = gauge.riskScore ?: 0
    val gaugeColor = when {
        safeScore >= 80 -> AlertRed
        safeScore >= 60 -> PulseOrange
        safeScore >= 40 -> PulseGold
        else -> SuccessGreen
    }

    // Format raw value strictly to 2 decimal places to maintain clean UI alignment
    val formattedRawValue = gauge.value?.let {
        String.format(Locale.US, "%.2f", it)
    } ?: "--"

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.padding_small))
            .clickable { onClick(SelectedGaugeState(title, gauge, definition)) }
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            // Header Row: Contains Gauge Title and Info Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = stringResource(id = R.string.radar_content_desc_info),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))

            // Data Row: Contains status label, raw value, and the visual progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left side: Text Data
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gauge.label ?: stringResource(id = R.string.radar_data_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = gaugeColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                    Text(
                        text = stringResource(id = R.string.radar_raw_value, formattedRawValue),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Right side: Visual Gauge Data
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(id = R.string.radar_score_out_of_100, safeScore),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
                    )

                    LinearProgressIndicator(
                        progress = { safeScore / 100f },
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.progress_bar_width_large))
                            .height(dimensionResource(id = R.dimen.progress_bar_height)),
                        color = gaugeColor,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaugeEducationalBottomSheet(
    selectedState: SelectedGaugeState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.padding_large))
        ) {
            // Sheet Title
            Text(
                text = selectedState.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            // Highlighted Current Status Banner
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(id = R.dimen.padding_medium)),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
            ) {
                Row(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.radar_current_status),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium)),
                        text = selectedState.gauge.label ?: stringResource(id = R.string.radar_unknown),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))

            // Educational Concept Description
            Text(
                text = stringResource(id = R.string.radar_what_it_measures),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            Text(
                text = selectedState.definition.whatItMeasures,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))

            // Evaluation Brackets Breakdown
            Text(
                text = stringResource(id = R.string.radar_how_to_read_it),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            selectedState.definition.brackets.forEach { (bracketName, description) ->
                Row(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))) {
                    Box(
                        modifier = Modifier
                            .padding(top = dimensionResource(id = R.dimen.padding_tiny))
                            .size(dimensionResource(id = R.dimen.bullet_size))
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                    Column {
                        Text(
                            text = bracketName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}