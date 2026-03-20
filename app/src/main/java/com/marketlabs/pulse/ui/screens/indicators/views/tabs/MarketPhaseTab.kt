package com.marketlabs.pulse.ui.screens.indicators.views.tabs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.*
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
                item { PhaseSection("Trend Phase", it, onIndicatorClick) }
            }
            phaseData.healthDetails?.let {
                item { PhaseSection("Health Phase", it, onIndicatorClick) }
            }
            phaseData.riskDetails?.let {
                item { PhaseSection("Risk Phase", it, onIndicatorClick) }
            }
        } else {
            item { Text(stringResource(id = R.string.no_market_phase_data), color = MaterialTheme.colorScheme.onSurfaceVariant) }
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

    Card(
        colors = CardDefaults.cardColors(containerColor = headerBgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_extra_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 💡 DYNAMIC DICTIONARY ITEM for Outer Card (Formula)
                onIndicatorClick(
                    DictionaryItem(
                        title = "Market Phase Score",
                        subtitle = "Calculated Value: $score/100",
                        definition = "Formula: ${phase.verdictFormula ?: "Weighted average of Trend, Health, and Risk."}",
                        howToRead = "A score > 65 indicates a confirmed Bull Market. A score < 45 indicates a Bear Market or severe correction."
                    )
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_extra_large))) {
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
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ScoreGauge(
                    score = score,
                    previousScore = previousScore,
                    isHigherBetter = true,
                    statusText = phase.marketRegime.name.replace("_", " "),
                    ringColor = ringColor
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
                    modifier = Modifier.weight(1f).clickable {
                        // 💡 DYNAMIC DICTIONARY ITEM for Inner Card (Verdict)
                        onIndicatorClick(
                            DictionaryItem(
                                title = "Market Posture",
                                subtitle = phase.verdictCall.name.replace("_", " "),
                                definition = phase.verdictAction ?: "Action not available.",
                                howToRead = "This is the medium-term positioning strategy based on the current regime."
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
                                contentDescription = "Details",
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
fun PhaseSection(title: String, details: PhaseDetails, onIndicatorClick: (DictionaryItem?) -> Unit) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 💡 FIX: Removed .name, and safely handle the nullable String
        Text(
            text = details.overallSignal ?: "UNKNOWN",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = details.summary ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny), bottom = dimensionResource(id = R.dimen.padding_large))
        )

        details.indicators.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), // 💡 ADDED
                horizontalArrangement = Arrangement.spacedBy(paddingMedium)
            ) {
                rowItems.forEach { item ->
                    UniversalMetricCard(
                        title = item.name,
                        value = item.value,
                        changeString = item.changePercent,
                        signalText = item.signal,
                        signalColor = item.signalColor,
                        modifier = Modifier.weight(1f).fillMaxHeight(), // 💡 ADDED fillMaxHeight
                        onClick = { onIndicatorClick(IndicatorsDictionary.getDefinitionFor(item.name)) }
                    )
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f).fillMaxHeight()) // 💡 ADDED fillMaxHeight to spacer too
            }
            Spacer(modifier = Modifier.height(paddingMedium))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = paddingMedium))
    }
}