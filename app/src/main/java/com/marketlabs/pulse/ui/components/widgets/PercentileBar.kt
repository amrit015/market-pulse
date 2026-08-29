package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * A 0-100 track with a single dot marking where the current reading falls -- CFTC COT futures
 * positioning's trailing-year percentile, per the Positioning design mockup's structure. The "0"/
 * "Percentile N"/"100" labels around it are left to the caller (a plain `Row` of `Text`) rather
 * than baked in here, so this stays a reusable bare track+marker for any future 0-100 percentile
 * reading, not something COT-specific.
 */
@Composable
fun PercentileBar(percentile: Int, markerColor: Color, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val trackHeight = dimensionResource(id = R.dimen.percentile_bar_track_height)
    val markerRadius = dimensionResource(id = R.dimen.percentile_bar_marker_radius)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(markerRadius * 2)
    ) {
        val centerY = size.height / 2
        val trackHeightPx = trackHeight.toPx()
        val markerRadiusPx = markerRadius.toPx()
        val usableWidth = size.width - markerRadiusPx * 2

        drawLine(
            color = trackColor,
            start = Offset(markerRadiusPx, centerY),
            end = Offset(size.width - markerRadiusPx, centerY),
            strokeWidth = trackHeightPx,
            cap = StrokeCap.Round
        )

        val fraction = (percentile / 100f).coerceIn(0f, 1f)
        drawCircle(
            color = markerColor,
            radius = markerRadiusPx,
            center = Offset(markerRadiusPx + usableWidth * fraction, centerY)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewPercentileBar() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PercentileBar(percentile = 42, markerColor = MaterialTheme.colorScheme.primary)
    }
}
