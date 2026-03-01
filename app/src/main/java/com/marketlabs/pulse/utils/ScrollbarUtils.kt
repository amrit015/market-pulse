package com.marketlabs.pulse.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 💡 NEW: Custom Scrollbar Modifier
fun Modifier.verticalScrollbar(
    state: ScrollState,
    width: Dp = 4.dp,
    color: Color = Color.Gray.copy(alpha = 0.5f) // 💡 Generic fallback
): Modifier = drawWithContent {
    // 1. Draw the actual column content first
    drawContent()

    val viewportHeight = this.size.height
    val totalContentHeight = viewportHeight + state.maxValue

    // 2. Only draw the scrollbar if the content is taller than the screen
    if (state.maxValue > 0) {
        // Calculate how tall the scrollbar thumb should be
        val scrollbarHeight = (viewportHeight / totalContentHeight) * viewportHeight

        // Calculate how far down the user has scrolled
        val scrollFraction = state.value.toFloat() / state.maxValue
        val scrollbarY = scrollFraction * (viewportHeight - scrollbarHeight)

        // Draw the sleek pill-shaped scrollbar on the right edge
        drawRoundRect(
            color = color,
            topLeft = Offset(this.size.width - width.toPx() - 4.dp.toPx(), scrollbarY), // 4dp padding from right edge
            size = Size(width.toPx(), scrollbarHeight),
            cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
        )
    }
}