package com.marketlabs.pulse.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 💡 THOUGHT PROCESS:
 * Compose's `MaterialTheme.colorScheme` covers the ~30 standard M3 roles (background, surface,
 * onBackground, onSurface, outline, etc.) but this app also needs a handful of tokens on top of
 * that (`surface.tinted`, `accent.surface`, `accent.surfaceBorder`, `signal.*.text`,
 * `signal.*.pill`). A custom `CompositionLocal` carrying this data class, provided alongside
 * `MaterialTheme` at the top of the theme tree, is the standard way to extend Compose's color
 * system without fighting it — every composable reads standard roles via `MaterialTheme.colorScheme`
 * and extended tokens via `LocalPulseColors.current`.
 *
 * `background`/`surface`/`surface.elevated`/`onBackground`/`onSurface`/`outline` are deliberately
 * NOT duplicated here — they already live on `MaterialTheme.colorScheme`, sourced from the same
 * mode's shared ramp in `MarketPulseTheme.toColorScheme()`.
 */
data class PulseColors(
    // --- Layer 1 — signal (locked, preset-invariant within a mode) ---
    val signalBullishText: Color,
    val signalBearishText: Color,
    val signalNeutralText: Color,
    val signalWarningText: Color,
    val signalBullishPill: Color,
    val signalBearishPill: Color,
    val signalNeutralPill: Color,
    val signalWarningPill: Color,

    /** No resolved value from Design yet — placeholder-mapped to the mode's `onSurfaceMuted` in `MarketPulseTheme.toPulseColors()`. TODO(session-3): resolve. */
    val signalUnknown: Color,

    // --- Layer 2 — accent group (per preset) ---
    val accentPrimary: Color,
    val accentOn: Color,
    val accentSurface: Color,
    val accentSurfaceBorder: Color,

    /** Accent-washed neutral used by every price card regardless of direction — the thing that decouples the accent's touch on price cards from the signal layer entirely. */
    val surfaceTinted: Color,

    // --- Layer 2 — surface ramp (shared per mode) ---
    val onSurfaceMuted: Color
)

val LocalPulseColors = staticCompositionLocalOf<PulseColors> {
    error("PulseColors not provided — wrap content in MarketPulseTheme")
}
