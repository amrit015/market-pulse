package com.marketlabs.pulse.ui.screens.summary.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.SignalColor

/**
 * The "cheat-sheet curve" for `market_position.positioning` -- a schematic, not a plotted
 * distribution: the backend gives us three scalars (`range_position`, `extension_percentile_1y`,
 * the pct-from-high/low pair), never a real series of closes, so there is no actual distribution
 * to draw. The hump shape here is a fixed decorative silhouette (same "illustrative gauge, real
 * marker" split [PriceRangeBar] uses for its own simplifications, see that file's doc comment) --
 * only the marker dot's horizontal position (`rangePosition`, 0-100 = the window's low..high) and
 * its color (`signalColor`, already classified backend-side) are data-driven. The gradient itself
 * mirrors the backend's own contrarian convention (`indicators/positioningLogic.ts`'s
 * `classifyPositioning`): stretched-to-the-downside reads bullish/green, stretched-to-the-upside
 * reads bearish/red, the middle is neutral/yellow.
 */
@Composable
fun PositioningGauge(
    rangePosition: Double?,
    windowLabel: String?,
    signalText: String?,
    signalColor: SignalColor?,
    modifier: Modifier = Modifier
) {
    if (rangePosition == null) return

    val pulseColors = LocalPulseColors.current
    val fraction = (rangePosition / 100.0).coerceIn(0.0, 1.0).toFloat()
    // 💡 .textColor, not .pillColor -- same lesson already applied to the conviction meter, the
    // gauge's own signalText caption, and the driver pills elsewhere in this file: this app's
    // `signalXPill` tokens are deliberately soft/pastel, meant to be a *background* another color
    // sits on top of, not a standalone foreground color. Using them for the marker dot/stem and
    // the hump fill (which have nothing else layered on top of them to provide contrast) was
    // exactly the mistake that made both read as washed-out. `.textColor` is the more saturated
    // half of the same pair, tuned to read clearly on its own.
    val markerColor = signalColor.textColor
    val humpColor = Brush.horizontalGradient(
        listOf(
            pulseColors.signalBullishText.copy(alpha = 0.85f),
            pulseColors.signalNeutralText.copy(alpha = 0.85f),
            pulseColors.signalBearishText.copy(alpha = 0.85f)
        )
    )
    // 💡 Filled (no `style = Stroke(...)`), not just an outline -- an outlined track at the same
    // alpha read as a near-invisible hairline; a filled bar reads clearly at the same alpha,
    // same lesson PriceRangeBar's own track already applies. Bumped alpha again (0.30f still read
    // faint against this card's own tinted background).
    val trackColor = pulseColors.accentPrimary.copy(alpha = 0.55f)
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val trackInsetDp = dimensionResource(id = R.dimen.padding_medium)
    val humpHeightDp = dimensionResource(id = R.dimen.padding_xxlarge)
    val trackHeightDp = dimensionResource(id = R.dimen.padding_medium)
    val gapDp = dimensionResource(id = R.dimen.padding_small)
    val stemWidthDp = dimensionResource(id = R.dimen.gauge_needle_width)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(humpHeightDp + gapDp + trackHeightDp)
        ) {
            val trackInset = trackInsetDp.toPx()
            val trackWidth = size.width - 2 * trackInset
            val humpHeight = humpHeightDp.toPx()
            val trackTop = humpHeight + gapDp.toPx()
            val trackHeight = trackHeightDp.toPx()

            // 💡 Decorative hump silhouette only -- see the doc comment above. A single cubic
            // bezier from the bottom-left corner up to a peak just past center and back down,
            // the same "smooth single-hump" shape a bell curve reads as at a glance without
            // claiming to be a fitted distribution.
            val humpPath = Path().apply {
                moveTo(trackInset, humpHeight)
                cubicTo(
                    trackInset + trackWidth * 0.15f, humpHeight * 0.15f,
                    trackInset + trackWidth * 0.55f, 0f,
                    trackInset + trackWidth * 0.5f, 0f
                )
                cubicTo(
                    trackInset + trackWidth * 0.45f, 0f,
                    trackInset + trackWidth * 0.85f, humpHeight * 0.15f,
                    trackInset + trackWidth, humpHeight
                )
                close()
            }
            drawPath(path = humpPath, brush = humpColor)

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(trackInset, trackTop),
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2, trackHeight / 2)
            )

            val markerX = trackInset + fraction * trackWidth
            val dotRadius = trackHeight
            val dotCenterY = trackTop + trackHeight / 2f
            drawLine(
                color = markerColor,
                start = Offset(markerX, 0f),
                end = Offset(markerX, dotCenterY - dotRadius),
                strokeWidth = stemWidthDp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = markerColor, radius = dotRadius, center = Offset(markerX, dotCenterY))
        }

        signalText?.let {
            // 💡 A pill (pillColor + textColor together), not plain text in `markerColor` alone --
            // a signal color's pill/text pair is tuned as a matched unit for contrast against
            // *its own* background; using just one half of that pair as a foreground color over
            // an unrelated card background (this card is DATA-styled/`surfaceTinted`) produced
            // washed-out, near-illegible text in several presets sharing a hue family with that
            // background. Same reasoning `DriversSection`'s pills and `RisksSection`'s severity
            // pills already follow.
            SignalPill(
                text = it,
                pillColor = signalColor.pillColor,
                contentColor = signalColor.textColor,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
            )
        }

        val window = windowLabel ?: stringResource(id = R.string.label_window_fallback)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = trackInsetDp, vertical = dimensionResource(id = R.dimen.padding_tiny))
        ) {
            Text(
                text = stringResource(id = R.string.label_window_low, window),
                style = MaterialTheme.typography.labelSmall,
                color = axisLabelColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(id = R.string.label_window_high, window),
                style = MaterialTheme.typography.labelSmall,
                color = axisLabelColor
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewPositioningGaugeLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PositioningGauge(
            rangePosition = 93.17,
            windowLabel = "251-day",
            signalText = "Extended Near Highs",
            signalColor = SignalColor.RED,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPositioningGaugeDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PositioningGauge(
            rangePosition = 22.0,
            windowLabel = "52-week",
            signalText = "Lower Range",
            signalColor = SignalColor.YELLOW,
            modifier = Modifier.padding(16.dp)
        )
    }
}
