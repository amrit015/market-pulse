package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.PulseTabRow
import com.marketlabs.pulse.ui.components.tutorials.CenteredCardCarousel
import com.marketlabs.pulse.ui.components.tutorials.Mechanism
import com.marketlabs.pulse.ui.components.tutorials.mechanismContentPages
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Tutorials -> Market mechanisms -> one mechanism: its 6 content cards in a centered carousel (at
 * most 70% of the screen tall, all cards one height). "See all indicators" lives inside the "What
 * this is" card itself (see `mechanismContentPages`) and always lists just [current]'s own
 * indicators -- never a combined multi-mechanism list, regardless of [group]. When reached from a
 * screen's "Show More" that spans several mechanisms (Indicators' four pillars, Insights' Posture/
 * Positioning), [group] still lets a tab row above the carousel switch between their *content* decks
 * (What this is/Signals/etc.) -- that's a separate concern from "See all indicators," which each
 * mechanism keeps to itself even while grouped this way; [group] empty means no tabs.
 */
@Composable
fun MechanismDeckScreen(
    mechanism: Mechanism,
    group: List<Mechanism>,
    onNavigateUp: () -> Unit,
    onSeeIndicators: (Mechanism) -> Unit,
    onNavigateToRoute: (String) -> Unit = {}
) {
    val hasTabs = group.size > 1
    var selectedIndex by rememberSaveable { mutableIntStateOf(group.indexOf(mechanism).coerceAtLeast(0)) }
    val current = if (hasTabs) group[selectedIndex.coerceIn(group.indices)] else mechanism

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
    ) {
        PulseBackTitleRow(title = stringResource(id = current.titleRes), onNavigateUp = onNavigateUp)
        if (hasTabs) {
            PulseTabRow(
                tabs = group.map { stringResource(id = it.titleRes) },
                selectedTabIndex = selectedIndex,
                onTabSelected = { selectedIndex = it }
            )
        }
        key(current) {
            CenteredCardCarousel(
                pages = mechanismContentPages(current, onSeeIndicators = { onSeeIndicators(current) }),
                modifier = Modifier.weight(1f),
                enlargedText = true,
                onNavigateToRoute = onNavigateToRoute
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewMechanismDeckScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        MechanismDeckScreen(mechanism = Mechanism.SYSTEMIC_RISK, group = emptyList(), onNavigateUp = {}, onSeeIndicators = {})
    }
}

@Preview(name = "Dark grouped", showBackground = true)
@Composable
private fun PreviewMechanismDeckScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        MechanismDeckScreen(
            mechanism = Mechanism.POSTURE,
            group = listOf(Mechanism.POSTURE, Mechanism.POSITIONING),
            onNavigateUp = {},
            onSeeIndicators = {}
        )
    }
}
