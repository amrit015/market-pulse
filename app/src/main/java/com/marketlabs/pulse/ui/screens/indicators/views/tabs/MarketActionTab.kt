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
                            changeString = actionData.fearAndGreed.change,
                            signalColor = actionData.fearAndGreed.signalColor,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(fearGreedStr))
                        }
                        UniversalMetricCard(
                            title = putCallStr,
                            value = actionData.putCallRatio.value,
                            changeString = actionData.putCallRatio.change,
                            signalColor = actionData.putCallRatio.signalColor,
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
                            changeString = actionData.sp500Rsi.change,
                            signalColor = actionData.sp500Rsi.signalColor,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(rsiStr))
                        }
                        UniversalMetricCard(
                            title = smaStr,
                            value = actionData.smaExtension.value,
                            changeString = actionData.smaExtension.change,
                            signalColor = actionData.smaExtension.signalColor,
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

    // 💡 Resolve strings outside the clickable lambda
    val buyScoreTitle = stringResource(id = R.string.dict_buy_score_title)
    val calculatedValue = stringResource(id = R.string.dict_calculated_value, score)
    val buyScoreDef = stringResource(id = R.string.dict_buy_score_def)
    val buyScoreRead = stringResource(id = R.string.dict_buy_score_read)

    Card(
        colors = CardDefaults.cardColors(containerColor = headerBgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_extra_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onIndicatorClick(
                    DictionaryItem(
                        title = buyScoreTitle,
                        subtitle = calculatedValue,
                        definition = buyScoreDef,
                        howToRead = buyScoreRead
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
                    contentDescription = null,
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

                // 💡 Resolve internal card strings
                val actionSignalTitle = stringResource(id = R.string.dict_action_signal_title)
                val defaultActionDef = stringResource(id = R.string.dict_action_signal_def_fallback)
                val actionSignalRead = stringResource(id = R.string.dict_action_signal_read)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
                    modifier = Modifier.weight(1f).clickable {
                        onIndicatorClick(
                            DictionaryItem(
                                title = actionSignalTitle,
                                subtitle = action.signal.name.replace("_", " "),
                                definition = action.description ?: defaultActionDef,
                                howToRead = actionSignalRead
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
                                contentDescription = null,
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
                                    text = stringResource(id = R.string.score_change_pts, changePrefix, change),
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