package com.marketlabs.pulse.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 💡 THOUGHT PROCESS:
 * Full rewrite for the ten-preset theme migration (spec-20260809-theme-migration.md). Replaces
 * the old flat `PulseBlue`/`PulseBlack`/`PulseGold`/`PulseOrange`/`AlertRed`/light+dark surface
 * constants/`PulseStatusColors`/`ColorGreen`/`ColorRed`/`ColorNeutral` entirely — every one of
 * those is gone, not deprecated. This file is now pure DATA: every resolved hex value from the
 * Token Contract (rev 3, Notion `40 — Design System › Token Contract`), organized so a future hex
 * change from Design is a single-file edit here. The logic that assembles these into `PulseColors`
 * / `ColorScheme` per preset lives in `MarketPulseTheme.kt`, not here.
 *
 * Three groups, matching the Token Contract's own layering:
 * - `Signal` — locked, identical across every preset in a given mode. Every dark preset paints
 *   the same 8 dark hexes; every light preset the same 8 light hexes. `unknown` has no resolved
 *   value from Design yet (Session 3 owns it) — placeholder-mapped to the mode's own
 *   `Surface.*.onSurfaceMuted` in `MarketPulseTheme.kt`, not invented here.
 * - `Surface` — the shared ramp, one instance for all 5 light presets, one for all 5 dark presets.
 * - `Accent` — the one thing that actually varies per preset (5 values × 10 presets).
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
            background = Color(0xFFF4F2ED),
            surface = Color(0xFFFBFAF7),
            surfaceElevated = Color(0xFFFFFFFF),
            onBackground = Color(0xFF14161B),
            onSurface = Color(0xFF14161B),
            onSurfaceMuted = Color(0xFF6B6E76),
            outline = Color(0xFFE4E2DC)
        )

        val dark = SurfaceRamp(
            background = Color(0xFF0D0E12),
            surface = Color(0xFF17181D),
            surfaceElevated = Color(0xFF1F2026),
            onBackground = Color(0xFFF0EEF3),
            onSurface = Color(0xFFF0EEF3),
            onSurfaceMuted = Color(0xFF9A9BA3),
            outline = Color(0xFF2A2B31)
        )
    }

    /**
     * Layer 2 — accent group, one per preset. `primary`/`on`/`surface`/`surfaceBorder`/`tinted`
     * map 1:1 to the Token Contract's `accent.primary` / `accent.on` / `accent.surface` /
     * `accent.surfaceBorder` / `surface.tinted` columns.
     */
    object Accent {
        object Plum {
            val primary = Color(0xFF5B2A82)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFEBDFF3)
            val surfaceBorder = Color(0xFFD8C3E4)
            val tinted = Color(0xFFF3EEF6)
        }

        object Navy {
            val primary = Color(0xFF14315E)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFE3E9F3)
            val surfaceBorder = Color(0xFFC7D3E5)
            val tinted = Color(0xFFEDF0F5)
        }

        object Fuchsia {
            val primary = Color(0xFF9C1A6B)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFF5E1EE)
            val surfaceBorder = Color(0xFFECC7DE)
            val tinted = Color(0xFFF5EBF1)
        }

        object Graphite {
            val primary = Color(0xFF2B303A)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFE9E9EB)
            val surfaceBorder = Color(0xFFDDDCD8)
            val tinted = Color(0xFFEFEDEA)
        }

        object Teal {
            val primary = Color(0xFF05555C)
            val on = Color(0xFFFFFFFF)
            val surface = Color(0xFFDDECED)
            val surfaceBorder = Color(0xFFC1DEDE)
            val tinted = Color(0xFFEBF0F0)
        }

        object Lilac {
            val primary = Color(0xFFC7A9FF)
            val on = Color(0xFF1A0F2E)
            val surface = Color(0xFF2C2338)
            val surfaceBorder = Color(0xFF3B2E4B)
            val tinted = Color(0xFF1E1B27)
        }

        object Sky {
            val primary = Color(0xFF7BC0FF)
            val on = Color(0xFF08192E)
            val surface = Color(0xFF1E2A3B)
            val surfaceBorder = Color(0xFF2A3B54)
            val tinted = Color(0xFF171C25)
        }

        object Sand {
            val primary = Color(0xFFC9B49A)
            val on = Color(0xFF1F1A11)
            val surface = Color(0xFF2B261E)
            val surfaceBorder = Color(0xFF3C3325)
            val tinted = Color(0xFF1D1C18)
        }

        object Rose {
            val primary = Color(0xFFE9A2D8)
            val on = Color(0xFF2B0F22)
            val surface = Color(0xFF331F2C)
            val surfaceBorder = Color(0xFF452838)
            val tinted = Color(0xFF1E1A1E)
        }

        object Aqua {
            val primary = Color(0xFF7ED9D6)
            val on = Color(0xFF062120)
            val surface = Color(0xFF1B2E2D)
            val surfaceBorder = Color(0xFF294241)
            val tinted = Color(0xFF141D1D)
        }
    }
}
