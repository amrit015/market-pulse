package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlin.math.roundToInt

/**
 * Small circular progress ring + centered value, for the Posture cards (NAAIM Exposure, Dark Pool
 * Index) that read as a bounded 0-100-ish gauge next to the card's title/status, per the Posture
 * design mockup's structure. A purpose-built, smaller sibling of `ScoreGauge` rather than a reuse
 * of it -- `ScoreGauge` is hardcoded to an Int 0-100 score with its own baked-in delta/status text
 * layout (Risk Radar's full-size hero gauge), where this is a compact side-of-card indicator that
 * takes a raw Double against a caller-supplied `maxValue` (NAAIM can read above 100, up to ~150)
 * and leaves delta/status entirely to the caller (`DirectionalChangePill`/`SignalPill` sit next to
 * it, not inside it).
 */
@Composable
fun RingGauge(
    value: Double,
    maxValue: Double,
    ringColor: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = dimensionResource(id = R.dimen.gauge_size_small)
) {
    val progress = (value / maxValue).toFloat().coerceIn(0f, 1f)
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val strokeWidthDp = dimensionResource(id = R.dimen.gauge_stroke_width_small)
    val density = LocalDensity.current

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = with(density) { strokeWidthDp.toPx() }
            val inset = strokeWidthPx / 2
            val arcSize = Size(this.size.width - strokeWidthPx, this.size.height - strokeWidthPx)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor,
                startAngle = 270f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        Text(
            text = value.roundToInt().toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewRingGauge() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        RingGauge(value = 84.2, maxValue = 100.0, ringColor = MaterialTheme.colorScheme.primary)
    }
}
