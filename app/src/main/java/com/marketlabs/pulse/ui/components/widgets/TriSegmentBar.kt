package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Three-way proportion bar for AAII retail sentiment's bull/neutral/bear split, per the Positioning
 * design mockup's structure. A weighted `Row` of three colored `Box` segments, using the
 * `signalBullish/Neutral/Bearish TEXT` tokens -- NOT the `...Pill` tokens `SignalPill` uses for its
 * own fill. `Pill` tokens are deliberately pale (SignalPill.kt: "meant to sit behind bright text"),
 * so using them as a large solid fill here read as washed-out/faded rather than the natural
 * green/neutral/red this bar is meant to show at a glance. `Text` tokens are the saturated,
 * standalone-strength versions of the same three colors (already used unpilled elsewhere, e.g. VIX's
 * plain "GREED"/"FEAR" label) -- correct for a bar that IS the color, not text sitting on top of one.
 * A fraction of exactly zero skips that segment entirely rather than rendering a zero-width
 * `weight`, which `RowScope.weight` would otherwise happily do.
 */
@Composable
fun TriSegmentBar(
    bullFraction: Float,
    neutralFraction: Float,
    bearFraction: Float,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.tri_segment_bar_height))
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.tri_segment_bar_height) / 2))
    ) {
        if (bullFraction > 0f) {
            Box(modifier = Modifier.weight(bullFraction).fillMaxHeight().background(pulseColors.signalBullishText))
        }
        if (neutralFraction > 0f) {
            Box(modifier = Modifier.weight(neutralFraction).fillMaxHeight().background(pulseColors.signalNeutralText))
        }
        if (bearFraction > 0f) {
            Box(modifier = Modifier.weight(bearFraction).fillMaxHeight().background(pulseColors.signalBearishText))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewTriSegmentBar() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TriSegmentBar(bullFraction = 0.355f, neutralFraction = 0.246f, bearFraction = 0.399f)
    }
}
