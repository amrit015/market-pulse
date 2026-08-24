package com.marketlabs.pulse.ui.screens.indicators.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainHorizonBlock
import com.marketlabs.pulse.storage.model.indicators.DomainHorizons
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.RiskImpactLevel

/**
 * Stateless -- plain data + lambdas, no ViewModel awareness. All three horizons render as a single
 * stacked, scrollable list of cards (replacing the old Short/Medium/Long tab switcher) -- reads as
 * one continuous briefing rather than three separate views the reader has to tab between.
 *
 * 💡 No metric-name resolution needed here -- `horizons.*.key_drivers[]` was removed from the
 * backend schema/prompt/validation/assembly 2026-08-22, so there's no metric_id left in this
 * shape to resolve to a display name.
 */
@Composable
fun IndicatorHorizonsScreen(
    horizons: DomainHorizons,
    innerPadding: PaddingValues
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val horizonBlocks = listOfNotNull(horizons.shortTerm, horizons.mediumTerm, horizons.longTerm)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = paddingLarge,
            end = paddingLarge,
            top = paddingLarge,
            bottom = paddingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.indicators_horizons_screen_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(horizonBlocks) { horizon ->
            HorizonCard(horizon = horizon)
        }
    }
}

@Composable
private fun HorizonCard(horizon: DomainHorizonBlock) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    // 💡 SYNTHESIS style -- same AI-sourced card family as the executive briefing hero and the
    // pillar scorecard cards, so every AI-authored surface in this domain reads consistently.
    PulseCard(style = PulseCardStyle.SYNTHESIS, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            HorizonCardHeader(horizon)

            Spacer(modifier = Modifier.height(paddingMedium))
            Text(
                text = horizon.posture,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(paddingLarge))
            Text(
                text = horizon.whatThisMeans,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )

            if (!horizon.watchFor.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(paddingLarge))
                Text(
                    text = stringResource(id = R.string.indicators_watch_for_line, horizon.watchFor),
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                )
            }
        }
    }
}

@Composable
private fun HorizonCardHeader(horizon: DomainHorizonBlock) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = horizon.timeWindow.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 💡 "RISK" label -- the pill alone just reads LOW/MEDIUM/HIGH with no context for
            // what that's rating.
            Text(
                text = stringResource(id = R.string.indicators_risk_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            SignalPill(
                text = horizon.riskLevel.label.uppercase(),
                pillColor = horizon.riskLevel.pillColor,
                contentColor = horizon.riskLevel.textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IndicatorHorizonsScreenPreview() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        IndicatorHorizonsScreen(
            horizons = DomainHorizons(
                shortTerm = DomainHorizonBlock(
                    posture = "Tactical Consolidation within a Healthy Uptrend",
                    timeWindow = "Days to weeks",
                    riskLevel = RiskImpactLevel.MEDIUM,
                    whatThisMeans = "The market is currently consolidating within a healthy uptrend, supported by a neutral SPY RSI and a stable VIX.",
                    watchFor = "A break of the SPY RSI below 50 or a sudden spike in the VIX above 18, which could signal a temporary tactical pullback."
                ),
                mediumTerm = DomainHorizonBlock(
                    posture = "Liquidity-Driven Expansion Insulated by Compressed Spreads",
                    timeWindow = "Months to a quarter",
                    riskLevel = RiskImpactLevel.MEDIUM,
                    whatThisMeans = "The market continues to shrug off high valuations because systemic risk remains exceptionally low.",
                    watchFor = null
                ),
                longTerm = null
            ),
            innerPadding = PaddingValues(0.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HorizonCardPreview() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            HorizonCard(
                horizon = DomainHorizonBlock(
                    posture = "Tactical Consolidation within a Healthy Uptrend",
                    timeWindow = "Days to weeks",
                    riskLevel = RiskImpactLevel.MEDIUM,
                    whatThisMeans = "The market is currently consolidating within a healthy uptrend, supported by a neutral SPY RSI and a stable VIX.",
                    watchFor = "A break of the SPY RSI below 50 or a sudden spike in the VIX above 18, which could signal a temporary tactical pullback."
                )
            )
        }
    }
}
