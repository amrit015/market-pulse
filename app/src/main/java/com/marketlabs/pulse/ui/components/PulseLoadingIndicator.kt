package com.marketlabs.pulse.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Indeterminate loading treatment used while a screen is waiting for its first set of data.
 * It briefly begins as the completed launcher mark, clears, then reconstructs it continuously.
 *
 * `size` defaults to the original full-screen treatment's 184dp but is otherwise free to shrink —
 * `LauncherMarkAnimation`'s geometry is derived from its own `DrawScope.size` (`size.minDimension /
 * 108f`), so passing a smaller `size` here scales the whole mark proportionally rather than clipping
 * it. Used at a small size for `DashboardRoute`'s pull-to-refresh indicator, in place of Material's
 * default spinner, for brand consistency with the splash screen and every other loading state.
 */
@Composable
fun PulseLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 184.dp
) {
    val transition = rememberInfiniteTransition(label = "pulse-loader")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            // Briefly show the completed mark, then reset the rings and begin the sequence. The
            // chart bars remain filled throughout.
            animation = keyframes {
                durationMillis = 2_650
                1f at 0 using LinearEasing
                1f at 250 using LinearEasing
                0f at 251 using LinearEasing
                1f at 2_650 using LinearEasing
            }
        ),
        label = "launcher-mark-progress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // A low-opacity version of the real launcher drawable keeps the loader tied to the
            // app mark while the canvas above redraws its individual pieces in sequence.
            Image(
                painter = painterResource(id = R.drawable.app_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .alpha(0.14f)
                    .scale(1.42f)
            )
            LauncherMarkAnimation(
                progress = progress.value,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
private fun LauncherMarkAnimation(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val scale = size.minDimension / 108f
        // The launcher foreground has a 0.65x safe-zone group. The dimmed drawable above is
        // enlarged by 1.42x, so the animated geometry uses the same combined scale.
        val markScale = 0.65f * 1.42f
        fun Float.coordinatePx() = ((this - 54f) * markScale + 54f) * scale
        fun Float.lengthPx() = this * markScale * scale
        fun phase(start: Float, duration: Float) =
            ((progress - start) / duration).coerceIn(0f, 1f)

        // Rings begin about 0.5 seconds apart, from the inner ring to the outer ring. The final
        // arc completes exactly at the end of the rebuild, which triggers the next loop.
        listOf(
            Triple(21f, Color(0xFFBFAEE3), 0f),
            Triple(28f, Color(0xFF514E6B), 0.21f),
            Triple(35f, Color(0xFF514E6B), 0.42f),
            Triple(42f, Color(0xFF514E6B), 0.63f)
        ).forEach { (radius, color, start) ->
            val ringProgress = phase(start = start, duration = 0.37f)
            if (ringProgress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * FastOutSlowInEasing.transform(ringProgress),
                    useCenter = false,
                    topLeft = Offset(
                        (54f - radius).coordinatePx(),
                        (54f - radius).coordinatePx()
                    ),
                    size = Size((radius * 2).lengthPx(), (radius * 2).lengthPx()),
                    style = Stroke(width = 1.5f.lengthPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Match the foreground's chart geometry. These remain filled while only the rings cycle.
        data class Bar(val left: Float, val top: Float, val height: Float, val color: Color)
        listOf(
            Bar(29.5f, 55f, 21f, Color(0xFF92C17D)),
            Bar(47.5f, 45f, 31f, Color(0xFF92C17D)),
            Bar(65.5f, 35f, 41f, Color(0xFFF08E8F))
        ).forEach { bar ->
            drawRoundRect(
                color = bar.color,
                topLeft = Offset(bar.left.coordinatePx(), bar.top.coordinatePx()),
                size = Size(13f.lengthPx(), bar.height.lengthPx()),
                cornerRadius = CornerRadius(3f.lengthPx(), 3f.lengthPx())
            )
        }
    }
}

/** Full-screen Compose splash shown after Android's required static system splash icon. */
@Composable
fun PulseSplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(contentAlignment = Alignment.Center) {
            PulseLoadingIndicator()
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PulseLoadingIndicatorLightPreview() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PulseLoadingIndicator()
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PulseLoadingIndicatorDarkPreview() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PulseLoadingIndicator()
    }
}
