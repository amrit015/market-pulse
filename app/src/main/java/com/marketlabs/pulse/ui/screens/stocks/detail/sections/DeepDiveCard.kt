package com.marketlabs.pulse.ui.screens.stocks.detail.sections

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.GlowOrigin
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.buildBulletJoinedText
import com.marketlabs.pulse.ui.screens.stocks.detail.ViewMoreRow
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Chrome-level `PulseCard(SYNTHESIS)` on Stock Detail: an icon + date line, a fixed description, and a CTA.
 * When [isFlashing] is true, renders a continuous glow that fills the card outward from the top-left corner toward the bottom-right.
 */
@Composable
fun DeepDiveCard(
    deepAnalysisDate: String?,
    nextDeepDiveTriggerDate: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFlashing: Boolean = false
) {
    val parts = deepDiveDisplayParts(deepAnalysisDate, nextDeepDiveTriggerDate)
    if (parts.isEmpty()) return
    val hasDeepDive = deepAnalysisDate != null
    val pulseColors = LocalPulseColors.current

    // 💡 The accent-colored glow reads clearly on a dark card but not on light mode's tinted card,
    // so light mode gets a plain white, stronger glow instead. It has to be pure white: that card's
    // background is itself 75% white + 25% accent, so any accent-tinted "whitish" mix lands on the
    // exact same color as the card and the glow disappears.
    val glowColor = if (pulseColors.isDark) pulseColors.accentPrimary else Color.White
    val glowPeakAlpha = if (pulseColors.isDark) DarkGlowPeakAlpha else LightGlowPeakAlpha

    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = modifier.fillMaxWidth(),
        onClick = if (hasDeepDive) onClick else null,
        glowColor = if (isFlashing) glowColor else null,
        glowPeakAlpha = glowPeakAlpha,
        glowOrigin = GlowOrigin.TOP_LEFT,
        glowFill = true
    ) {
        Column(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_standard), horizontal = dimensionResource(id = R.dimen.padding_large))) {
            val labelStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            Row( verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_deep_dive),
                    contentDescription = stringResource(id = R.string.stock_analysis_ai_glyph_content_description),
                    tint = pulseColors.accentPrimary,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large))
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = buildBulletJoinedText(
                        parts = parts,
                        bullet = stringResource(id = R.string.bullet_separator),
                        baseFontSize = MaterialTheme.typography.labelSmall.fontSize
                    ),
                    style = labelStyle,
                    color = pulseColors.accentPrimary
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            Text(
                text = stringResource(id = R.string.deep_dive_card_description),
                style = MaterialTheme.typography.bodySmall,
                color = pulseColors.onSurfaceMuted
            )
            if (hasDeepDive) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                ViewMoreRow(text = stringResource(id = R.string.deep_dive_open_full_cta), onClick = onClick)
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Both dates", showBackground = true)
@Composable
private fun PreviewDeepDiveCardBoth() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DeepDiveCard(deepAnalysisDate = "2026-09-04", nextDeepDiveTriggerDate = "2026-09-18", onClick = {})
    }
}

@Preview(name = "Next only (cold start)", showBackground = true)
@Composable
private fun PreviewDeepDiveCardNextOnly() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DeepDiveCard(deepAnalysisDate = null, nextDeepDiveTriggerDate = "2026-09-18", onClick = {})
    }
}

// How opaque each mode's glow gets at its brightest (the shared default is 0.32).
private const val LightGlowPeakAlpha = 0.75f
private const val DarkGlowPeakAlpha = 0.32f
