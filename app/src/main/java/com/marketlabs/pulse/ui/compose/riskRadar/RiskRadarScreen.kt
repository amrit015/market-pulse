package com.marketlabs.pulse.ui.compose.riskRadar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.marketlabs.pulse.ui.theme.AlertRed
import com.marketlabs.pulse.ui.theme.PulseGold
import com.marketlabs.pulse.ui.theme.PulseOrange
import com.marketlabs.pulse.ui.theme.SuccessGreen
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

        item { RadarHeaderSection(data.lastSyncedTimestamp) }

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

    // 💡 NEW: Format raw value to exactly 2 decimal places
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
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Educational Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gauge.label ?: stringResource(id = R.string.radar_data_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = gaugeColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.radar_raw_value, formattedRawValue),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    // 💡 NEW: Upgraded Typography for explicit score
                    Text(
                        text = stringResource(id = R.string.radar_score_out_of_100, safeScore),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // 💡 NEW: Restyled Progress Bar
                    LinearProgressIndicator(
                        progress = { safeScore / 100f },
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.progress_bar_width_large))
                            .height(dimensionResource(id = R.dimen.progress_bar_height)),
                        color = gaugeColor,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round,
                        // Add these two lines to override the new Material 3 defaults:
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
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
        ) {
            Text(
                text = selectedState.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.radar_current_status),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier.padding(start = 5.dp),
                        text = selectedState.gauge.label
                            ?: stringResource(id = R.string.radar_unknown),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.radar_what_it_measures),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = selectedState.definition.whatItMeasures,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.radar_how_to_read_it),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            selectedState.definition.brackets.forEach { (bracketName, description) ->
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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

@Composable
private fun ScoreBoardCard(
    data: RiskRadar,
    // 💡 NEW: Add onClick parameter to trigger the bottom sheet
    onClick: (SelectedGaugeState) -> Unit
) {
    val safeStatus = data.status ?: RiskStatus.UNKNOWN

    val statusColor = when (safeStatus) {
        RiskStatus.SAFE -> SuccessGreen
        RiskStatus.STABLE -> PulseGold
        RiskStatus.CAUTION -> PulseOrange
        RiskStatus.DANGER -> AlertRed
        RiskStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val interpretationText = when (safeStatus) {
        RiskStatus.SAFE -> stringResource(id = R.string.radar_action_safe)
        RiskStatus.STABLE -> stringResource(id = R.string.radar_action_stable)
        RiskStatus.CAUTION -> stringResource(id = R.string.radar_action_caution)
        RiskStatus.DANGER -> stringResource(id = R.string.radar_action_danger)
        RiskStatus.UNKNOWN -> stringResource(id = R.string.radar_data_unavailable)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            // 💡 NEW: Make the Scoreboard clickable and pass a dummy Gauge object
            .clickable {
                val overallDummyGauge = Gauge(
                    value = null, // Raw value doesn't apply here
                    riskScore = data.score ?: 0,
                    label = safeStatus.name
                )
                onClick(
                    SelectedGaugeState(
                        title = "Vulnerability Score",
                        gauge = overallDummyGauge,
                        definition = GaugeDictionary.overallScore
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_xlarge)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 💡 NEW: Wrap the Title and Info Icon in a Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.radar_vulnerability_score).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = PulseOrange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Educational Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
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

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            val trendText = data.trend?.name ?: stringResource(id = R.string.radar_unknown)
            val prevScoreText = data.previousScore?.toString() ?: "--"

            Text(
                text = stringResource(id = R.string.radar_trend_prev, trendText, prevScoreText),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.radar_what_this_means).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
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