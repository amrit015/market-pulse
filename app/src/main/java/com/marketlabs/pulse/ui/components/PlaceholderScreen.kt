package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Bare back button + centered "coming soon" text — a real, navigable destination with no real
 * content yet. Used for the Settings rows that are wired up but not designed (Notifications, Data
 * &amp; Sync, About). No `Scaffold`/`TopAppBar`, same as every other screen reached from Settings
 * (see `DocumentSectionsScreen`'s doc comment).
 */
@Composable
fun PlaceholderScreen(
    title: String,
    onNavigateUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
    ) {
        PulseBackTitleRow(title = title, onNavigateUp = onNavigateUp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.placeholder_coming_soon),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalPulseColors.current.onSurfaceMuted
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewPlaceholderScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PlaceholderScreen(title = "Notifications", onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewPlaceholderScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PlaceholderScreen(title = "Notifications", onNavigateUp = {})
    }
}
