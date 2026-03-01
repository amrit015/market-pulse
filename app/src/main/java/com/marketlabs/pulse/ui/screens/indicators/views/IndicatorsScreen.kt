package com.marketlabs.pulse.ui.screens.indicators.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.IndicatorItem
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.model.indicators.PhaseDetails
import com.marketlabs.pulse.storage.model.indicators.PhaseSummary
import com.marketlabs.pulse.storage.model.indicators.enums.SignalColor
import com.marketlabs.pulse.ui.screens.indicators.DictionaryItem
import com.marketlabs.pulse.ui.screens.indicators.FrameworkSheet
import com.marketlabs.pulse.ui.screens.indicators.IndicatorDetailSheet
import com.marketlabs.pulse.ui.screens.indicators.IndicatorsDictionary

val VerdictBuyText = Color(0xFF2E7D32)
val VerdictSellText = Color(0xFFC62828)
val VerdictNeutralText = Color(0xFFCE5A03)

@Composable
fun SignalColor?.toColor(): Color {
    return when (this) {
        SignalColor.GREEN -> VerdictBuyText
        SignalColor.YELLOW -> VerdictNeutralText
        SignalColor.RED -> VerdictSellText
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun IndicatorsScreen(
    data: MarketIndicators,
    scaffoldPadding: PaddingValues
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val scrollState = rememberScrollState()

    val paddingExtraLarge = dimensionResource(id = R.dimen.padding_extra_large)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    var showFrameworkSheet by remember { mutableStateOf(false) }
    var selectedIndicator by remember { mutableStateOf<DictionaryItem?>(null) }

    if (showFrameworkSheet) {
        FrameworkSheet(onDismiss = { showFrameworkSheet = false })
    }

    selectedIndicator?.let { dictionaryItem ->
        IndicatorDetailSheet(
            item = dictionaryItem,
            onDismiss = { selectedIndicator = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(
                top = statusBarHeight + paddingExtraLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingExtraLarge,
                start = paddingLarge,
                end = paddingLarge
            ),
        verticalArrangement = Arrangement.spacedBy(paddingExtraLarge)
    ) {
        // 1. Title Row with Info Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.indicators_screen_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = { showFrameworkSheet = true },
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
            ) {
                // 💡 REPLACED ICON WITH PAINTER RESOURCE
                Icon(
                    painter = painterResource(id = R.drawable.ic_info), // REPLACE with your info icon drawable
                    contentDescription = stringResource(id = R.string.indicators_glossary_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 2. The Header (Circular Gauge)
        data.summary?.let { summary ->
            IndicatorScoreHeader(summary = summary)
        }

        // 3. The 3 Phases (Trend, Health, Risk)
        data.trendPhase?.let { PhaseSection(details = it, onIndicatorClick = { item -> selectedIndicator = item }) }
        data.healthPhase?.let { PhaseSection(details = it, onIndicatorClick = { item -> selectedIndicator = item }) }
        data.riskPhase?.let { PhaseSection(details = it, onIndicatorClick = { item -> selectedIndicator = item }) }
    }
}

@Composable
fun IndicatorScoreHeader(summary: PhaseSummary) {
    val score = summary.score ?: 0
    var animationPlayed by remember { mutableFloatStateOf(0f) }

    val currentProgress by animateFloatAsState(
        targetValue = animationPlayed,
        animationSpec = tween(durationMillis = 1500),
        label = "score_animation"
    )

    LaunchedEffect(score) {
        animationPlayed = score / 100f
    }

    val ringColor = when {
        score >= 65 -> VerdictBuyText
        score >= 45 -> VerdictNeutralText
        else -> VerdictSellText
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_extra_large)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_extra_large)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(dimensionResource(id = R.dimen.gauge_size))
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    strokeWidth = dimensionResource(id = R.dimen.gauge_stroke_width),
                    strokeCap = StrokeCap.Round
                )

                CircularProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = ringColor,
                    strokeWidth = dimensionResource(id = R.dimen.gauge_stroke_width),
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(id = R.string.indicators_score_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))

            Text(
                text = summary.call?.name?.replace("_", " ") ?: stringResource(id = R.string.indicators_unknown_verdict),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ringColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = summary.marketRegime ?: stringResource(id = R.string.indicators_analyzing_regime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card))
            ) {
                Text(
                    text = summary.action ?: stringResource(id = R.string.indicators_no_action),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                )
            }
        }
    }
}

@Composable
fun PhaseSection(
    details: PhaseDetails,
    onIndicatorClick: (DictionaryItem?) -> Unit
) {
    val isGridLayout = true
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = details.phaseName ?: stringResource(id = R.string.indicators_unknown_phase),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = details.summary ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.padding_tiny),
                bottom = dimensionResource(id = R.dimen.padding_large)
            )
        )

        val items = details.indicators ?: emptyList()

        if (isGridLayout) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(paddingMedium)
                ) {
                    rowItems.forEach { item ->
                        IndicatorCard(
                            item = item,
                            modifier = Modifier.weight(1f),
                            onCardClick = { onIndicatorClick(IndicatorsDictionary.getDefinitionFor(item.name ?: "")) }
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(paddingMedium))
            }
        } else {
            items.forEach { item ->
                IndicatorCard(
                    item = item,
                    modifier = Modifier.fillMaxWidth(),
                    onCardClick = { onIndicatorClick(IndicatorsDictionary.getDefinitionFor(item.name ?: "")) }
                )
                Spacer(modifier = Modifier.height(paddingMedium))
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = paddingMedium)
        )
    }
}

@Composable
fun IndicatorCard(
    item: IndicatorItem,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
        modifier = modifier.clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Text(
                text = item.name ?: stringResource(id = R.string.indicators_unknown_indicator),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = item.value ?: stringResource(id = R.string.indicators_empty_value),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!item.changePercent.isNullOrBlank()) {
                    val isPositive = !item.changePercent.contains("-")
                    Text(
                        text = " ${item.changePercent}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPositive) VerdictBuyText else VerdictSellText,
                        modifier = Modifier.padding(
                            bottom = dimensionResource(id = R.dimen.padding_micro),
                            start = dimensionResource(id = R.dimen.padding_tiny)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            Surface(
                color = item.signalColor.toColor().copy(alpha = 0.15f),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
            ) {
                Text(
                    text = item.signal ?: stringResource(id = R.string.indicators_neutral_signal),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = item.signalColor.toColor(),
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.padding_small),
                        vertical = dimensionResource(id = R.dimen.padding_tiny)
                    )
                )
            }
        }
    }
}