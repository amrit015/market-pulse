package com.marketlabs.pulse.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 💡 THOUGHT PROCESS:
 * This app supports ten color presets (5 light, 5 dark), replacing the old flat single-theme
 * palette (`PulseBlue`/`PulseBlack`/`PulseGold`/`PulseOrange`/`AlertRed`/light+dark surface
 * constants/`PulseStatusColors`/`ColorGreen`/`ColorRed`/`ColorNeutral`) entirely — every one of
 * those is gone, not deprecated. This file is now pure DATA: every resolved hex value the presets
 * use, organized so a future hex change is a single-file edit here. The logic that assembles these
 * into `PulseColors`/`ColorScheme` per preset lives in `MarketPulseTheme.kt`, not here.
 *
 * Three groups:
 * - `Signal` — locked, identical across every preset in a given mode. Bullish/bearish/neutral/
 *   warning meanings must stay recognizable regardless of which preset is active, so every dark
 *   preset paints the same 8 dark hexes and every light preset the same 8 light hexes; nothing in
 *   `Accent` is ever allowed to override them. `unknown` (the fourth signal state, for missing
 *   data) has no resolved hex yet — placeholder-mapped to the mode's own `Surface.*.onSurfaceMuted`
 *   in `MarketPulseTheme.kt` rather than invented here; see the `TODO` next to it below.
 * - `Surface` — the shared ramp (background/surface/outline/etc.), one instance for all 5 light
 *   presets, one for all 5 dark presets.
 * - `Accent` — the one thing that actually varies per preset (5 values × 10 presets): the brand
 *   color a preset paints itself with, kept deliberately out of the signal hues above so a themed
 *   accent can never be mistaken for a bullish/bearish/warning read.
 */
/**
 * `SignalPalette`/`SurfaceRamp` back `PulseTokens.Signal`/`.Surface` below with actual data-class
 * instances (`light`/`dark` vals of ONE shared type) rather than two separate anonymous `object`
 * declarations. That distinction matters beyond style: `MarketPulseTheme.kt` picks between the two
 * with `if (isDark) ... else ...`, and Kotlin can only infer a meaningful common type for that
 * expression when both branches share a real type — two unrelated `object`s would only unify to
 * `Any`, which has none of the properties being read off the result.
 */
data class SignalPalette(
    val bullishText: Color,
    val bearishText: Color,
    val neutralText: Color,
    val warningText: Color,
    val bullishPill: Color,
    val bearishPill: Color,
    val neutralPill: Color,
    val warningPill: Color
)

data class SurfaceRamp(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val outline: Color
)

object PulseTokens {

    /** Layer 1 — signal tokens. LOCKED. Never restyled by any preset. */
    object Signal {
        val light = SignalPalette(
            bullishText = Color(0xFF1B5E20),
            bearishText = Color(0xFFB71C1C),
            neutralText = Color(0xFFB36A00),
            warningText = Color(0xFFBF360C),
            bullishPill = Color(0xFFC6E4C1),
            bearishPill = Color(0xFFF8C8BE),
            neutralPill = Color(0xFFF0DEB0),
            warningPill = Color(0xFFF5CFB8)
        )

        val dark = SignalPalette(
            bullishText = Color(0xFF81C784),
            bearishText = Color(0xFFF37B7B),
            neutralText = Color(0xFFFFD54F),
            warningText = Color(0xFFE65100),
            bullishPill = Color(0xFF1F4A28),
            bearishPill = Color(0xFF4A2222),
            neutralPill = Color(0xFF4A3820),
            warningPill = Color(0xFF4A2210)
        )

        // 💡 signal.unknown has no resolved value from Design — Session 3 owns it (data-missing
        // state, surfaces most on Indicators). Do NOT invent a hex here; MarketPulseTheme.kt
        // wires this to the mode's own onSurfaceMuted as an explicit placeholder instead.
    }

    /** Layer 2 — surface ramp. Shared per mode: one instance for all 5 light presets, one for all 5 dark. */
    object Surface {
        val light = SurfaceRamp(
            // 💡 Blended halfway toward `surface` below -- the literal Token Contract value read as
            // more visibly grey/cream than wanted once the top bar started sharing this exact color
            // (AppTopBar.kt), a greyish-white was asked for instead. Dark mode's background is
            // untouched -- "more white" only applies to light mode.
            background = Color(0xFFF8F6F2),
            surface = Color(0xFFFBFAF7),
            surfaceElevated = Color(0xFFFFFFFF),
            onBackground = Color(0xFF14161B),
            onSurface = Color(0xFF14161B),
            // 💡 Darkened from the literal Token Contract value (was 0xFF6B6E76) -- this is the one
            // muted-text color every preset in both modes shares (dates, "52W LOW"-style stat
            // labels, subtitles -- 130+ call sites via `onSurfaceMuted`/`colorScheme.onSurfaceVariant`,
            // both of which read this same value), and it read too light/washed-out against light
            // backgrounds. Converted to HLS, lightness dropped ~0.08, hue/saturation untouched --
            // same technique already used for the `tinted` desaturation elsewhere in this file.
            onSurfaceMuted = Color(0xFF585A61),
            outline = Color(0xFFE4E2DC)
        )

        val dark = SurfaceRamp(
            background = Color(0xFF0D0E12),
            surface = Color(0xFF17181D),
            surfaceElevated = Color(0xFF1F2026),
            onBackground = Color(0xFFF0EEF3),
            onSurface = Color(0xFFF0EEF3),
            // 💡 Lightened from the literal Token Contract value (was 0xFF9A9BA3) -- same reasoning
            // as light mode's `onSurfaceMuted` above, mirrored: too dark/washed-out against dark
            // backgrounds. Lightness raised ~0.08 via the same HLS method.
            onSurfaceMuted = Color(0xFFAFB0B6),
            outline = Color(0xFF2A2B31)
        )
    }

    /**
     * Layer 2 — accent group, one per preset: the brand color (`primary`), the color text/icons
     * use when sitting on top of it (`on`), a soft tint for synthesis-layer cards like AI
     * briefings and news (`surface`) with a matching hairline border (`surfaceBorder`), and an
     * accent-washed neutral used for price-card backgrounds regardless of bullish/bearish
     * direction (`tinted`).
     */
    object Accent {
        object Plum {
            val primary = Color(0xFF5B2A82)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFEBDFF3)
            val surfaceBorder = Color(0xFFD8C3E4)
            // 💡 Grey-desaturated from the literal Token Contract value (was 0xFFF3EEF6) -- text
            // sitting on price cards read with too little contrast against the full-saturation
            // tint, across every preset in both modes. Converted to HSL, saturation halved, hue
            // and lightness left untouched, converted back -- keeps each preset's accent hue
            // faintly recognizable while pushing it toward neutral grey. Price cards need a
            // stronger version of this for their actual background -- that adjustment happens as a
            // runtime blend toward `surface` in `MarketPulseTheme.toPulseColors()` rather than
            // baked into this constant, specifically so the blend factor is a one-line tweak
            // instead of hand-recomputing ten hex values every time it needs to change (which
            // happened three times across two sessions before this was worth doing properly).
            val tinted = Color(0xFFF2F0F4)
        }

        object Navy {
            val primary = Color(0xFF14315E)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFE3E9F3)
            val surfaceBorder = Color(0xFFC7D3E5)
            val tinted = Color(0xFFEFF0F3)
        }

        object Fuchsia {
            val primary = Color(0xFF9C1A6B)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFF5E1EE)
            val surfaceBorder = Color(0xFFECC7DE)
            val tinted = Color(0xFFF3EDF0)
        }

        object Graphite {
            val primary = Color(0xFF2B303A)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFE9E9EB)
            val surfaceBorder = Color(0xFFDDDCD8)
            val tinted = Color(0xFFEEEDEB)
        }

        object Teal {
            val primary = Color(0xFF05555C)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFDDECED)
            val surfaceBorder = Color(0xFFC1DEDE)
            val tinted = Color(0xFFECEFEF)
        }

        object Lilac {
            val primary = Color(0xFFC7A9FF)
            val on = Color(0xFF1A0F2E)
            val surface = Color(0xFF2C2338)
            val surfaceBorder = Color(0xFF3B2E4B)
            val tinted = Color(0xFF201E24)
        }

        object Sky {
            val primary = Color(0xFF7BC0FF)
            val on = Color(0xFF08192E)
            val surface = Color(0xFF1E2A3B)
            val surfaceBorder = Color(0xFF2A3B54)
            val tinted = Color(0xFF1A1D22)
        }

        object Sand {
            val primary = Color(0xFFC9B49A)
            val on = Color(0xFF1F1A11)
            val surface = Color(0xFF2B261E)
            val surfaceBorder = Color(0xFF3C3325)
            val tinted = Color(0xFF1C1B19)
        }

        object Rose {
            val primary = Color(0xFFE9A2D8)
            val on = Color(0xFF2B0F22)
            val surface = Color(0xFF331F2C)
            val surfaceBorder = Color(0xFF452838)
            val tinted = Color(0xFF1D1B1D)
        }

        object Aqua {
            val primary = Color(0xFF7ED9D6)
            val on = Color(0xFF062120)
            val surface = Color(0xFF1B2E2D)
            val surfaceBorder = Color(0xFF294241)
            val tinted = Color(0xFF161B1B)
        }
    }
}
