package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The small-caps "eyebrow" label every SYNTHESIS-family card header uses -- Market Signal, Market
 * Sentiment, Market Read, and Where Capital's Moving (all Summary), Today's Read (Indicators),
 * and Digest (Insights' `SynthesisHeroCard`, shared by Playbook/Risks/Posture/Positioning). One
 * shared labelSmall+uppercase+bold text style so these six call sites can't drift back out of
 * sync with each other the way they'd been hand-kept-in-sync before this was pulled out.
 *
 * Each caller still owns its own color and whether it carries the leading AI-sparkle icon -- those
 * genuinely differ (Market Signal/Sentiment/Read/Where Capital's Moving have neither icon nor the
 * `accentPrimary` tint; Today's Read/Digest both do) -- and any trailing affordance (a nav
 * chevron, an expand/collapse arrow) stays the caller's own concern, composed alongside this
 * rather than inside it, since that differs by interaction model (navigate away vs. expand in
 * place vs. neither), not by what the label itself looks like.
 */
@Composable
fun CardEyebrowLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    iconContentDescription: String? = null
) {
    val textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)

    if (iconRes == null) {
        Text(text = text.uppercase(), style = textStyle, color = color, modifier = modifier)
        return
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // 💡 Icon size is derived from the text style's own font size, not a separate fixed
        // dimension -- it shrinks/grows in lockstep with the label text automatically.
        val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = iconContentDescription,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
        Text(text = text.uppercase(), style = textStyle, color = color)
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light — no icon", showBackground = true)
@Composable
private fun PreviewCardEyebrowLabelNoIcon() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        CardEyebrowLabel(text = "Market Signal", color = MaterialTheme.colorScheme.primary)
    }
}

@Preview(name = "Dark — with icon", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewCardEyebrowLabelWithIcon() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        CardEyebrowLabel(
            text = "Digest",
            color = LocalPulseColors.current.accentPrimary,
            iconRes = R.drawable.ic_ai_sparkle_filled
        )
    }
}
