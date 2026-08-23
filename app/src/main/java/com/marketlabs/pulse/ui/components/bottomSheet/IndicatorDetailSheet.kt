package com.marketlabs.pulse.ui.components.bottomSheet

import android.util.Log
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.glossary.MetricGlossaryBand
import com.marketlabs.pulse.core.glossary.MetricGlossaryEntry
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.SignalColor
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Per-metric detail sheet -- shows the live reading (value + current signal) alongside the static
 * glossary content (what it is / how to read / bands) bundled in assets/metric_glossary.json,
 * keyed by [metric]'s own `id`. The "current" row in the Bands section is resolved by matching
 * [metric]'s live `signalText` against each band's `label` -- not by re-deriving a threshold here,
 * so the highlighted row always reflects whatever the backend's classifier actually returned.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicatorDetailSheet(
    metric: DomainUnifiedMetric,
    glossaryEntry: MetricGlossaryEntry?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val paddingExtraLarge = dimensionResource(id = R.dimen.padding_extra_large)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    val currentBandIndex = glossaryEntry?.bands?.indexOfFirst {
        it.label.equals(metric.signalText?.trim(), ignoreCase = true)
    }?.takeIf { it >= 0 }

    // 💡 A bundled band label that no longer matches the metric's live signal_text means the
    // backend's classifier moved out from under the bundle's content -- degrade to an
    // unhighlighted list (see the Bands section below) instead of crashing, but this should get
    // fixed on the content side, hence the dev-visible warning.
    LaunchedEffect(metric.id, metric.signalText) {
        if (glossaryEntry != null && glossaryEntry.bands.isNotEmpty() && currentBandIndex == null) {
            Log.w(
                "IndicatorDetailSheet",
                "No band in metric_glossary.json for '${metric.id}' matches live signal_text \"${metric.signalText}\" -- bundle may be stale."
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingExtraLarge)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 💡 `Modifier.padding` throws on a negative value (this crashed the sheet on
                    // open) -- `offset` is the correct tool for pulling the touch target flush
                    // with the sheet's edge, since it allows negative values.
                    IconButton(onClick = onDismiss, modifier = Modifier.offset(x = -paddingMedium)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = stringResource(id = R.string.nav_back_content_description),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = metric.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(paddingLarge))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = metric.valueDisplay ?: stringResource(id = R.string.not_available_short),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!metric.signalText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(paddingMedium))
                        SignalPill(
                            text = metric.signalText.uppercase(),
                            pillColor = metric.signalColor.pillColor,
                            contentColor = metric.signalColor.textColor
                        )
                    }
                }

                if (!metric.releaseDate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(paddingSmall))
                    Text(
                        text = stringResource(id = R.string.released_date, formatReleaseDate(metric.releaseDate)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(paddingExtraLarge))
            }

            if (glossaryEntry != null) {
                item {
                    Text(
                        text = stringResource(id = R.string.indicators_detail_what_it_is).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = glossaryEntry.whatItIs,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = paddingSmall, bottom = paddingLarge)
                    )

                    Text(
                        text = stringResource(id = R.string.indicators_detail_how_to_read).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = glossaryEntry.howToRead,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = paddingSmall, bottom = paddingLarge)
                    )
                }

                if (glossaryEntry.bands.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.indicators_detail_bands).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = paddingMedium)
                        )
                    }
                    items(glossaryEntry.bands) { band ->
                        val isCurrent = glossaryEntry.bands.indexOf(band) == currentBandIndex
                        BandRow(band = band, isCurrent = isCurrent)
                        Spacer(modifier = Modifier.height(paddingSmall))
                    }
                    item { Spacer(modifier = Modifier.height(paddingMedium)) }
                }

                if (!glossaryEntry.gotchas.isNullOrBlank()) {
                    item {
                        GotchaCallout(text = glossaryEntry.gotchas)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(paddingExtraLarge))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun BandRow(band: MetricGlossaryBand, isCurrent: Boolean) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)
    val accent = LocalPulseColors.current.accentPrimary

    // 💡 DATA style -- was a `Surface(surfaceVariant.copy(alpha = 0.4f))`, which blended straight
    // into the sheet's own `surfaceContainerHighest` background instead of reading as a card. This
    // is the same standardized, opaque "raw reading" card look every other data card in the app
    // uses, so it's actually visible against any surface. The current-band row adds its own accent
    // border on top of the card's own hairline one (via `Modifier.border`, since `PulseCard` locks
    // its own border color) rather than replacing it -- the two align on the same shape, so it
    // reads as one emphasized ring, not two stacked borders.
    val cardModifier = Modifier
        .fillMaxWidth()
        .let {
            if (isCurrent) {
                it.border(BorderStroke(1.dp, accent), RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)))
            } else {
                it
            }
        }

    PulseCard(style = PulseCardStyle.DATA, modifier = cardModifier) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = band.label,
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
                text = band.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = paddingSmall)
            )
        }
    }
}

@Composable
private fun GotchaCallout(text: String) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    // 💡 No card surface here on purpose -- wrapped in the same DATA-style PulseCard as BandRow
    // above, it read as just another band row instead of a distinct footnote. Plain icon + text on
    // the sheet's own background reads as a caveat, not a fourth band.
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            painter = painterResource(id = R.drawable.ic_info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
        )
        Spacer(modifier = Modifier.width(paddingMedium))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatReleaseDate(dateString: String): String {
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateString)
        SimpleDateFormat("MMM yyyy", Locale.US).format(parsed!!)
    } catch (e: Exception) {
        dateString
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewIndicatorDetailSheet() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        IndicatorDetailSheet(
            metric = DomainUnifiedMetric(
                id = "pe_ratio",
                name = "P/E Ratio (Trailing)",
                category = "VALUATION",
                subcategory = null,
                valueRaw = 25.79,
                valueDisplay = "25.79x",
                previousValueRaw = null,
                previousValueDisplay = null,
                changeRaw = 0.12,
                changeDisplay = "0.12%",
                signalText = "Expensive",
                signalColor = SignalColor.RED,
                releaseDate = "2026-07-01"
            ),
            glossaryEntry = MetricGlossaryEntry(
                whatItIs = "Trailing twelve-month price divided by trailing earnings for the S&P 500.",
                howToRead = "Higher readings mean investors are paying more per dollar of past earnings -- a stretched multiple leaves less room for disappointment.",
                bands = listOf(
                    MetricGlossaryBand(label = "Cheap", meaning = "16x or below -- earnings priced for pessimism."),
                    MetricGlossaryBand(label = "Fair Value", meaning = "16x-22x -- in line with typical historical norms."),
                    MetricGlossaryBand(label = "Expensive", meaning = "22x or above -- priced for a largely benign outcome.")
                ),
                gotchas = "Earnings can be revised after the fact, which quietly moves this ratio without any price change."
            ),
            onDismiss = {}
        )
    }
}
