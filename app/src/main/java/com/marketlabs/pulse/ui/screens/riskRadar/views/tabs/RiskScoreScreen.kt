package com.marketlabs.pulse.ui.screens.riskRadar.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.riskRadar.Gauge
import com.marketlabs.pulse.storage.model.riskRadar.RiskGauges
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.ui.components.StatusPillState
import com.marketlabs.pulse.ui.components.UnifiedScoreHeaderCard
import com.marketlabs.pulse.ui.components.UniversalGaugeCard
import com.marketlabs.pulse.ui.components.bottomSheet.GaugeEducationalBottomSheet
import com.marketlabs.pulse.ui.components.bottomSheet.RiskGlossaryBottomSheet
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import com.marketlabs.pulse.utils.enums.RiskStatus
import com.marketlabs.pulse.utils.enums.RiskTrend
import com.marketlabs.pulse.utils.glossary.GaugeDefinition
import com.marketlabs.pulse.utils.glossary.GaugeDictionary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SelectedGaugeState(
    val title: String,
    val gauge: Gauge,
    val definition: GaugeDefinition
)

@Composable
fun RiskScoreScreen(
    data: RiskRadar,
    scaffoldPadding: PaddingValues
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingXLarge = dimensionResource(id = R.dimen.padding_xlarge)
    var selectedGaugeState by remember { mutableStateOf<SelectedGaugeState?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingLarge,
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
    // 1. Extract the exact text style you are using
    val textStyle = MaterialTheme.typography.headlineMedium

    // 2. Convert its font size (sp) into a Compose dimension (dp)
    val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_engine_quant),
                contentDescription = "Analysis Engine",
                tint = MaterialTheme.colorScheme.onBackground,
                // 3. Apply the calculated size here
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            Text(
                text = stringResource(id = R.string.tab_risk_radar),
                // 4. Use the exact same style reference here
                style = textStyle,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
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
    var showRiskGlossary by remember { mutableStateOf(false) }
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

    UnifiedScoreHeaderCard(
        title = stringResource(id = R.string.radar_title_vulnerability),
        score = score,
        previousScore = previousScore,
        isHigherBetter = false, // Lower is better for Risk
        scoreLabel = stringResource(id = R.string.score_label_risk),
        ringColor = statusColor, // Kept from your existing logic
        headerBgColor = statusBgColor, // Kept from your existing logic
        pills = listOf(
            StatusPillState(
                text = safeStatus.name,
                textColor = statusColor
            ),
            StatusPillState(
                text = safeTrend.name,
                textColor = trendColor,
                iconRes = trendIconRes // Uses the trending_up/down icon you already defined
            )
        ),
        summaryText = interpretationText, // Kept from your existing logic
        onClick = { showRiskGlossary = true }
    )

    if (showRiskGlossary) {
        // You will need to import RiskGlossaryBottomSheet
        RiskGlossaryBottomSheet(
            currentStatus = safeStatus.name,
            currentTrend = safeTrend.name,
            onDismiss = { showRiskGlossary = false }
        )
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

    val formattedRawValue = gauge.value?.let {
        String.format(Locale.US, "%.2f", it)
    } ?: "--"

    // 💡 Restored your specific PulseStatusColors for Text
    val gaugeColor = when {
        safeScore >= 80 -> PulseStatusColors.BearishText
        safeScore >= 60 -> PulseStatusColors.WarningText
        safeScore >= 40 -> PulseStatusColors.NeutralText
        else -> PulseStatusColors.BullishText
    }

    // 💡 Restored your specific PulseStatusColors for Background
    val gaugeBgColor = when {
        safeScore >= 80 -> PulseStatusColors.BearishBg
        safeScore >= 60 -> PulseStatusColors.WarningBg
        safeScore >= 40 -> PulseStatusColors.NeutralBg
        else -> PulseStatusColors.BullishBg
    }

    UniversalGaugeCard(
        title = title,
        value = formattedRawValue,
        score = safeScore,
        signalText = gauge.label,
        baseColor = gaugeColor,
        bgColor = gaugeBgColor,
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(SelectedGaugeState(title, gauge, definition)) }
    )
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
