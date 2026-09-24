package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.PulseTabRow
import com.marketlabs.pulse.ui.components.tutorials.CenteredCardCarousel
import com.marketlabs.pulse.ui.components.tutorials.Mechanism
import com.marketlabs.pulse.ui.components.tutorials.mechanismContentPages
import com.marketlabs.pulse.ui.screens.stocks.detail.ViewMoreRow
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Tutorials -> Market mechanisms -> one mechanism: its 6 content cards in a centered carousel (at
 * most 70% of the screen tall, all cards one height), with a "See all indicators" link underneath
 * (same look as the guide's "Show More"): from the Tutorials hub it lists that one mechanism's
 * indicators, from a screen's "Show More" it lists every indicator of the whole screen. When reached from a screen's "Show More" that spans several
 * mechanisms (Indicators' four pillars, Insights' Posture/Positioning), [group] lists them all and a
 * tab row above the carousel switches between their decks; otherwise it's empty and there are no tabs.
 */
@Composable
fun MechanismDeckScreen(
    mechanism: Mechanism,
    group: List<Mechanism>,
    onNavigateUp: () -> Unit,
    onSeeIndicators: (Mechanism, List<Mechanism>) -> Unit
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
            CenteredCardCarousel(pages = mechanismContentPages(current), modifier = Modifier.weight(1f), enlargedText = true)
        }
        ViewMoreRow(
            text = stringResource(id = R.string.deck_link_see_all_indicators),
            onClick = { onSeeIndicators(current, if (hasTabs) group else emptyList()) },
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.padding_extra_large),
                top = dimensionResource(id = R.dimen.padding_large),
                bottom = dimensionResource(id = R.dimen.padding_extra_large)
            )
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewMechanismDeckScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        MechanismDeckScreen(mechanism = Mechanism.SYSTEMIC_RISK, group = emptyList(), onNavigateUp = {}, onSeeIndicators = { _, _ -> })
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
            onSeeIndicators = { _, _ -> }
        )
    }
}
