package com.marketlabs.pulse.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Shared hero card for the small Gemini-authored narrative layer both the Posture and Positioning
 * sections add on top of their deterministic gauges (2026-08-26 revamp) -- a "SYNTHESIS" eyebrow +
 * headline + expandable detail, matching Indicators' `AiExecutiveBriefingHero` eyebrow/expand
 * pattern (icon + label row, `animateContentSize()`, tap-to-expand) rather than inventing a new
 * interaction for what is functionally the same kind of card. Takes primitive headline/detail/
 * isUnavailable rather than either domain's own `DomainPostureSynthesis`/`DomainPositioningSynthesis`
 * type directly, since those are two separate (if identically-shaped) classes -- see those models'
 * own doc comments for why this app keeps them duplicated per domain instead of sharing one type.
 *
 * `isUnavailable` renders the backend's `state: "unavailable"` first-run edge case (no headline/
 * detail yet, nothing to fall back to) as real empty-state copy -- not the "Preview unavailable
 * state" toggle link seen in the design mockups, which is a design-tool artifact for switching
 * between mockup variants, not a feature meant to ship.
 *
 * Headline/detail text styles are pinned to `titleMedium.Bold`/`bodyMedium` (`onSurface`, 1.2x
 * line height, ellipsis when collapsed) to match `AiExecutiveBriefingHero` (Indicators) and the
 * Market Signal card (Summary) exactly -- all three are the same kind of card (the one AI-authored
 * flash statement at the top of a domain's executive view) and are meant to read at the same
 * visual weight across the app, not each pick their own size.
 */
@Composable
fun SynthesisHeroCard(
    headline: String?,
    detail: String?,
    isUnavailable: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val pulseColors = LocalPulseColors.current
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = if (detail.isNullOrBlank()) null else { { isExpanded = !isExpanded } }
    ) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_ai_sparkle_filled),
                        contentDescription = null,
                        tint = pulseColors.accentPrimary,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.width(paddingSmall))
                    Text(
                        text = stringResource(id = R.string.insights_synthesis_label),
                        style = textStyle,
                        color = pulseColors.accentPrimary
                    )
                }
                if (!detail.isNullOrBlank()) {
                    Icon(
                        painter = painterResource(id = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = paddingMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(paddingMedium))

            if (isUnavailable || headline.isNullOrBlank()) {
                Text(
                    text = stringResource(id = R.string.insights_synthesis_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!detail.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(paddingLarge))
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                    )
                }
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewSynthesisHeroCardLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SynthesisHeroCard(
            headline = "Institutions accumulating as liquidity expands",
            detail = "Dark pool buying crossed into accumulation territory while net liquidity ticked up for a second straight reading, and manager exposure climbed to a bullish 84%.",
            isUnavailable = false
        )
    }
}

@Preview(name = "Dark — unavailable", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewSynthesisHeroCardUnavailable() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        SynthesisHeroCard(headline = null, detail = null, isUnavailable = true)
    }
}
