package com.marketlabs.pulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Defines the centralized color palette for MarketLabs Pulse.
 * Action: Use generic naming to maintain brand identity while avoiding IP risks.
 */

/** Signature Branding Colors */
val PulseBlack = Color(0xFF000000)
val PulseBlue = Color (0xFF083B95)

/** Dark Mode Surface Colors */
val DarkSurface = Color(0xFF121212)
val DarkOnSurface = Color(0xFFEFEFEF)
val PulseDeepGray = Color(0xFF2C2C2C)

/** Light Mode Surface Colors */
val LightBackground = Color(0xFFF5F5F5)
val LightSurface = Color(0xFFFAFAFA)

/** Alert and Error Colors */
val AlertRed = Color(0xFFC62828)

/** Functional Status Colors */
val PulseOrange = Color(0xFFEF6C00) // Material Orange 800 (Deep, intense orange)
val PulseGold = Color(0xFFF9A825)   // Material Yellow 800 (Rich mustard/gold)

/**
 * 💡 NEW: Centralized Dynamic Status Colors
 * These automatically handle Light/Dark mode transitions for perfect contrast.
 */
/**
 * Centralized Dynamic Status Colors
 * These automatically handle Light/Dark mode transitions for perfect contrast.
 */
/**
 * Centralized Dynamic Status Colors
 * These automatically handle Light/Dark mode transitions for perfect contrast.
 */
object PulseStatusColors {
    // --- TEXT & ICON COLORS ---
    val BullishText: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
    val BearishText: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF37B7B) else Color(0xFFC62828)

    // 💡 TWEAKED: Deep Marigold / Amber (A rich, readable gold)
    val NeutralText: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFFD54F) else Color(0xFFD87B00)

    // 💡 TWEAKED: Deep Burnt Orange (Distinct from the gold, but still very vibrant)
    val WarningText: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE65100) else Color(0xFFCC4A00)

    // --- BACKGROUND CARD COLORS ---
    val BullishBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1B5E20).copy(alpha = 0.25f) else Color(0xFFBADCBE)
    val BearishBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFB71C1C).copy(alpha = 0.25f) else Color(0xFFF3D8DB)

    // 💡 TWEAKED: Soft Golden Cream (Amber 50)
    val NeutralBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF57F17).copy(alpha = 0.15f) else Color(
            0xFFF5E3CD
        )

    // 💡 TWEAKED: Soft Warm Peach (Deep Orange 50)
    val WarningBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE65100).copy(alpha = 0.15f) else Color(0xFFF8DFD3)
}

val ColorGreen = Color(0xFF2E7D32)
val ColorRed = Color(0xFFC62828)
val ColorNeutral = Color(0xFFCE5A03)
