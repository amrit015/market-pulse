package com.marketlabs.pulse.ui.screens.indicators.views

/*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.IndicatorItem
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.model.indicators.PhaseDetails
import com.marketlabs.pulse.storage.model.indicators.PhaseSummary
import com.marketlabs.pulse.storage.model.indicators.enums.SignalColor
import com.marketlabs.pulse.ui.components.widgets.ScoreGauge
import com.marketlabs.pulse.ui.screens.indicators.DictionaryItem
import com.marketlabs.pulse.ui.screens.indicators.FrameworkSheet
import com.marketlabs.pulse.ui.screens.indicators.IndicatorDetailSheet
import com.marketlabs.pulse.ui.screens.indicators.IndicatorsDictionary
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SignalColor?.toColor(): Color {
    return when (this) {
        SignalColor.GREEN -> PulseStatusColors.BullishText
        SignalColor.YELLOW -> PulseStatusColors.NeutralText
        SignalColor.RED -> PulseStatusColors.BearishText
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun SignalColor?.toBgColor(): Color {
    return when (this) {
        SignalColor.GREEN -> PulseStatusColors.BullishBg
        SignalColor.YELLOW -> PulseStatusColors.NeutralBg
        SignalColor.RED -> PulseStatusColors.BearishBg
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
fun IndicatorsScreen(
    data: MarketIndicators,
    scaffoldPadding: PaddingValues
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val scrollState = rememberScrollState()

    // 💡 FIXED: Unified margin spacing identical to RiskRadarScreen
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingXLarge = dimensionResource(id = R.dimen.padding_xlarge)

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
                top = statusBarHeight + paddingLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
                start = paddingLarge,
                end = paddingLarge
            ),
        verticalArrangement = Arrangement.spacedBy(paddingXLarge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.indicators_screen_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                data.lastUpdated.let { timestamp ->
                    val date = Date(timestamp)
                    val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

                    Text(
                        text = stringResource(id = R.string.analyzed_at, format.format(date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
                    )
                }
            }

            IconButton(
                onClick = { showFrameworkSheet = true },
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = stringResource(id = R.string.indicators_glossary_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        data.summary?.let { summary ->
            IndicatorScoreHeader(summary = summary)
        }

        data.trendPhase?.let {
            PhaseSection(
                details = it,
                onIndicatorClick = { item -> selectedIndicator = item })
        }
        data.healthPhase?.let {
            PhaseSection(
                details = it,
                onIndicatorClick = { item -> selectedIndicator = item })
        }
        data.riskPhase?.let {
            PhaseSection(
                details = it,
                onIndicatorClick = { item -> selectedIndicator = item })
        }
    }
}

@Composable
fun IndicatorScoreHeader(summary: PhaseSummary) {
    val score = summary.score ?: 0
    val previousScore = summary.previousScore ?: score

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

    var expanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = headerBgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_extra_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Future Main Header Click */ }
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
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
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
                    statusText = summary.marketRegime,
                    ringColor = ringColor
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.5f
                        )
                    ),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { /* TODO: Future Child Card Click */ }
                ) {
                    // 💡 FIXED: Increased internal padding for breathing room
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                        Text(
                            text = summary.call?.name?.replace("_", " ")
                                ?: stringResource(id = R.string.indicators_unknown_verdict),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ringColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // 💡 Only make it clickable if it overflows or is already expanded
                    .then(
                        if (hasOverflow || expanded) {
                            Modifier.clickable { expanded = !expanded }
                        } else Modifier
                    )
                    .padding(top = dimensionResource(id = R.dimen.padding_medium)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.action ?: stringResource(id = R.string.indicators_no_action),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).animateContentSize(),
                    onTextLayout = { textLayoutResult ->
                        // 💡 Check if the text was truncated when it's collapsed
                        if (!expanded) {
                            hasOverflow = textLayoutResult.hasVisualOverflow
                        }
                    }
                )

                // 💡 Only show the arrow if it overflows the 2 lines, or if it's currently open
                if (hasOverflow || expanded) {
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    Icon(
                        painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                }
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
            color = MaterialTheme.colorScheme.primary
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
                            onCardClick = {
                                onIndicatorClick(
                                    IndicatorsDictionary.getDefinitionFor(
                                        item.name ?: ""
                                    )
                                )
                            }
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
                    onCardClick = {
                        onIndicatorClick(
                            IndicatorsDictionary.getDefinitionFor(
                                item.name ?: ""
                            )
                        )
                    }
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
    val baseColor = item.signalColor.toColor()
    val bgColor = item.signalColor.toBgColor()

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
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
                    val changeColor = when {
                        item.changePercent.contains("+") -> PulseStatusColors.BullishText
                        item.changePercent.contains("-") -> PulseStatusColors.BearishText
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Text(
                        text = item.changePercent,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = changeColor,
                        modifier = Modifier.padding(
                            bottom = dimensionResource(id = R.dimen.padding_micro),
                            start = dimensionResource(id = R.dimen.padding_small)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            Surface(
                color = baseColor.copy(alpha = 0.0f),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
            ) {
                Text(
                    text = item.signal ?: stringResource(id = R.string.sentiment_neutral),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = baseColor,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.padding_small),
                        vertical = dimensionResource(id = R.dimen.padding_tiny)
                    )
                )
            }
        }
    }
}
 */