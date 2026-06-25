//package com.marketlabs.pulse.ui.screens.indicators.views.tabs
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.IntrinsicSize
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.dimensionResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import com.marketlabs.pulse.R
//import com.marketlabs.pulse.storage.model.indicators.DomainMarketAction
//import com.marketlabs.pulse.ui.components.StatusPillState
//import com.marketlabs.pulse.ui.components.UnifiedScoreHeaderCard
//import com.marketlabs.pulse.ui.components.UniversalMetricCard
//import com.marketlabs.pulse.ui.components.bottomSheet.MarketGlossaryBottomSheet
//import com.marketlabs.pulse.ui.screens.indicators.views.AnalyzedAtText
//import com.marketlabs.pulse.ui.screens.indicators.views.ContextHeaderCard
//import com.marketlabs.pulse.utils.glossary.DictionaryItem
//import com.marketlabs.pulse.utils.glossary.IndicatorsDictionary
//import toBgColor
//import toColor
//
//@Composable
//fun MarketActionTab(
//    actionData: DomainMarketAction?,
//    onIndicatorClick: (DictionaryItem?) -> Unit
//) {
//    val guide = IndicatorsDictionary.dashboardPillars[0]
//
//    LazyColumn(
//        contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
//        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_xlarge)),
//        modifier = Modifier.fillMaxSize()
//    ) {
//        item { ContextHeaderCard(guide) }
//
//        if (actionData != null) {
//            item {
//                Column(modifier = Modifier.fillMaxWidth()) {
//                    AnalyzedAtText(actionData.timestamp)
//                    ActionScoreHeader(actionData, onIndicatorClick)
//                }
//            }
//
//            item {
//                Text(
//                    text = stringResource(id = R.string.tactical_metrics_title),
//                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                    color = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
//                )
//
//                val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
//                val fearGreedStr = stringResource(id = R.string.metric_fear_greed)
//                val putCallStr = stringResource(id = R.string.metric_put_call)
//                val rsiStr = stringResource(id = R.string.metric_rsi)
//                val smaStr = stringResource(id = R.string.metric_sma_extension)
//
//                Column {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(IntrinsicSize.Max),
//                        horizontalArrangement = Arrangement.spacedBy(paddingMedium)
//                    ) {
//                        // 💡 Added signalText to pass the Status indicator for each tactical metric
//                        UniversalMetricCard(
//                            title = fearGreedStr,
//                            value = actionData.fearAndGreed.value,
//                            changeString = actionData.fearAndGreed.change,
//                            signalText = actionData.fearAndGreed.signal,
//                            signalColor = actionData.fearAndGreed.signalColor,
//                            modifier = Modifier
//                                .weight(1f)
//                                .fillMaxHeight()
//                        ) {
//                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(fearGreedStr))
//                        }
//                        UniversalMetricCard(
//                            title = putCallStr,
//                            value = actionData.putCallRatio.value,
//                            changeString = actionData.putCallRatio.change,
//                            signalText = actionData.putCallRatio.signal,
//                            signalColor = actionData.putCallRatio.signalColor,
//                            modifier = Modifier
//                                .weight(1f)
//                                .fillMaxHeight()
//                        ) {
//                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(putCallStr))
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(paddingMedium))
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(IntrinsicSize.Max),
//                        horizontalArrangement = Arrangement.spacedBy(paddingMedium)
//                    ) {
//                        UniversalMetricCard(
//                            title = rsiStr,
//                            value = actionData.sp500Rsi.value,
//                            changeString = actionData.sp500Rsi.change,
//                            signalText = actionData.sp500Rsi.signal,
//                            signalColor = actionData.sp500Rsi.signalColor,
//                            modifier = Modifier
//                                .weight(1f)
//                                .fillMaxHeight()
//                        ) {
//                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(rsiStr))
//                        }
//                        UniversalMetricCard(
//                            title = smaStr,
//                            value = actionData.smaExtension.value,
//                            changeString = actionData.smaExtension.change,
//                            signalText = actionData.smaExtension.signal,
//                            signalColor = actionData.smaExtension.signalColor,
//                            modifier = Modifier
//                                .weight(1f)
//                                .fillMaxHeight()
//                        ) {
//                            onIndicatorClick(IndicatorsDictionary.getDefinitionFor(smaStr))
//                        }
//                    }
//                }
//            }
//        } else {
//            item {
//                Text(
//                    stringResource(id = R.string.no_market_action_data),
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun ActionScoreHeader(action: DomainMarketAction, onIndicatorClick: (DictionaryItem?) -> Unit) {
//    var showActionGlossary by remember { mutableStateOf(false) }
//
//    val buyScoreDef = stringResource(id = R.string.dict_buy_score_def)
//
//    UnifiedScoreHeaderCard(
//        title = stringResource(id = R.string.market_action),
//        score = action.actionScore ?: 50,
//        previousScore = action.previousScore ?: action.actionScore ?: 50,
//        isHigherBetter = true,
//        scoreLabel = stringResource(id = R.string.score_label_action),
//        ringColor = action.colorString.toColor(),
//        headerBgColor = action.colorString.toBgColor(),
//        pills = listOf(
//            StatusPillState(
//                text = action.signal.name.replace("_", " "),
//                textColor = action.colorString.toColor()
//            )
//        ),
//        summaryText = action.description ?: "",
//        onClick = { showActionGlossary = true }
//    )
//
//    if (showActionGlossary) {
//        // You will need to import VerdictGlossaryBottomSheet
//        MarketGlossaryBottomSheet(
//            currentAction = action.signal.name.replace("_", " "),
//            description = buyScoreDef,
//            onDismiss = { showActionGlossary = false }
//        )
//    }
//}