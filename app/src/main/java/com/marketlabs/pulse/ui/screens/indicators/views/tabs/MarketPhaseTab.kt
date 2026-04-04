package com.marketlabs.pulse.ui.screens.indicators.views.tabs

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainMarketPhase
import com.marketlabs.pulse.storage.model.indicators.PhaseDetails
import com.marketlabs.pulse.ui.components.widgets.ScoreGauge
import com.marketlabs.pulse.ui.screens.indicators.DictionaryItem
import com.marketlabs.pulse.ui.screens.indicators.IndicatorsDictionary
import com.marketlabs.pulse.ui.screens.indicators.views.AnalyzedAtText
import com.marketlabs.pulse.ui.screens.indicators.views.ContextHeaderCard
import com.marketlabs.pulse.ui.screens.indicators.widgets.UniversalMetricCard
import com.marketlabs.pulse.ui.theme.PulseStatusColors

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

    // 💡 Resolve strings outside the clickable lambda
    val marketPhaseTitle = stringResource(id = R.string.dict_market_phase_score_title)
    val calculatedValue = stringResource(id = R.string.dict_calculated_value, score)
    val formulaFallback = stringResource(id = R.string.dict_formula_fallback)
    val formulaString = stringResource(id = R.string.dict_formula, phase.verdictFormula ?: formulaFallback)
    val marketPhaseRead = stringResource(id = R.string.dict_market_phase_read)

    Card(
        colors = CardDefaults.cardColors(containerColor = headerBgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_extra_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onIndicatorClick(
                    DictionaryItem(
                        title = marketPhaseTitle,
                        subtitle = calculatedValue,
                        definition = formulaString,
                        howToRead = marketPhaseRead
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
                    text = stringResource(id = R.string.market_score),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
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
                    isHigherBetter = true,
                    statusText = phase.marketRegime ?: stringResource(id = R.string.phase_analyzing),
                    ringColor = ringColor
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))

                val marketPostureTitle = stringResource(id = R.string.dict_market_posture_title)
                val defaultPostureDef = stringResource(id = R.string.dict_market_posture_def_fallback)
                val marketPostureRead = stringResource(id = R.string.dict_market_posture_read)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onIndicatorClick(
                                DictionaryItem(
                                    title = marketPostureTitle,
                                    subtitle = phase.verdictCall.name.replace("_", " "),
                                    definition = phase.verdictAction ?: defaultPostureDef,
                                    howToRead = marketPostureRead
                                )
                            )
                        }
                ) {
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
                                text = stringResource(id = R.string.market_posture),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron_forward),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                        Text(
                            text = phase.verdictCall.name.replace("_", " "),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ringColor
                        )
                    }
                }
            }
        }
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