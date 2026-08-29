package com.marketlabs.pulse.ui.components.bottomSheet

import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
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
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.glossary.GlossaryTerm
import com.marketlabs.pulse.core.glossary.MarketGlossaryProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketGlossaryBottomSheet(
    currentRegime: String? = null,
    currentSetup: String? = null,
    currentDirection: String? = null,
    currentCycleZone: String? = null,
    currentAction: String? = null,
    description: String? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glossary = MarketGlossaryProvider.get(LocalContext.current)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // 💡 Was colorScheme.background. Bottom sheets sit above the base background, at the
        // elevated step of the surface ramp, so they read as a distinct layer over the screen.
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_extra_large))
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.market_status_and_glossary),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
                )
            }

            item {
                if (description != null) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                    )
                }

                // 💡 NEW: Only show the "Current Verdict" section header and divider if at least one status is present
                if (currentRegime != null || currentSetup != null || currentDirection != null || currentCycleZone != null || currentAction != null) {
                    Text(
                        text = stringResource(id = R.string.current_verdict),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
                    )

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

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
        Text(
            text = definition,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GlossarySection(title: String, terms: List<GlossaryTerm>, currentVal: String?) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium), bottom = dimensionResource(id = R.dimen.padding_small))
    )

    terms.forEach { item ->
        val isCurrent = item.term == currentVal
        val bgColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent
        val titleColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = bgColor, shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium)))
                .padding(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.term,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = titleColor
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    Surface(
                        color = titleColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                    ) {
                        Text(
                            text = stringResource(id = R.string.status_current),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            // FIX: Change this to titleColor so it perfectly matches the pill background
                            color = titleColor,
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(id = R.dimen.padding_medium),
                                vertical = dimensionResource(id = R.dimen.padding_tiny)
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = item.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
    }
}