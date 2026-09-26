package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.marketlabs.pulse.ui.theme.LocalPulseColors

/**
 * The 9 named color roles every Learn/Tutorials diagram draws with -- deliberately excludes
 * `SignalColor` (bullish/bearish/neutral/warning) so a diagram never implies a live buy/sell read.
 * [band]/[fill] are alpha steps of [emphasis] rather than separate palette entries, so a new preset
 * or theme mode is correctly tinted here with no extra work.
 */
@Immutable
data class DiagramColors(
    val ink: Color,
    val inkMuted: Color,
    val emphasis: Color,
    val onEmphasis: Color,
    val band: Color,
    val fill: Color,
    val edge: Color,
    val rule: Color,
    val card: Color
)

@Composable
fun rememberDiagramColors(): DiagramColors {
    val pulseColors = LocalPulseColors.current
    val colorScheme = MaterialTheme.colorScheme
    return remember(pulseColors, colorScheme) {
        DiagramColors(
            ink = colorScheme.onSurface,
            inkMuted = pulseColors.onSurfaceMuted,
            emphasis = pulseColors.accentPrimary,
            onEmphasis = pulseColors.accentOn,
            band = pulseColors.accentPrimary.copy(alpha = 0.12f),
            fill = pulseColors.accentPrimary.copy(alpha = 0.24f),
            edge = pulseColors.accentSurfaceBorder,
            rule = colorScheme.outline,
            card = colorScheme.surfaceVariant
        )
    }
}
