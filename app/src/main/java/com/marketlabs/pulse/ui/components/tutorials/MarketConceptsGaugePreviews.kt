package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.ui.components.widgets.PercentileBar
import com.marketlabs.pulse.ui.components.widgets.PutCallHorizontalBar
import com.marketlabs.pulse.ui.components.widgets.RingGauge
import com.marketlabs.pulse.ui.components.widgets.SpeedometerGauge
import com.marketlabs.pulse.ui.components.widgets.TriSegmentBar
import com.marketlabs.pulse.ui.components.widgets.VixFullWidthCard
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.AssetType
import com.marketlabs.pulse.utils.enums.CotPositioningStatus
import com.marketlabs.pulse.utils.enums.DixStatus
import com.marketlabs.pulse.utils.enums.NaaimStatus

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
 * being misread as today's actual reading. The caller wraps this in `DiagramScaffold`'s
 * `LIVE_WIDGET_PREVIEW` note, which says so explicitly below the gauges.
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
    }
}

/**
 * A single real VIX card at a calm reading -- VIX is named in the Volatility Regimes article's own
 * text (`volatility_regimes.in_market_pulse_diagram`), so this shows the reader exactly what that
 * reading looks like inside the app rather than a redrawn illustration.
 */
@Composable
fun VixCardPreview() {
    VixFullWidthCard(
        asset = AssetOverview(
            symbol = "VIX",
            name = "CBOE Volatility Index",
            type = AssetType.INDEX,
            price = 14.0,
            changePercent = -1.2,
            rsiStatus = "GREED"
        ),
        onClick = {}
    )
}

/**
 * The real NAAIM Exposure and Dark Pool Index ring gauges, colored exactly as
 * [com.marketlabs.pulse.ui.screens.insights.views.MarketPostureView] derives them (status enum ->
 * `textColor`, both pure functions of the status string, not ViewModel state). Values mirror the
 * Posture deck's own `in_the_wild` scenario (`posture.what_to_watch_diagram` in
 * `learn_content.json`) -- NAAIM 38 (Extreme Fear/Hedged), DIX 47 (Accumulation).
 */
@Composable
fun PostureRingsPreview(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GaugePreviewLabel(text = stringResource(id = R.string.posture_naaim_title))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            RingGauge(value = 38.0, maxValue = 100.0, ringColor = NaaimStatus.EXTREME_FEAR_HEDGED.textColor)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GaugePreviewLabel(text = stringResource(id = R.string.posture_dix_title))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            RingGauge(value = 47.0, maxValue = 100.0, ringColor = DixStatus.ACCUMULATION_BULLISH.textColor)
        }
    }
}

/**
 * The real CFTC COT percentile bar and AAII bull/neutral/bear split, colored exactly as
 * [com.marketlabs.pulse.ui.screens.insights.views.MarketPositioningView] derives them. Values
 * mirror the Positioning deck's own `in_the_wild` scenario (`positioning.what_to_watch_diagram` in
 * `learn_content.json`) -- COT percentile 22, AAII bull 20% / neutral 25% / bear 55% (a −35% spread).
 */
@Composable
fun PositioningBarsPreview(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        GaugePreviewLabel(text = stringResource(id = R.string.tutorial_gauge_cot_percentile_label))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        PercentileBar(percentile = 22, markerColor = CotPositioningStatus.NEUTRAL.textColor)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
        GaugePreviewLabel(text = stringResource(id = R.string.tutorial_gauge_aaii_label))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        TriSegmentBar(bullFraction = 0.20f, neutralFraction = 0.25f, bearFraction = 0.55f)
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

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewVixCardPreviewLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        VixCardPreview()
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewVixCardPreviewDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        VixCardPreview()
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewPostureRingsPreviewLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PostureRingsPreview()
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPostureRingsPreviewDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PostureRingsPreview()
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewPositioningBarsPreviewLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PositioningBarsPreview()
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPositioningBarsPreviewDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PositioningBarsPreview()
    }
}
