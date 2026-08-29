package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskFactor
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.MetricInfoAction
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TailRisksSection(risksData: MarketRiskAssessment) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        RiskAssessmentHeader(risksData)

        if (!risksData.risks.isNullOrEmpty()) {
            risksData.risks.forEach { risk ->
                TailRiskCard(risk = risk)
            }
        } else {
            Text(
                text = stringResource(id = R.string.no_tail_risks_available),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiskAssessmentHeader(data: MarketRiskAssessment) {
    // 💡 CHANGED: Now identical to the WeeklyPlaybook header style
    val textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ai_sparkle_filled),
                contentDescription = "Analysis Engine",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = stringResource(id = R.string.risk_assessment),
                style = textStyle,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            MetricInfoAction(
                title = stringResource(id = R.string.risk_assessment),
                description = stringResource(id = R.string.risk_assessment_description)
            )
        }

        val date = data.lastUpdated?.let { Date(it) } ?: Date()
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = stringResource(id = R.string.analyzed_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.padding_micro),
                bottom = dimensionResource(id = R.dimen.padding_large)
            )
        )

        data.summary?.let { summaryText ->
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Expandable Source Narrative Section
        data.sourceNarrative?.let { narrative ->
            var isExpanded by remember { mutableStateOf(false) }

            Column(modifier = Modifier.animateContentSize()) {
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    Text(
                        text = stringResource(id = R.string.source_narrative_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                    Text(
                        text = narrative,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                Text(
                    text = if (isExpanded) stringResource(id = R.string.action_show_less) else stringResource(id = R.string.action_read_more),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = dimensionResource(id = R.dimen.padding_tiny))
                )
            }
        }
    }
}

@Composable
private fun TailRiskCard(risk: MarketRiskFactor) {
    val pulseColors = LocalPulseColors.current

    // 💡 The old flat, non-theme-aware AlertRed constant is gone. EXTREME used to get its own raw
    // red, kept visually distinct from HIGH's theme-aware bearish red. This app's signal system
    // has exactly four tiers (bullish/bearish/neutral/warning) and no fifth "beyond bearish" tier,
    // so EXTREME and HIGH now collapse to the same signalBearishText/.pill pair. A real visual
    // change worth knowing about: EXTREME risk cards no longer look distinctly redder than HIGH ones.
    val impactTextColor = when (risk.impactLevel) {
        RiskImpactLevel.EXTREME, RiskImpactLevel.HIGH -> pulseColors.signalBearishText
        RiskImpactLevel.MEDIUM -> pulseColors.signalWarningText
        RiskImpactLevel.LOW -> pulseColors.signalBullishText
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val impactBgColor = when (risk.impactLevel) {
        RiskImpactLevel.EXTREME, RiskImpactLevel.HIGH -> pulseColors.signalBearishPill
        RiskImpactLevel.MEDIUM -> pulseColors.signalWarningPill
        RiskImpactLevel.LOW -> pulseColors.signalBullishPill
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    // 💡 DATA style -- was SYNTHESIS (an AI-assessed tail risk, treated as AI content). This app's
    // darker SYNTHESIS background is now reserved for the one AI briefing/verdict hero card per
    // screen, so a risk-list card reads with the same background every other data-display card in
    // the app uses. Replaces the old `secondaryContainer.copy(alpha = 0.4f)` leftover from before
    // this app had its own token system. `elevation = 0.dp` dropped along with it -- PulseCard
    // never adds elevation, matching this app's flat, no-shadow convention everywhere else.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(dimensionResource(id = R.dimen.border_medium))
                    .background(impactTextColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Text(
                    text = risk.riskFactor ?: stringResource(id = R.string.unknown_risk),
                    // 💡 titleSmall (15sp, semi-bold), not titleMedium.Bold (17sp, bold) -- the
                    // consistent title tier every curated/AI-content card title uses now, separate
                    // from DATA-style cards (Equities, VIX, Indicators), which keep their bold
                    // 17sp title.
                    style = MaterialTheme.typography.titleSmall,
                    // 💡 Card titles are always onSurface (dark-on-light/white-on-dark) across
                    // this app -- same treatment as Equities/AI/News cards.
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                    if (risk.category != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                        ) {
                            Text(
                                text = risk.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                                    vertical = dimensionResource(id = R.dimen.padding_tiny)
                                )
                            )
                        }
                    }

                    if (risk.impactLevel != null) {
                        SignalPill(
                            text = risk.impactLevel.label.uppercase(),
                            pillColor = impactBgColor,
                            contentColor = impactTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                Text(
                    text = risk.context ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}