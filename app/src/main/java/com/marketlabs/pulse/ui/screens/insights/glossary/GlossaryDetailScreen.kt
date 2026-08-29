package com.marketlabs.pulse.ui.screens.insights.glossary

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
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.verticalScrollbar

/**
 * Stateless content for the pushed glossary-detail page (2026-08-27 convergence pass -- one whole
 * Positioning/Posture CARD is the tap target, not its individual values, so this screen shows
 * everything the card covers in one place: the card's own "what is this" description, then each
 * underlying value's what-it-is/how-to-read/gotcha, then one merged bands list with the card's
 * live status highlighted -- same "current" concept `MetricDetailScreen` uses for Indicators,
 * resolved once across every section here instead of one entry's own bands.
 */
@Composable
fun GlossaryDetailScreen(
    title: String,
    description: String?,
    sections: List<GlossarySection>,
    mergedBands: List<MetricGlossaryBand>,
    currentBandIndex: Int?,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val paddingExtraLarge = dimensionResource(id = R.dimen.padding_extra_large)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)
    val scrollState = rememberScrollState()
    val showSectionLabels = sections.size > 1

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
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(paddingExtraLarge))
        }

        sections.forEachIndexed { index, section ->
            if (showSectionLabels) {
                Text(
                    text = section.label.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = paddingSmall)
                )
            } else {
                Text(
                    text = stringResource(id = R.string.indicators_detail_what_it_is).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = section.whatItIs,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = paddingSmall, bottom = paddingMedium)
            )

            if (!showSectionLabels) {
                Text(
                    text = stringResource(id = R.string.indicators_detail_how_to_read).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = section.howToRead,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = paddingSmall, bottom = paddingMedium)
                )
            } else {
                Text(
                    text = section.howToRead,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = paddingMedium)
                )
            }

            if (!section.gotchas.isNullOrBlank()) {
                GlossaryGotchaCallout(text = section.gotchas)
                Spacer(modifier = Modifier.height(paddingMedium))
            }

            if (index != sections.lastIndex) {
                Spacer(modifier = Modifier.height(paddingMedium))
            }
        }

        if (mergedBands.isNotEmpty()) {
            Spacer(modifier = Modifier.height(paddingMedium))
            Text(
                text = stringResource(id = R.string.indicators_detail_bands).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = paddingMedium)
            )
            mergedBands.forEachIndexed { index, band ->
                GlossaryBandRow(band = band, isCurrent = index == currentBandIndex)
                Spacer(modifier = Modifier.height(paddingSmall))
            }
        }

        Spacer(modifier = Modifier.height(paddingExtraLarge))
    }
}

@Composable
private fun GlossaryBandRow(band: MetricGlossaryBand, isCurrent: Boolean) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)
    val accent = LocalPulseColors.current.accentPrimary

    // 💡 Same accent-border-on-top-of-the-card's-own-hairline treatment as Indicators'
    // MetricDetailScreen -- one emphasized ring, not two stacked borders (PulseCard locks its own
    // border color, so the "current" highlight has to be a second border layered via `.border()`
    // rather than overriding PulseCard's).
    val cardModifier = Modifier
        .fillMaxWidth()
        .let { if (isCurrent) it.border(BorderStroke(1.dp, accent), RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large))) else it }

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
                    color = if (isCurrent) accent else MaterialTheme.colorScheme.onBackground
                )
                if (isCurrent) {
                    Text(
                        text = stringResource(id = R.string.status_current),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accent
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
private fun GlossaryGotchaCallout(text: String) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

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

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val previewSections = listOf(
    GlossarySection(
        label = "Days to Cover",
        whatItIs = "If every short seller tried to buy back their shares using only a typical day's trading volume, this is roughly how many days it would take.",
        howToRead = "Higher days-to-cover means less room for short sellers to exit quickly.",
        gotchas = "This measures potential, not likelihood."
    ),
    GlossarySection(
        label = "Month-over-Month Change",
        whatItIs = "How much short interest changed since the prior FINRA settlement date.",
        howToRead = "The trend is often more informative than the absolute level.",
        gotchas = "Settlement data is roughly 8 business days old by the time it's published."
    )
)

private val previewBands = listOf(
    MetricGlossaryBand(label = "ELEVATED (CROWDED SHORT)", meaning = "5x days-to-cover or more."),
    MetricGlossaryBand(label = "NEUTRAL", meaning = "Below 5x, little meaningful mom-change."),
    MetricGlossaryBand(label = "COVERING (SHORTS EXITING)", meaning = "-15% or lower.")
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewGlossaryDetailScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        GlossaryDetailScreen(
            title = "SPDR S&P 500 (SPY)",
            description = "FINRA-reported short interest in SPY/QQQ/IWM/DIA/RSP/MAGS as index/style-ETF proxies.",
            sections = previewSections,
            mergedBands = previewBands,
            currentBandIndex = 1,
            scaffoldPadding = PaddingValues()
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewGlossaryDetailScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        GlossaryDetailScreen(
            title = "SPDR S&P 500 (SPY)",
            description = "FINRA-reported short interest in SPY/QQQ/IWM/DIA/RSP/MAGS as index/style-ETF proxies.",
            sections = previewSections,
            mergedBands = previewBands,
            currentBandIndex = 1,
            scaffoldPadding = PaddingValues()
        )
    }
}
