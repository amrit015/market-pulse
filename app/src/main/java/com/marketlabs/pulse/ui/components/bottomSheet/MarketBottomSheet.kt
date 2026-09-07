package com.marketlabs.pulse.ui.components.bottomSheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.glossary.GlossaryTerm
import com.marketlabs.pulse.core.glossary.MarketGlossaryProvider
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketGlossaryBottomSheet(
    currentRegime: String? = null,
    currentSetup: String? = null,
    currentDirection: String? = null,
    currentCycleZone: String? = null,
    currentAction: String? = null,
    currentStockSetup: String? = null,
    description: String? = null,
    title: String = stringResource(id = R.string.market_status_and_glossary),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glossary = MarketGlossaryProvider.get(LocalContext.current)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // 💡 2026-09-07, second pass: colorScheme.surface -- neither of the two tokens tried before
        // this works, because this app's simplified surface ramp only has 3 genuinely distinct
        // values (see PulseTokens.Color.kt's `SurfaceRamp`: `background`, `surface`,
        // `surfaceElevated`), and this sheet needs to sit between two of them at once:
        //   - `surfaceContainerHighest` (first pass) resolves to the literal same value as
        //     `surfaceVariant` (`surfaceElevated`) -- the exact fill `PulseCard`'s borderless DATA
        //     style uses, so every DATA card nested in this sheet (the "Current Verdict" card, every
        //     term card) had zero contrast against the sheet itself.
        //   - `colorScheme.background` (second pass) fixed that, but is the literal same value the
        //     screen behind the sheet already uses, so the sheet lost its own visual boundary --
        //     hard to tell where it starts/ends against the page.
        // `colorScheme.surface` (`SurfaceRamp.surface`) is the one remaining token, genuinely
        // distinct from both `background` and `surfaceElevated` in this ramp (confirmed via
        // PulseTokens.Color.kt's literal hex values, dark mode: 0xFF17181D sits between background's
        // 0xFF0D0E12 and surfaceElevated's 0xFF1F2026) -- the sheet now reads as its own layer
        // against the screen AND lets a DATA card read against the sheet, at the same time. Was
        // deliberately avoided as a `TopAppBar` containerColor elsewhere (see SettingsScreen.kt's own
        // comment) for reading "noticeably different" from `background` -- that's a bug for a top bar
        // meant to blend seamlessly with the page below it, but it's exactly the property this sheet
        // needs.
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_extra_large))
        ) {
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
                )
            }

            item {
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                    )
                }

                // 💡 2026-09-06: was a headerless block of plain text rows -- now one DATA card
                // (header + divider, same convention every other data card on Summary/Positioning
                // uses) so "current verdict" reads as a card among cards, not a stray text block
                // sitting above the term-list cards below it.
                if (currentRegime != null || currentSetup != null || currentDirection != null || currentCycleZone != null || currentAction != null || currentStockSetup != null) {
                    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.current_verdict),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                thickness = dimensionResource(id = R.dimen.border_thin)
                            )
                            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                                if (currentRegime != null) {
                                    CurrentStatusRow(stringResource(id = R.string.market_regime_label), currentRegime, glossary.regimes)
                                }
                                if (currentSetup != null) {
                                    CurrentStatusRow(stringResource(id = R.string.technical_setup_label), currentSetup, glossary.setups)
                                }
                                if (currentDirection != null) {
                                    CurrentStatusRow(stringResource(id = R.string.direction_label), currentDirection, glossary.directions)
                                }
                                if (currentCycleZone != null) {
                                    CurrentStatusRow(stringResource(id = R.string.cycle_zone_label), currentCycleZone, glossary.cycleZones)
                                }
                                if (currentAction != null) {
                                    CurrentStatusRow(stringResource(id = R.string.action_signal_label), currentAction, glossary.actions)
                                }
                                if (currentStockSetup != null) {
                                    CurrentStatusRow(stringResource(id = R.string.stock_setup_label), currentStockSetup, glossary.stockSetups)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                }
            }

            // 💡 NEW: Safely and individually render each Glossary Section only if its specific data is passed
            if (currentAction != null) {
                item { GlossarySection(stringResource(id = R.string.action_glossary_title), glossary.actions, currentAction) }
            }

            if (currentRegime != null) {
                item { GlossarySection(stringResource(id = R.string.regime_glossary_title), glossary.regimes, currentRegime) }
            }

            if (currentSetup != null) {
                item { GlossarySection(stringResource(id = R.string.setup_glossary_title), glossary.setups, currentSetup) }
            }

            if (currentDirection != null) {
                item { GlossarySection(stringResource(id = R.string.direction_glossary_title), glossary.directions, currentDirection) }
            }

            if (currentCycleZone != null) {
                item { GlossarySection(stringResource(id = R.string.cycle_zone_glossary_title), glossary.cycleZones, currentCycleZone) }
            }

            if (currentStockSetup != null) {
                item { GlossarySection(stringResource(id = R.string.stock_setup_glossary_title), glossary.stockSetups, currentStockSetup) }
            }

            item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
    }
}

@Composable
private fun CurrentStatusRow(label: String, value: String, dictionary: List<GlossaryTerm>) {
    val definition = dictionary.find { it.term == value }?.definition ?: stringResource(id = R.string.not_available_short)

    Column(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label ",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
        Text(
            text = definition,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 2026-09-07: rebuilt to match Indicators' own `MetricDetailScreen.kt` `BandRow`/"BANDS" section
 * exactly (byte-for-byte on spacing and card shape, not just the same general idea) -- was
 * modeled on `GlossaryDetailScreen.kt`'s `GlossaryBandRow` instead, which turns out to differ from
 * `BandRow` in both spacing (label-medium/padding_medium section title vs. that one's label-small/
 * split padding) and card content (this now uses a `SignalPill` "CURRENT" badge + always-onBackground
 * term label, not a plain accent-colored "CURRENT" text label with the term itself re-colored).
 * `BandRow` is Indicators' canonical version of this pattern; the two should read identically.
 */
@Composable
private fun GlossarySection(title: String, terms: List<GlossaryTerm>, currentVal: String?) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
    )

    val accent = LocalPulseColors.current.accentPrimary
    terms.forEach { item ->
        val isCurrent = item.term == currentVal
        GlossaryTermCard(term = item, isCurrent = isCurrent, accent = accent)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
    }
}

@Composable
private fun GlossaryTermCard(term: GlossaryTerm, isCurrent: Boolean, accent: Color) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .let {
            if (isCurrent) {
                it.border(
                    BorderStroke(dimensionResource(id = R.dimen.border_thin), accent),
                    RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large))
                )
            } else {
                it
            }
        }

    PulseCard(style = PulseCardStyle.DATA, modifier = cardModifier) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = term.term,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (isCurrent) {
                    SignalPill(
                        text = stringResource(id = R.string.status_current),
                        pillColor = accent.copy(alpha = 0.16f),
                        contentColor = accent
                    )
                }
            }
            Text(
                text = term.definition,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewMarketGlossaryBottomSheet() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        MarketGlossaryBottomSheet(
            currentRegime = "RISK ON",
            currentSetup = "BREAKOUT",
            currentDirection = "BULLISH",
            currentCycleZone = "EXPANSION",
            currentAction = "ADD",
            description = "The market's current read across regime, setup, direction, and cycle.",
            onDismiss = {}
        )
    }
}