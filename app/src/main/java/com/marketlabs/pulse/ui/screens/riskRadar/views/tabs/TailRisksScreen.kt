package com.marketlabs.pulse.ui.screens.riskRadar.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskFactor
import com.marketlabs.pulse.ui.theme.AlertRed // 💡 Imported your pure red from Color.kt
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TailRisksScreen(
    data: MarketRiskAssessment?,
    scaffoldPadding: PaddingValues
) {
    if (data == null || data.risks.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.no_tail_risks_available),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    // 1. Extract the exact text style you are using
    val textStyle = MaterialTheme.typography.headlineMedium

    // 2. Convert its font size (sp) into a Compose dimension (dp)
    val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingLarge,
            bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
            start = paddingLarge,
            end = paddingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_xlarge))
    ) {
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_engine_ai_sparkles),
                        contentDescription = "Analysis Engine",
                        tint = MaterialTheme.colorScheme.onBackground,
                        // 3. Apply the calculated size here
                        modifier = Modifier.size(iconSize)
                    )

                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

                    Text(
                        text = stringResource(id = R.string.risk_assessment),
                        // 4. Use the exact same style reference here
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onBackground
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
                        bottom = paddingLarge
                    )
                )

                data.summary?.let { summaryText ->
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 💡 NEW: Expandable Source Narrative Section
                data.sourceNarrative?.let { narrative ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.animateContentSize()) {
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                            Text(
                                text = stringResource(id = R.string.source_narrative_title), // Add to strings: "Source Narrative"
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
                            text = if (isExpanded) stringResource(id = R.string.action_show_less) else stringResource(
                                id = R.string.action_read_more
                            ), // Add to strings: "Show Less" and "Read More"
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

        items(data.risks) { risk ->
            TailRiskCard(risk = risk)
        }
    }
}

@Composable
fun TailRiskCard(risk: MarketRiskFactor) {
    val impactTextColor = when (risk.impactLevel) {
        RiskImpactLevel.EXTREME -> AlertRed
        RiskImpactLevel.HIGH -> PulseStatusColors.BearishText
        RiskImpactLevel.MEDIUM -> PulseStatusColors.WarningText
        RiskImpactLevel.LOW -> PulseStatusColors.BullishText
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val impactBgColor = when (risk.impactLevel) {
        RiskImpactLevel.EXTREME -> AlertRed.copy(alpha = 0.15f)
        RiskImpactLevel.HIGH -> PulseStatusColors.BearishBg
        RiskImpactLevel.MEDIUM -> PulseStatusColors.WarningBg
        RiskImpactLevel.LOW -> PulseStatusColors.BullishBg
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left Accent Bar uses the vibrant text color
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                // Metadata Pills (Category & Impact)
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {

                    // Category Pill (Matches Summary Screen Macro Card tags perfectly)
                    if (risk.category != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                        ) {
                            Text(
                                text = risk.category.uppercase(),
                                // 💡 CHANGED: Removed the Bold override to match Summary Screen exactly
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                                    vertical = dimensionResource(id = R.dimen.padding_tiny)
                                )
                            )
                        }
                    }

                    // Impact Level Pill
                    if (risk.impactLevel != null) {
                        Surface(
                            color = impactBgColor,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                        ) {
                            Text(
                                text = risk.impactLevel.label.uppercase(),
                                // 💡 CHANGED: Removed the Bold override to match Summary Screen exactly
                                style = MaterialTheme.typography.labelSmall,
                                color = impactTextColor,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                                    vertical = dimensionResource(id = R.dimen.padding_tiny)
                                )
                            )
                        }
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