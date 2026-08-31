package com.marketlabs.pulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

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
 * OWN composables should still read `LocalPulseColors.current.accentPrimary` directly, not
 * `colorScheme.primary` — `LocalPulseColors` is the real source of truth for the accent; the
 * `ColorScheme` copy exists purely for framework components that only know how to read M3 roles.
 *
 * `secondary`/`secondaryContainer` have no independently-defined hex — resolved here as
 * `secondary = onSurfaceMuted` / `secondaryContainer = surfaceElevated` (existing ramp tokens, not
 * new invented values) since call sites across the app already use `secondary` for muted
 * meta-text/labels and `secondaryContainer.copy(alpha=…)` for a subtle card-background wash — both
 * read correctly against these.
 *
 * `error`/`errorContainer`/`onError`/`onErrorContainer` are intentionally left OUT of the
 * `ColorScheme` builder calls below — there is no danger/error hex defined anywhere in this
 * theming system (the closest thing, the old flat `AlertRed` constant, is deleted along with the
 * rest of the single-theme palette). Rather than invent a value, M3's own
 * `lightColorScheme()`/`darkColorScheme()` baseline default takes over for these four roles —
 * same reasoning as leaving `signal.unknown` as an explicit placeholder rather than a guess.
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
        val signal = if (isDark) PulseTokens.Signal.dark else PulseTokens.Signal.light
        val surface = if (isDark) PulseTokens.Surface.dark else PulseTokens.Surface.light

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
            // 💡 AI/News cards start from the same `tinted` base as price cards below (so both
            // card types read as the same underlying color family) but blend 55% toward
            // `surfaceBorder` instead of `surface` -- `surfaceBorder` is the stronger, more
            // saturated anchor of the two (it's already one step further from `tinted` than
            // `surface` is, in the same direction), and the higher blend factor pushes further
            // still. That combination lands this reliably darker and more saturated than
            // `surfaceTinted` below in every preset, in both modes, without hand-picking a
            // separate hex per preset.
            //
            // 💡 2026-08-29, dark mode: darkened one step further by blending 12% toward plain black
            // on top of the above. `surfaceBorder` isn't reliably darker than `tinted` in every
            // preset -- for several dark presets it's actually the lighter of the two (a more
            // visible accent tone against a near-black `tinted`), so pushing further toward
            // `surfaceBorder` alone would have made AI cards lighter in dark mode, not darker.
            // Blending toward black instead guarantees "darker," independent of which direction
            // `surfaceBorder` happens to sit relative to `tinted` for a given preset. Left exactly
            // as it landed through every later light-mode pass below -- dark mode was asked to stay
            // untouched each time.
            //
            // 💡 2026-08-29, light mode: went through a darkening pass (6% toward black, mirroring
            // dark mode's approach) and then a lightening one -- the darkened version read too heavy
            // against the light page once the card shadow itself got stronger. Rather than just
            // easing the black blend back down, this switched approach entirely: white blended with
            // a moderate amount of this preset's own `accent.primary`, the same "light, tuned to the
            // brand color" technique `toColorScheme()`'s page-background blend already uses (see
            // that function), just at a stronger 15% (vs. the page background's 5%) so an AI card
            // still reads as a distinctly-colored surface sitting on top of that faintly-tinted page,
            // not the same tint at the same strength.
            accentSurfaceStrong = if (isDark) {
                lerp(lerp(accent.tinted, accent.surfaceBorder, 0.55f), Color.Black, 0.12f)
            } else {
                lerp(Color.White, accent.primary, 0.15f)
            },
            // 💡 Blended 45% from the literal Token Contract `tinted` value toward `surface` --
            // the raw `tinted` value alone reads as too close to the page background to register
            // as a distinct card (35% was an earlier attempt, still a bit too subtle once judged
            // against the finished app rather than in isolation). Computed here (not baked into
            // the `tinted` constant) so this factor is a one-line change the next time it needs
            // tuning -- and it only needs tuning in this one spot, since every DATA-style card
            // (Equities' AssetCard, Indicators' UniversalMetricCard) reads it through the same
            // `PulseCard` component.
            surfaceTinted = lerp(accent.tinted, accent.surface, 0.45f),
            onSurfaceMuted = surface.onSurfaceMuted,
            // 💡 2026-08-25: was signal.bullishText/bearishText in every mode (committed), then
            // briefly signal.bullishPill/bearishPill in every mode (an in-progress edit to fix how
            // dark mode looked) -- the pill tone reads well as a dark-mode tile fill but washed out
            // in light mode, so this picks a different source per mode instead of one token family
            // for both. See PulseColors' own doc comment on these two fields.
            sectorHeatmapBullish = if (isDark) signal.bullishPill else signal.bullishText,
            sectorHeatmapBearish = if (isDark) signal.bearishPill else signal.bearishText
        )
    }

    fun toColorScheme(): ColorScheme {
        val accent = accentGroup()
        val surface = if (isDark) PulseTokens.Surface.dark else PulseTokens.Surface.light

        // 💡 2026-08-29: light mode's page background is `surface.background` (pure white, see the
        // field's own doc comment in `Color.kt`) blended with a small hint of this preset's own
        // `accent.primary` -- a page that reads as "white, tinted faintly toward the app's brand
        // color" instead of one flat neutral shared by every preset. Dark mode is untouched --
        // `surface.background` passes straight through, same as before this pass. Computed once
        // here and reused everywhere `surface.background` used to be read directly below
        // (`background`, `surfaceContainerLowest`, `surfaceDim` -- all three are the same page-level
        // role, just different M3 slots for it) so they can't drift apart from each other.
        val background = if (isDark) surface.background else lerp(surface.background, accent.primary, 0.05f)

        // 💡 error/onError/errorContainer/onErrorContainer are deliberately NOT passed below —
        // omitting them lets lightColorScheme()/darkColorScheme()'s own default parameter values
        // (M3's baseline error palette, already correct per-mode) apply, rather than hand-picking
        // a value this theming system has no hex for at all. See the file header comment above.
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
                background = background,
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
                surfaceContainerLowest = background,
                surfaceDim = background
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
                background = background,
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
                surfaceContainerLowest = background,
                surfaceDim = background
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
