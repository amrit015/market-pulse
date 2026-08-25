package com.marketlabs.pulse.ui.screens.indicators.detail

// Content preserved from the deleted IndicatorDetailSheet.kt (header, value+pill, release date,
// glossary what-it-is/how-to-read/bands/gotchas) -- only the ModalBottomSheet wrapper is gone,
// replaced by a plain scrollable Column matching AssetDetailScreen's shape. IndicatorHistoryChart
// is the one genuinely new section, inserted between the release date and the glossary.

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.marketlabs.pulse.storage.model.indicators.MetricHistoryPoint
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.charts.IndicatorHistoryChart
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.verticalScrollbar
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Stateless content for the pushed metric-detail page -- moved out of the old
 * `IndicatorDetailSheet` body verbatim (value + signal pill, release date, glossary sections),
 * only the `ModalBottomSheet` wrapper is gone. The "current" row in the Bands section is still
 * resolved by matching [metric]'s live `signalText` against each band's `label`, not by
 * re-deriving a threshold here, so the highlighted row always reflects whatever the backend's
 * classifier actually returned.
 */
@Composable
fun MetricDetailScreen(
    metric: DomainUnifiedMetric,
    glossaryEntry: MetricGlossaryEntry?,
    historyPoints: List<MetricHistoryPoint>,
    isHistoryLoading: Boolean,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val paddingExtraLarge = dimensionResource(id = R.dimen.padding_extra_large)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)
    val scrollState = rememberScrollState()

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
                "MetricDetailScreen",
                "No band in metric_glossary.json for '${metric.id}' matches live signal_text \"${metric.signalText}\" -- bundle may be stale."
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScrollbar(state = scrollState, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            .verticalScroll(scrollState)
            .padding(
                start = paddingExtraLarge,
                end = paddingExtraLarge,
                top = scaffoldPadding.calculateTopPadding() + paddingLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge
            )
    ) {
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

        // History chart -- the one section that's genuinely new here, not preserved from
        // IndicatorDetailSheet. No range picker (see IndicatorHistoryChart's own doc comment).
        Spacer(modifier = Modifier.height(paddingExtraLarge))
        IndicatorHistoryChart(
            points = historyPoints,
            isLoading = isHistoryLoading,
            modifier = Modifier.fillMaxWidth()
        )

        if (glossaryEntry != null) {
            Spacer(modifier = Modifier.height(paddingExtraLarge))

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

            if (glossaryEntry.bands.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.indicators_detail_bands).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = paddingMedium)
                )
                glossaryEntry.bands.forEachIndexed { index, band ->
                    BandRow(band = band, isCurrent = index == currentBandIndex)
                    Spacer(modifier = Modifier.height(paddingSmall))
                }
                Spacer(modifier = Modifier.height(paddingMedium))
            }

            if (!glossaryEntry.gotchas.isNullOrBlank()) {
                GotchaCallout(text = glossaryEntry.gotchas)
            }
        }

        Spacer(modifier = Modifier.height(paddingExtraLarge))
    }
}

@Composable
private fun BandRow(band: MetricGlossaryBand, isCurrent: Boolean) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)
    val accent = LocalPulseColors.current.accentPrimary

    // 💡 DATA style, same standardized "raw reading" card look every other data card in the app
    // uses -- the current-band row adds its own accent border on top of the card's own hairline
    // one (via `Modifier.border`, since `PulseCard` locks its own border color) rather than
    // replacing it, so it reads as one emphasized ring, not two stacked borders.
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
    // above, it read as just another band row instead of a distinct footnote. Plain icon + text
    // reads as a caveat, not a fourth band.
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

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val previewMetric = DomainUnifiedMetric(
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
)

private val previewGlossaryEntry = MetricGlossaryEntry(
    whatItIs = "Trailing twelve-month price divided by trailing earnings for the S&P 500.",
    howToRead = "Higher readings mean investors are paying more per dollar of past earnings -- a stretched multiple leaves less room for disappointment.",
    bands = listOf(
        MetricGlossaryBand(label = "Cheap", meaning = "16x or below -- earnings priced for pessimism."),
        MetricGlossaryBand(label = "Fair Value", meaning = "16x-22x -- in line with typical historical norms."),
        MetricGlossaryBand(label = "Expensive", meaning = "22x or above -- priced for a largely benign outcome.")
    ),
    gotchas = "Earnings can be revised after the fact, which quietly moves this ratio without any price change."
)

private val previewHistoryPoints = listOf(
    MetricHistoryPoint("2026-08-21", 24.10, "24.10x", SignalColor.YELLOW),
    MetricHistoryPoint("2026-08-22", 24.85, "24.85x", SignalColor.YELLOW),
    MetricHistoryPoint("2026-08-23", 25.30, "25.30x", SignalColor.RED),
    MetricHistoryPoint("2026-08-24", 25.60, "25.60x", SignalColor.RED),
    MetricHistoryPoint("2026-08-25", 25.79, "25.79x", SignalColor.RED)
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewMetricDetailScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        MetricDetailScreen(
            metric = previewMetric,
            glossaryEntry = previewGlossaryEntry,
            historyPoints = previewHistoryPoints,
            isHistoryLoading = false,
            scaffoldPadding = PaddingValues()
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewMetricDetailScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        MetricDetailScreen(
            metric = previewMetric,
            glossaryEntry = previewGlossaryEntry,
            historyPoints = previewHistoryPoints,
            isHistoryLoading = false,
            scaffoldPadding = PaddingValues()
        )
    }
}
