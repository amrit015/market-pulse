package com.marketlabs.pulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 💡 THOUGHT PROCESS:
 * Ten baked-mode presets (5 light + 5 dark) — the theme picker IS the appearance picker, a chosen
 * preset overrides OS dark mode, there's no separate light/dark toggle. `toPulseColors()` resolves
 * the extended-token layer (`PulseColors`, for `LocalPulseColors`); `toColorScheme()` resolves the
 * standard M3 roles (for `MaterialTheme.colorScheme`). Both draw from the same three data sources
 * in `Color.kt` — `PulseTokens.Signal`, `.Surface`, `.Accent` — so there is exactly one place a
 * hex value lives.
 *
 * `colorScheme.primary`/`onPrimary` are deliberately set to the SAME values as
 * `LocalPulseColors.current.accentPrimary`/`.accentOn` — not a second source of truth, just so
 * built-in M3 components (ripples, default `Switch`/`CircularProgressIndicator` tinting, etc.)
 * that read `colorScheme.primary` internally stay visually coherent with the accent. This app's
 * OWN composables should still read `LocalPulseColors.current.accentPrimary` directly per the
 * migration table, not `colorScheme.primary` — see `spec-20260809-theme-migration.md`.
 *
 * `secondary`/`secondaryContainer` have no dedicated Token Contract entry — the spec says "both
 * modes use the standard M3 secondary mapping from the shared surface ramp" without giving exact
 * hexes. Resolved here as `secondary = onSurfaceMuted` / `secondaryContainer = surfaceElevated`
 * (ramp tokens, not invented values) since existing call sites use `secondary` for muted
 * meta-text/labels and `secondaryContainer.copy(alpha=…)` for a subtle card-background wash —
 * both read correctly against these. Flagging per the spec's own "flag in-line rather than guess"
 * instruction for exactly this kind of gap.
 *
 * `error`/`errorContainer`/`onError`/`onErrorContainer` are intentionally left OUT of the
 * `ColorScheme` builder calls below — neither the Token Contract nor the Design Direction defines
 * an error/danger token (the closest thing, `AlertRed`, is explicitly deleted by this migration).
 * Same "don't invent a value" principle the spec applies to `signal.unknown`: M3's own
 * `lightColorScheme()`/`darkColorScheme()` baseline default takes over for these four roles.
 */
enum class MarketPulseTheme(val displayName: String, val isDark: Boolean) {
    // --- Light presets ---
    PLUM("Plum", isDark = false),
    NAVY("Navy", isDark = false),
    FUCHSIA("Fuchsia", isDark = false),
    GRAPHITE("Graphite", isDark = false),
    TEAL("Teal", isDark = false),

    // --- Dark presets ---
    LILAC("Lilac", isDark = true),
    SKY("Sky", isDark = true),
    SAND("Sand", isDark = true),
    ROSE("Rose", isDark = true),
    AQUA("Aqua", isDark = true);

    fun toPulseColors(): PulseColors {
        val accent = accentGroup()
        val signal = if (isDark) PulseTokens.Signal.Dark else PulseTokens.Signal.Light
        val surface = if (isDark) PulseTokens.Surface.Dark else PulseTokens.Surface.Light

        return PulseColors(
            signalBullishText = signal.bullishText,
            signalBearishText = signal.bearishText,
            signalNeutralText = signal.neutralText,
            signalWarningText = signal.warningText,
            signalBullishPill = signal.bullishPill,
            signalBearishPill = signal.bearishPill,
            signalNeutralPill = signal.neutralPill,
            signalWarningPill = signal.warningPill,
            signalUnknown = surface.onSurfaceMuted, // TODO(session-3): resolve signal.unknown
            accentPrimary = accent.primary,
            accentOn = accent.on,
            accentSurface = accent.surface,
            accentSurfaceBorder = accent.surfaceBorder,
            surfaceTinted = accent.tinted,
            onSurfaceMuted = surface.onSurfaceMuted
        )
    }

    fun toColorScheme(): ColorScheme {
        val accent = accentGroup()
        val surface = if (isDark) PulseTokens.Surface.Dark else PulseTokens.Surface.Light

        // 💡 error/onError/errorContainer/onErrorContainer are deliberately NOT passed below —
        // omitting them lets lightColorScheme()/darkColorScheme()'s own default parameter values
        // (M3's baseline error palette, already correct per-mode) apply, rather than hand-picking
        // a value neither the Token Contract nor the Design Direction defines. See file header.
        val scheme = if (isDark) {
            darkColorScheme(
                primary = accent.primary,
                onPrimary = accent.on,
                primaryContainer = accent.surface,
                onPrimaryContainer = surface.onSurface,
                secondary = surface.onSurfaceMuted,
                onSecondary = surface.onBackground,
                secondaryContainer = surface.surfaceElevated,
                onSecondaryContainer = surface.onSurface,
                tertiary = accent.primary,
                onTertiary = accent.on,
                tertiaryContainer = accent.surface,
                onTertiaryContainer = surface.onSurface,
                background = surface.background,
                onBackground = surface.onBackground,
                surface = surface.surface,
                onSurface = surface.onSurface,
                surfaceVariant = surface.surfaceElevated,
                onSurfaceVariant = surface.onSurfaceMuted,
                surfaceTint = accent.primary,
                inverseSurface = surface.onSurface,
                inverseOnSurface = surface.surface,
                inversePrimary = accent.primary,
                outline = surface.outline,
                outlineVariant = surface.outline,
                surfaceBright = surface.surfaceElevated,
                surfaceContainer = surface.surfaceElevated,
                surfaceContainerHigh = surface.surfaceElevated,
                surfaceContainerHighest = surface.surfaceElevated,
                surfaceContainerLow = surface.surface,
                surfaceContainerLowest = surface.background,
                surfaceDim = surface.background
            )
        } else {
            lightColorScheme(
                primary = accent.primary,
                onPrimary = accent.on,
                primaryContainer = accent.surface,
                onPrimaryContainer = surface.onSurface,
                secondary = surface.onSurfaceMuted,
                onSecondary = surface.onBackground,
                secondaryContainer = surface.surfaceElevated,
                onSecondaryContainer = surface.onSurface,
                tertiary = accent.primary,
                onTertiary = accent.on,
                tertiaryContainer = accent.surface,
                onTertiaryContainer = surface.onSurface,
                background = surface.background,
                onBackground = surface.onBackground,
                surface = surface.surface,
                onSurface = surface.onSurface,
                surfaceVariant = surface.surfaceElevated,
                onSurfaceVariant = surface.onSurfaceMuted,
                surfaceTint = accent.primary,
                inverseSurface = surface.onSurface,
                inverseOnSurface = surface.surface,
                inversePrimary = accent.primary,
                outline = surface.outline,
                outlineVariant = surface.outline,
                surfaceBright = surface.surfaceElevated,
                surfaceContainer = surface.surfaceElevated,
                surfaceContainerHigh = surface.surfaceElevated,
                surfaceContainerHighest = surface.surfaceElevated,
                surfaceContainerLow = surface.surface,
                surfaceContainerLowest = surface.background,
                surfaceDim = surface.background
            )
        }
        return scheme
    }

    private fun accentGroup(): AccentGroup = when (this) {
        PLUM -> AccentGroup(PulseTokens.Accent.Plum.primary, PulseTokens.Accent.Plum.on, PulseTokens.Accent.Plum.surface, PulseTokens.Accent.Plum.surfaceBorder, PulseTokens.Accent.Plum.tinted)
        NAVY -> AccentGroup(PulseTokens.Accent.Navy.primary, PulseTokens.Accent.Navy.on, PulseTokens.Accent.Navy.surface, PulseTokens.Accent.Navy.surfaceBorder, PulseTokens.Accent.Navy.tinted)
        FUCHSIA -> AccentGroup(PulseTokens.Accent.Fuchsia.primary, PulseTokens.Accent.Fuchsia.on, PulseTokens.Accent.Fuchsia.surface, PulseTokens.Accent.Fuchsia.surfaceBorder, PulseTokens.Accent.Fuchsia.tinted)
        GRAPHITE -> AccentGroup(PulseTokens.Accent.Graphite.primary, PulseTokens.Accent.Graphite.on, PulseTokens.Accent.Graphite.surface, PulseTokens.Accent.Graphite.surfaceBorder, PulseTokens.Accent.Graphite.tinted)
        TEAL -> AccentGroup(PulseTokens.Accent.Teal.primary, PulseTokens.Accent.Teal.on, PulseTokens.Accent.Teal.surface, PulseTokens.Accent.Teal.surfaceBorder, PulseTokens.Accent.Teal.tinted)
        LILAC -> AccentGroup(PulseTokens.Accent.Lilac.primary, PulseTokens.Accent.Lilac.on, PulseTokens.Accent.Lilac.surface, PulseTokens.Accent.Lilac.surfaceBorder, PulseTokens.Accent.Lilac.tinted)
        SKY -> AccentGroup(PulseTokens.Accent.Sky.primary, PulseTokens.Accent.Sky.on, PulseTokens.Accent.Sky.surface, PulseTokens.Accent.Sky.surfaceBorder, PulseTokens.Accent.Sky.tinted)
        SAND -> AccentGroup(PulseTokens.Accent.Sand.primary, PulseTokens.Accent.Sand.on, PulseTokens.Accent.Sand.surface, PulseTokens.Accent.Sand.surfaceBorder, PulseTokens.Accent.Sand.tinted)
        ROSE -> AccentGroup(PulseTokens.Accent.Rose.primary, PulseTokens.Accent.Rose.on, PulseTokens.Accent.Rose.surface, PulseTokens.Accent.Rose.surfaceBorder, PulseTokens.Accent.Rose.tinted)
        AQUA -> AccentGroup(PulseTokens.Accent.Aqua.primary, PulseTokens.Accent.Aqua.on, PulseTokens.Accent.Aqua.surface, PulseTokens.Accent.Aqua.surfaceBorder, PulseTokens.Accent.Aqua.tinted)
    }
}

private data class AccentGroup(
    val primary: Color,
    val on: Color,
    val surface: Color,
    val surfaceBorder: Color,
    val tinted: Color
)
