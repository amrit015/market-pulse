package com.marketlabs.pulse.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize

private const val CardExpandDurationMs = 400
private const val ExpandPulsePeakScale = 1.012f
private const val ExpandPulseGrowMs = 180
private const val ExpandPulseSettleMs = 320

// 💡 Shared by every tap-to-expand card's `animateContentSize(...)` so they all open at the same
// pace. The default spring snaps open noticeably faster than the eye can follow on tall cards.
val CardExpandAnimationSpec = tween<IntSize>(
    durationMillis = CardExpandDurationMs,
    easing = FastOutSlowInEasing
)

/**
 * A barely-there "swell" on the whole card when [expanded] flips (either direction), layered on top
 * of the height animation so the card reads as growing rather than just getting taller. Apply to
 * the `PulseCard`'s own modifier -- unlike `animateContentSize`, a `graphicsLayer` scale doesn't
 * clip, so it is safe outside the card's shadow. The first composition is skipped so a card that
 * starts expanded (or is scrolled into view) doesn't pulse.
 */
@Composable
fun Modifier.cardExpandPulse(expanded: Boolean): Modifier {
    val scale = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(expanded) }
    LaunchedEffect(expanded) {
        if (expanded == previous) return@LaunchedEffect
        previous = expanded
        scale.animateTo(ExpandPulsePeakScale, tween(ExpandPulseGrowMs, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(ExpandPulseSettleMs, easing = FastOutSlowInEasing))
    }
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
