package com.marketlabs.pulse.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Configures the Material 3 ColorScheme for MarketLabs Pulse.
 * Action: Implement dual-mode support with high-contrast Dark and Light variants.
 * Action: Update SideEffect to handle transparent system bars for edge-to-edge.
 */

/** * High-Contrast Dark Scheme
 * Action: Use pure black background for an authoritative, "Pulse" aesthetic.
 */
/** Dark Scheme configuration */
private val PulseDarkColorScheme = darkColorScheme(
    primary = PulseGold,
    secondary = PulseOrange,
    onPrimary = PulseBlack,
    background = PulseBlack,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    // Action: Use named constant to avoid blueish tints and ensure consistency
    surfaceVariant = PulseDeepGray,
    onSurfaceVariant = DarkOnSurface,
    error = AlertRed
)

/** Light Scheme configuration */
private val PulseLightColorScheme = lightColorScheme(
    primary = PulseBlue,
    secondary = PulseBlack,
    onPrimary = PulseBlack,
    background = LightBackground,
    onBackground = PulseBlack,
    surface = LightSurface,
    onSurface = PulseBlack,
    surfaceVariant = Color.White,
    error = AlertRed
)

@Composable
fun MarketPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Action: Select color palette based on theme preference
    val colorScheme = if (darkTheme) PulseDarkColorScheme else PulseLightColorScheme

    // Action: Handle System UI and Status Bar integration
    val view = LocalView.current
    /** Action: Handle system bar icon contrast using the modern Controller API */
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                val controller = WindowCompat.getInsetsController(it, view)

                /** * Action: Set icon contrast.
                 * In PulseBlack (Dark), icons must be light (isAppearanceLightStatusBars = false).
                 * In PulseLight (Light), icons must be dark (isAppearanceLightS
                 * tatusBars = true).
                 */
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

/** * Helper extension to locate the Activity context
 * Action: Use tailrec for efficient context traversal.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}