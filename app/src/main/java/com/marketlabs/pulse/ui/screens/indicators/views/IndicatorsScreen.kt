package com.marketlabs.pulse.ui.screens.indicators.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainHorizon
import com.marketlabs.pulse.storage.model.indicators.DomainIndicatorPillar
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.ui.components.UniversalMetricCard
import com.marketlabs.pulse.ui.components.bottomSheet.FrameworkSheet
import com.marketlabs.pulse.ui.components.bottomSheet.IndicatorDetailSheet
import com.marketlabs.pulse.utils.enums.SubcategoryEnums
import com.marketlabs.pulse.utils.glossary.DictionaryItem
import com.marketlabs.pulse.utils.glossary.IndicatorsDictionary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
// 📱 MASTER STATE CONTROLLER
// ============================================================================
@Composable
fun IndicatorsScreen(
    data: MarketIndicators,
    scaffoldPadding: PaddingValues
) {
    var showHorizons by remember { mutableStateOf(false) }

    if (showHorizons) {
        HorizonBriefingsView(
            aiSynthesis = data.aiSynthesis,
            scaffoldPadding = scaffoldPadding,
            onBackClick = { showHorizons = false }
        )
    } else {
        IndicatorsMainFeed(
            data = data,
            scaffoldPadding = scaffoldPadding,
            onShowHorizons = { showHorizons = true }
        )
    }
}

// ============================================================================
// 📜 MAIN MINIMALIST FEED
// ============================================================================
@Composable
private fun IndicatorsMainFeed(
    data: MarketIndicators,
    scaffoldPadding: PaddingValues,
    onShowHorizons: () -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    var showFrameworkSheet by remember { mutableStateOf(false) }
    var selectedIndicator by remember { mutableStateOf<DictionaryItem?>(null) }

    if (showFrameworkSheet) {
        FrameworkSheet(onDismiss = { showFrameworkSheet = false })
    }

    selectedIndicator?.let { dictionaryItem ->
        IndicatorDetailSheet(item = dictionaryItem, onDismiss = { selectedIndicator = null })
    }

    val allPillars = listOfNotNull(
        data.tacticalMomentum?.let {
            PillarUIConfig(stringResource(id = R.string.pillar_tactical_momentum), it, data.aiSynthesis?.pillarGlances?.tactical)
        },
        data.systemicRisk?.let {
            PillarUIConfig(stringResource(id = R.string.pillar_systemic_risk), it, data.aiSynthesis?.pillarGlances?.systemicRisk)
        },
        data.valuation?.let {
            PillarUIConfig(stringResource(id = R.string.pillar_valuation), it, data.aiSynthesis?.pillarGlances?.valuation)
        },
        data.macroVitals?.let {
            // 💡 FLAGGED AS MACRO: This allows us to conditionally render the release dates
            PillarUIConfig(stringResource(id = R.string.pillar_macro_vitals), it, data.aiSynthesis?.pillarGlances?.macro, isMacro = true)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = statusBarHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingLarge, vertical = dimensionResource(id = R.dimen.padding_medium)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val textStyle = MaterialTheme.typography.headlineMedium
                val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

                Icon(
                    painter = painterResource(id = R.drawable.ic_engine_quant),
                    contentDescription = "Analysis Engine",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = stringResource(id = R.string.indicators_screen_title),
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = paddingLarge,
                end = paddingLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                AiExecutiveBriefingHero(
                    summaryText = data.aiSynthesis?.overarchingCondition,
                    whatChanged = data.aiSynthesis?.whatChanged,
                    timestamp = data.aiSynthesis?.timestamp
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            }

            item {
                HorizonNavigationCard(onClick = onShowHorizons)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            }

            items(allPillars) { config ->
                PillarSection(
                    config = config,
                    onIndicatorClick = { selectedIndicator = it }
                )
            }
        }
    }
}

// ============================================================================
// 🧱 SUB-COMPONENTS FOR MAIN FEED
// ============================================================================

@Composable
private fun AiExecutiveBriefingHero(summaryText: String?, whatChanged: String?, timestamp: Long?) {
    if (summaryText.isNullOrBlank() || timestamp == null) return

    var isExpanded by remember { mutableStateOf(false) }
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_engine_ai_sparkles),
                            contentDescription = "Analysis Engine",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(modifier = Modifier.width(paddingSmall))
                        Text(
                            text = stringResource(id = R.string.ai_executive_briefing),
                            style = textStyle,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.analyzed_at, format.format(date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(id = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = paddingMedium)
                )
            }

            Spacer(modifier = Modifier.height(paddingMedium))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            Spacer(modifier = Modifier.height(paddingMedium))

            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )

            if (!whatChanged.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(paddingMedium))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(paddingSmall)
                ) {
                    Text(
                        text = stringResource(id = R.string.shift_label),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = whatChanged,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizonNavigationCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show Horizon Briefings (Short, Medium, Long)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = "View Briefings",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// 💡 Added isMacro flag to safely toggle date views
data class PillarUIConfig(
    val title: String,
    val pillarData: DomainIndicatorPillar,
    val glanceText: String?,
    val isMacro: Boolean = false
)

@Composable
private fun PillarSection(
    config: PillarUIConfig,
    onIndicatorClick: (DictionaryItem?) -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = config.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = paddingMedium)
        )

        config.glanceText?.let { glance ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = paddingMedium)
            ) {
                Row(
                    modifier = Modifier.padding(paddingMedium),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_engine_ai_sparkles),
                        contentDescription = "AI Glance",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = glance,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                    )
                }
            }
        }

        val groupedMetrics = config.pillarData.metrics.groupBy { it.subcategory }

        groupedMetrics.forEach { (subcatEnum, metrics) ->

            if (subcatEnum != null) {
                val subcategoryText = when (subcatEnum) {
                    SubcategoryEnums.INFLATION -> stringResource(id = R.string.subcategory_inflation)
                    SubcategoryEnums.LABOR -> stringResource(id = R.string.subcategory_labor)
                    SubcategoryEnums.GROWTH -> stringResource(id = R.string.subcategory_growth)
                    SubcategoryEnums.POLICY -> stringResource(id = R.string.subcategory_policy)
                    else -> subcatEnum.label
                }

                // 💡 UPDATED: Typography changed to match native headers visually without uppercase labels
                Text(
                    text = subcategoryText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium), top = paddingLarge)
                )
            }

            metrics.chunked(2).forEach { rowMetrics ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(paddingMedium)
                ) {
                    rowMetrics.forEach { metric ->

                        val formattedChange = metric.changeDisplay?.let { changeStr ->
                            if (metric.changeRaw == 0.0 && !changeStr.startsWith("+") && !changeStr.startsWith("-")) {
                                "+$changeStr"
                            } else {
                                changeStr
                            }
                        }

                        UniversalMetricCard(
                            title = metric.name,
                            value = metric.valueDisplay,
                            changeString = formattedChange,
                            signalText = metric.signalText,
                            signalColor = metric.signalColor,
                            // 💡 HIDDEN: Evaluates macro flag to strip dates from pure technicals
                            dateString = if (config.isMacro) metric.releaseDate else null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { onIndicatorClick(IndicatorsDictionary.getDefinitionFor(metric.name)) }
                        )
                    }
                    if (rowMetrics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
                Spacer(modifier = Modifier.height(paddingMedium))
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = paddingMedium)
        )
    }
}

// ============================================================================
// 🔭 HORIZON BRIEFINGS SCREEN (EMBEDDED)
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizonBriefingsView(
    aiSynthesis: DomainAiSynthesis?,
    scaffoldPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        stringResource(id = R.string.tab_short_term),
        stringResource(id = R.string.tab_medium_term),
        stringResource(id = R.string.tab_long_term)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = statusBarHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Horizon Briefings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (aiSynthesis == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Briefing data unavailable.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return
        }

        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                    width = dimensionResource(id = R.dimen.tab_indicator_width),
                    height = dimensionResource(id = R.dimen.tab_indicator_height),
                    shape = RoundedCornerShape(
                        topStart = dimensionResource(id = R.dimen.tab_indicator_corner),
                        topEnd = dimensionResource(id = R.dimen.tab_indicator_corner)
                    )
                )
            },
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }

        val currentHorizon = when (selectedTabIndex) {
            0 -> aiSynthesis.shortTerm
            1 -> aiSynthesis.mediumTerm
            else -> aiSynthesis.longTerm
        }

        AnimatedContent(
            targetState = currentHorizon,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "horizon_animation",
            modifier = Modifier.fillMaxSize()
        ) { horizon ->
            if (horizon != null) {
                HorizonDetailPanel(horizon, scaffoldPadding)
            }
        }
    }
}

@Composable
private fun HorizonDetailPanel(horizon: DomainHorizon, scaffoldPadding: PaddingValues) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    LazyColumn(
        contentPadding = PaddingValues(
            start = paddingLarge,
            end = paddingLarge,
            top = paddingLarge,
            bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${stringResource(id = R.string.outlook_label)}: ${horizon.riskLevel}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = when (horizon.riskLevel.uppercase()) {
                        "HIGH" -> MaterialTheme.colorScheme.error
                        "MODERATE" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(id = R.string.driver_label)}: ${horizon.keyDriver}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            Text(
                text = horizon.briefing,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(paddingLarge)
            ) {
                Text(
                    text = stringResource(id = R.string.playbook_title).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = horizon.whatToDo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                )
            }
        }
    }
}