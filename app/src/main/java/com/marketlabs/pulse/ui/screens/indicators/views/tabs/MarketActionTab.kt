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
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainMarketAction
import com.marketlabs.pulse.ui.components.widgets.ScoreGauge
import com.marketlabs.pulse.ui.screens.indicators.DictionaryItem
import com.marketlabs.pulse.ui.screens.indicators.IndicatorsDictionary
import com.marketlabs.pulse.ui.screens.indicators.views.AnalyzedAtText
import com.marketlabs.pulse.ui.screens.indicators.views.ContextHeaderCard
import com.marketlabs.pulse.ui.screens.indicators.views.toBgColor
import com.marketlabs.pulse.ui.screens.indicators.views.toColor
import com.marketlabs.pulse.ui.screens.indicators.widgets.UniversalMetricCard

@Composable
fun MarketActionTab(
    actionData: DomainMarketAction?,
    onIndicatorClick: (DictionaryItem?) -> Unit
) {
    val guide = IndicatorsDictionary.dashboardPillars[0]

    LazyColumn(
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_xlarge)),
        modifier = Modifier.fillMaxSize()
    ) {
        item { ContextHeaderCard(guide) }

        if (actionData != null) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnalyzedAtText(actionData.timestamp)
                    ActionScoreHeader(actionData, onIndicatorClick)
                }
            }

            item {
                Text(
                    text = stringResource(id = R.string.tactical_metrics_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                )

                val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
                val fearGreedStr = stringResource(id = R.string.metric_fear_greed)
                val putCallStr = stringResource(id = R.string.metric_put_call)
                val rsiStr = stringResource(id = R.string.metric_rsi)
                val smaStr = stringResource(id = R.string.metric_sma_extension)

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(paddingMedium)
                    ) {
                        UniversalMetricCard(
                            title = fearGreedStr,
                            value = actionData.fearAndGreed.value,
                            signalColor = actionData.fearAndGreed.signalColor, // 💡 ADDED
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(fearGreedStr))
                        }
                        UniversalMetricCard(
                            title = putCallStr,
                            value = actionData.putCallRatio.value,
                            signalColor = actionData.putCallRatio.signalColor, // 💡 ADDED
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(putCallStr))
                        }
                    }
                    Spacer(modifier = Modifier.height(paddingMedium))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(paddingMedium)
                    ) {
                        UniversalMetricCard(
                            title = rsiStr,
                            value = actionData.sp500Rsi.value,
                            signalColor = actionData.sp500Rsi.signalColor, // 💡 ADDED
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(rsiStr))
                        }
                        UniversalMetricCard(
                            title = smaStr,
                            value = actionData.smaExtension.value,
                            signalColor = actionData.smaExtension.signalColor, // 💡 ADDED
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(smaStr))
                        }
                    }
                }
            }
        } else {
            item { Text(stringResource(id = R.string.no_market_action_data), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun ActionScoreHeader(action: DomainMarketAction, onIndicatorClick: (DictionaryItem?) -> Unit) {
    val score = action.actionScore ?: 50
    val previousScore = action.previousScore ?: score
    val ringColor = action.colorString.toColor()
    val headerBgColor = action.colorString.toBgColor()

    Card(
        colors = CardDefaults.cardColors(containerColor = headerBgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_extra_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 💡 DYNAMIC DICTIONARY ITEM for Outer Card (Score)
                onIndicatorClick(
                    DictionaryItem(
                        title = "Buy Score",
                        subtitle = "Calculated Value: $score/100",
                        definition = "This score aggregates Fear & Greed, Put/Call Ratios, Momentum, and Price extensions into a single 0-100 value.",
                        howToRead = "The higher the score, the better the buying opportunity (Panic). The lower the score, the higher the risk of a pullback (Euphoria)."
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
                    text = stringResource(id = R.string.market_action),
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
                    statusText = stringResource(id = R.string.buy_score_label),
                    ringColor = ringColor
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
                    modifier = Modifier.weight(1f).clickable {
                        // 💡 DYNAMIC DICTIONARY ITEM for Inner Card (Signal)
                        onIndicatorClick(
                            DictionaryItem(
                                title = "Action Signal",
                                subtitle = action.signal.name.replace("_", " "),
                                definition = action.description ?: "Current tactical setup.",
                                howToRead = "This is the system's recommended short-term action based on current market sentiment and over-extensions."
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
                                text = stringResource(id = R.string.signal_label),
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
                            text = action.signal.name.replace("_", " "),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ringColor
                        )

                        action.scoreChange?.let { change ->
                            if (change != 0) {
                                val changePrefix = if (change > 0) "+" else ""
                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_micro)))
                                Text(
                                    text = "$changePrefix$change pts",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}