package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Animates from [flashColor] back to [restingColor] whenever [value] changes -- for a live-quote
 * price that refreshes underneath the user (`intradayPoller`, pull-to-refresh) rather than on
 * first load, so a refreshed number visibly reads as "this just changed" instead of silently
 * swapping in place. Fires only on an actual change to [value]: the very first composition (and
 * any recomposition where [value] is unchanged, e.g. a refresh that returned the same price)
 * leaves the color pinned at [restingColor].
 */
@Composable
fun animateFlashColor(
    value: Any?,
    flashColor: Color,
    restingColor: Color,
    durationMillis: Int = 900
): Color {
    var previousValue by remember { mutableStateOf(value) }
    val color = remember { Animatable(restingColor) }

    LaunchedEffect(value) {
        if (previousValue != value) {
            previousValue = value
            color.snapTo(flashColor)
            color.animateTo(restingColor, animationSpec = tween(durationMillis))
        }
    }

    return color.value
}
