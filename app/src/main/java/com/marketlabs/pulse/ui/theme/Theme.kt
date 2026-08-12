package com.marketlabs.pulse.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 💡 THOUGHT PROCESS:
 * Full rewrite for the theme migration. The old signature — `MarketPulseTheme(darkTheme: Boolean =
 * isSystemInDarkTheme(), content)` — is gone: the theme picker now overrides OS dark mode by
 * design, so `isSystemInDarkTheme()` is never consulted. The composable takes a resolved
 * `theme: MarketPulseTheme` and derives both the standard `ColorScheme` and the extended
 * `PulseColors` from it via `theme.toColorScheme()` / `theme.toPulseColors()` (both in
 * `MarketPulseTheme.kt`).
 *
 * A same-named top-level function and enum in one file/package is legal Kotlin here without
 * ambiguity — `MarketPulseTheme` the enum has no externally-invokable constructor, so a call site
 * like `MarketPulseTheme(theme = MarketPulseTheme.LILAC) { ... }` only ever resolves to this
 * function.
 *
 * This composable deliberately does NOT know about `ThemeRepository` — it stays a pure function of
 * its `theme` parameter (easier to `@Preview`, no Hilt dependency here). The caller (`MainActivity`)
 * collects `ThemeRepository.selectedTheme` and passes the current value in — see that file for why
 * this split was chosen over an `EntryPointAccessors`-based lookup inside the composable itself.
 */
@Composable
fun MarketPulseTheme(
    theme: MarketPulseTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = theme.toColorScheme()
    val pulseColors = theme.toPulseColors()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                val controller = WindowCompat.getInsetsController(it, view)
                // isDark now comes straight off the selected preset, not isSystemInDarkTheme().
                controller.isAppearanceLightStatusBars = !theme.isDark
                controller.isAppearanceLightNavigationBars = !theme.isDark
            }
        }
    }

    CompositionLocalProvider(LocalPulseColors provides pulseColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
