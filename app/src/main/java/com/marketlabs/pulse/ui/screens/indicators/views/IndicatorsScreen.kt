package com.marketlabs.pulse.ui.screens.indicators.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.model.indicators.enums.SignalColor
import com.marketlabs.pulse.ui.screens.indicators.*
import com.marketlabs.pulse.ui.screens.indicators.views.tabs.MacroVitalsTab
import com.marketlabs.pulse.ui.screens.indicators.views.tabs.MarketActionTab
import com.marketlabs.pulse.ui.screens.indicators.views.tabs.MarketPhaseTab
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 🎨 SHARED UI EXTENSIONS & COMPONENTS
// ==========================================
@Composable
fun SignalColor?.toColor(): Color {
    return when (this) {
        SignalColor.GREEN -> PulseStatusColors.BullishText
        SignalColor.YELLOW -> PulseStatusColors.NeutralText
        SignalColor.RED -> PulseStatusColors.BearishText
        SignalColor.UNKNOWN, null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun SignalColor?.toBgColor(): Color {
    return when (this) {
        SignalColor.GREEN -> PulseStatusColors.BullishBg
        SignalColor.YELLOW -> PulseStatusColors.NeutralBg
        SignalColor.RED -> PulseStatusColors.BearishBg
        SignalColor.UNKNOWN, null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
}

@Composable
fun ContextHeaderCard(guide: PillarGuide) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)).animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guide.pillarName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = guide.timeframe,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
                    )
                }
                Icon(
                    painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = "Toggle Description",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = guide.purpose,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = guide.howToUse,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AnalyzedAtText(timestamp: Long, modifier: Modifier = Modifier) {
    if (timestamp <= 0L) return
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    Text(
        text = stringResource(id = R.string.analyzed_at, format.format(date)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
    )
}

// ==========================================
// 📱 MASTER SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicatorsScreen(
    data: MarketIndicators,
    scaffoldPadding: PaddingValues
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
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

    val tabs = listOf(
        stringResource(id = R.string.tab_action),
        stringResource(id = R.string.tab_phase),
        stringResource(id = R.string.tab_vitals)
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = statusBarHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingLarge, vertical = paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.indicators_screen_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { showFrameworkSheet = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = stringResource(id = R.string.indicators_glossary_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {
                // 💡 FIX: Material 3.1.2+ uses TabIndicatorScope implicitly here
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
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
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = scaffoldPadding.calculateBottomPadding())
        ) { page ->
            when (page) {
                0 -> MarketActionTab(
                    data.marketAction,
                    onIndicatorClick = { selectedIndicator = it })
                1 -> MarketPhaseTab(data.marketPhase, onIndicatorClick = { selectedIndicator = it })
                2 -> MacroVitalsTab(data.macroVitals, onIndicatorClick = { selectedIndicator = it })
            }
        }
    }
}