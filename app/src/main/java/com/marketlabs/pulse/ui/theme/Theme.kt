package com.marketlabs.pulse.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Define Schemes
private val DarkColorScheme = darkColorScheme(
    primary = MarketBlueLight,
    onPrimary = DarkBackground,
    secondary = NeutralOrange,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF2C2C2C) // Slightly lighter for cards
)

private val LightColorScheme = lightColorScheme(
    primary = MarketBlue,
    onPrimary = LightSurface,
    secondary = NeutralOrange,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFFFFFFF) // White cards on gray background
)

@Composable
fun MarketPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to true if you want wallpaper colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 2. Status Bar Handling (Optional but Recommended)
    // Ensures the status bar icons (battery, time) are visible
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context.findActivity())?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    // 3. Bind it all together
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Defined in Type.kt
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}