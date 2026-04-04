package com.marketlabs.pulse.ui.screens.indicators.views.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainMarketPhase
import com.marketlabs.pulse.storage.model.indicators.PhaseDetails
import com.marketlabs.pulse.ui.components.StatusPillState
import com.marketlabs.pulse.ui.components.UnifiedScoreHeaderCard
import com.marketlabs.pulse.ui.components.UniversalMetricCard
import com.marketlabs.pulse.ui.components.bottomSheet.MarketGlossaryBottomSheet
import com.marketlabs.pulse.ui.screens.indicators.views.AnalyzedAtText
import com.marketlabs.pulse.ui.screens.indicators.views.ContextHeaderCard
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import com.marketlabs.pulse.utils.glossary.DictionaryItem
import com.marketlabs.pulse.utils.glossary.IndicatorsDictionary

@Composable
fun MarketPhaseTab(
    phaseData: DomainMarketPhase?,
    onIndicatorClick: (DictionaryItem?) -> Unit
) {
    val guide = IndicatorsDictionary.dashboardPillars[1]

    val trendTitle = stringResource(id = R.string.phase_title_trend)
    val healthTitle = stringResource(id = R.string.phase_title_health)
    val riskTitle = stringResource(id = R.string.phase_title_risk)
    val valTitle = stringResource(id = R.string.phase_title_valuation)

    LazyColumn(
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_xlarge)),
        modifier = Modifier.fillMaxSize()
    ) {
        item { ContextHeaderCard(guide) }

        if (phaseData != null) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnalyzedAtText(phaseData.timestamp)
                    IndicatorScoreHeader(phaseData, onIndicatorClick)
                }
            }

            phaseData.trendDetails?.let {
                item { PhaseSection(trendTitle, it, onIndicatorClick) }
            }
            phaseData.healthDetails?.let {
                item { PhaseSection(healthTitle, it, onIndicatorClick) }
            }
            phaseData.riskDetails?.let {
                item { PhaseSection(riskTitle, it, onIndicatorClick) }
            }
            phaseData.valuationDetails?.let {
                item { PhaseSection(valTitle, it, onIndicatorClick) }
            }
        } else {
            item {
                Text(
                    stringResource(id = R.string.no_market_phase_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IndicatorScoreHeader(phase: DomainMarketPhase, onIndicatorClick: (DictionaryItem?) -> Unit) {

    var showVerdictGlossary by remember { mutableStateOf(false) }

    val score = phase.verdictScore ?: 0
    val previousScore = phase.previousScore ?: score
    val ringColor = when {
        score >= 65 -> PulseStatusColors.BullishText
        score >= 45 -> PulseStatusColors.NeutralText
        else -> PulseStatusColors.BearishText
    }
    val headerBgColor = when {
        score >= 65 -> PulseStatusColors.BullishBg
        score >= 45 -> PulseStatusColors.NeutralBg
        else -> PulseStatusColors.BearishBg
    }

    val marketPhaseRead = stringResource(id = R.string.dict_market_phase_read)

    UnifiedScoreHeaderCard(
        title = stringResource(id = R.string.market_score),
        score = score,
        previousScore = previousScore,
        isHigherBetter = true,
        scoreLabel = stringResource(id = R.string.score_label_phase),
        ringColor = ringColor, // Kept from your existing logic
        headerBgColor = headerBgColor, // Kept from your existing logic
        pills = listOf(
            StatusPillState(
                text = phase.marketRegime ?: "ANALYZING", // todo: use this strategy
                textColor = ringColor
            ),
            StatusPillState(
                text = phase.tradingCall?.label ?: "",
                textColor = ringColor
            )
        ),
        summaryText = phase.verdictAction ?: "",
        onClick = { showVerdictGlossary = true }
    )

    if (showVerdictGlossary) {
        MarketGlossaryBottomSheet(
            currentRegime = phase.marketRegime,
            currentCall = phase.tradingCall?.label,
            description = marketPhaseRead,
            onDismiss = { showVerdictGlossary = false }
        )
    }
}

@Composable
fun PhaseSection(
    title: String,
    details: PhaseDetails,
    onIndicatorClick: (DictionaryItem?) -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = details.overallSignal ?: stringResource(id = R.string.phase_unknown),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = details.summary ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.padding_tiny),
                bottom = dimensionResource(id = R.dimen.padding_large)
            )
        )

        details.indicators.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(paddingMedium)
            ) {
                rowItems.forEach { item ->
                    UniversalMetricCard(
                        title = item.name,
                        value = item.value,
                        changeString = item.changePercent,
                        signalText = item.signal,
                        signalColor = item.signalColor,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { onIndicatorClick(IndicatorsDictionary.getDefinitionFor(item.name)) }
                    )
                }
                if (rowItems.size == 1) Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            Spacer(modifier = Modifier.height(paddingMedium))
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = paddingMedium)
        )
    }
}