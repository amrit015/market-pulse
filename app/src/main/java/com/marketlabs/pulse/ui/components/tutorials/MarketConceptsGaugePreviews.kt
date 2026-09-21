package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.widgets.PercentileBar
import com.marketlabs.pulse.ui.components.widgets.PutCallHorizontalBar
import com.marketlabs.pulse.ui.components.widgets.SpeedometerGauge
import com.marketlabs.pulse.ui.components.widgets.TriSegmentBar
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The Sentiment & Positioning
 * section of Market Concepts names four gauges by name in its own bullet list (Fear & Greed,
 * Put/Call, CFTC COT, AAII) -- this renders the REAL production widget for each one, not a
 * redrawn illustration, so the reader sees exactly what that gauge looks like inside the app.
 * Used instead of [SentimentGaugesDiagram] (a hand-drawn 3-bar illustration covering Fear & Greed/
 * Put-Call/VIX) at this spot -- VIX isn't named anywhere in this section's own text, so it isn't
 * included here; that diagram is still defined and valid, just not wired to this call
 * site.
 *
 * Each gauge carries its own short "how to read it" line directly underneath -- the
 * interpretive explanation (contrarian
 * framing, "extremes can persist," COT/AAII's own gotchas), broken out per-gauge instead of one
 * combined caption, since these are four real, separately-labeled widgets rather than one
 * shared abstract diagram.
 *
 * Every value below is a fixed, neutral illustrative reading (flat/50th-percentile/even thirds),
 * not live data -- this screen has no data source of its own, and a skewed mock value would risk
 * being misread as today's actual reading. [R.string.tutorial_gauge_illustrative_caption] says so
 * explicitly at the end.
 */
@Composable
fun SentimentGaugesLivePreview(modifier: Modifier = Modifier) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    Column(modifier = modifier.fillMaxWidth()) {
        GaugePreviewLabel(text = stringResource(id = R.string.tutorial_diagram_fear_greed_label))
        Spacer(modifier = Modifier.height(paddingSmall))
        SpeedometerGauge(score = 50.0, previousScore = 50.0, status = "NEUTRAL")
        Spacer(modifier = Modifier.height(paddingSmall))
        GaugePreviewExplanation(text = stringResource(id = R.string.tutorial_gauge_fear_greed_explanation))

        Spacer(modifier = Modifier.height(paddingLarge))
        GaugePreviewLabel(text = stringResource(id = R.string.tutorial_diagram_put_call_label))
        Spacer(modifier = Modifier.height(paddingSmall))
        PutCallHorizontalBar(ratio = 0.80, change = 0.0, status = "NEUTRAL")
        Spacer(modifier = Modifier.height(paddingSmall))
        GaugePreviewExplanation(text = stringResource(id = R.string.tutorial_gauge_put_call_explanation))

        Spacer(modifier = Modifier.height(paddingLarge))
        GaugePreviewLabel(text = stringResource(id = R.string.tutorial_gauge_cot_percentile_label))
        Spacer(modifier = Modifier.height(paddingSmall))
        PercentileBar(percentile = 50, markerColor = LocalPulseColors.current.accentPrimary)
        Spacer(modifier = Modifier.height(paddingSmall))
        GaugePreviewExplanation(text = stringResource(id = R.string.tutorial_gauge_cot_percentile_explanation))

        Spacer(modifier = Modifier.height(paddingLarge))
        GaugePreviewLabel(text = stringResource(id = R.string.tutorial_gauge_aaii_label))
        Spacer(modifier = Modifier.height(paddingSmall))
        TriSegmentBar(bullFraction = 0.34f, neutralFraction = 0.33f, bearFraction = 0.33f)
        Spacer(modifier = Modifier.height(paddingSmall))
        GaugePreviewExplanation(text = stringResource(id = R.string.tutorial_gauge_aaii_explanation))

        Spacer(modifier = Modifier.height(paddingMedium))
        Text(
            text = stringResource(id = R.string.tutorial_gauge_illustrative_caption),
            style = MaterialTheme.typography.labelSmall,
            color = LocalPulseColors.current.onSurfaceMuted
        )
    }
}

@Composable
private fun GaugePreviewLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun GaugePreviewExplanation(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = LocalPulseColors.current.onSurfaceMuted
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewSentimentGaugesLivePreviewLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SentimentGaugesLivePreview()
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewSentimentGaugesLivePreviewDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        SentimentGaugesLivePreview()
    }
}
