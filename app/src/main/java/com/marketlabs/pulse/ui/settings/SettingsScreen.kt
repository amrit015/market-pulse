package com.marketlabs.pulse.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.PulseColors

/**
 * Stateless — plain data + lambdas, no ViewModel awareness, per this repo convention. Owns its own
 * `Scaffold`/`TopAppBar` with a back button, unlike every screen reached FROM here (Theme picker,
 * Tutorials, the More rows) -- this is the hub itself, so it keeps its chrome; see
 * `DocumentSectionsScreen`'s doc comment for why its own destinations don't.
 *
 * The Theme section is a compact row previewing the currently-selected preset; tapping it opens
 * the dedicated `ThemePickerScreen` (2 sections, Light/Dark). Every row -- Theme and each "More"
 * item -- is its own `PulseCard`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateToThemePicker: () -> Unit,
    onNavigateUp: () -> Unit,
    onMoreItemClick: (SettingsMoreItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = stringResource(id = R.string.nav_back_content_description)
                        )
                    }
                },
                // 💡 Matches the screen's background (and the same override every other pushed
                // destination's TopAppBar uses -- see NewsRoute.kt/AssetDetailRoute.kt) so the bar
                // doesn't render as a visibly different-colored band above the content. Left on the
                // default `colorScheme.surface` before, which reads noticeably different from
                // `colorScheme.background` now that light mode's background carries a per-preset
                // accent tint (`MarketPulseTheme.kt`'s `toColorScheme()`) that `surface` doesn't.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            // 💡 No "Theme"/"More" section labels -- just one flat stack of individual cards, the
            // Theme card first.
            val rowSpacing = dimensionResource(id = R.dimen.padding_medium)
            Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
                ThemePreviewRow(selectedTheme = uiState.selectedTheme, onClick = onNavigateToThemePicker)
                SettingsMoreRow(
                    label = stringResource(id = R.string.settings_item_notifications),
                    onClick = { onMoreItemClick(SettingsMoreItem.NOTIFICATIONS) }
                )
                SettingsMoreRow(
                    label = stringResource(id = R.string.settings_item_tutorials),
                    onClick = { onMoreItemClick(SettingsMoreItem.TUTORIALS) }
                )
                SettingsMoreRow(
                    label = stringResource(id = R.string.terms_conditions_screen_title),
                    onClick = { onMoreItemClick(SettingsMoreItem.TERMS_CONDITIONS) }
                )
                SettingsMoreRow(
                    label = stringResource(id = R.string.privacy_policy_screen_title),
                    onClick = { onMoreItemClick(SettingsMoreItem.PRIVACY_POLICY) }
                )
                SettingsMoreRow(
                    label = stringResource(id = R.string.settings_item_data_sync),
                    onClick = { onMoreItemClick(SettingsMoreItem.DATA_SYNC) }
                )
                SettingsMoreRow(
                    label = stringResource(id = R.string.settings_item_about),
                    onClick = { onMoreItemClick(SettingsMoreItem.ABOUT) }
                )
                SettingsMoreRow(
                    label = stringResource(id = R.string.settings_item_share),
                    onClick = { onMoreItemClick(SettingsMoreItem.SHARE) },
                    trailingIconRes = R.drawable.ic_share
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
        }
    }
}

/**
 * All six "More" rows are real, navigable destinations -- Data & Sync and About are static content
 * screens; Notifications is still a placeholder (content/design to follow separately). Share isn't
 * a destination: it opens the system share sheet. Terms & Conditions and Privacy Policy are their
 * own top-level rows (no intermediate "Legal" hub screen) linking to the same two documents shown
 * once during onboarding; Tutorials is its own screen.
 */
enum class SettingsMoreItem { NOTIFICATIONS, TUTORIALS, TERMS_CONDITIONS, PRIVACY_POLICY, DATA_SYNC, ABOUT, SHARE }

@Composable
private fun SettingsMoreRow(
    label: String,
    onClick: () -> Unit,
    @DrawableRes trailingIconRes: Int = R.drawable.ic_chevron_forward
) {
    PulseCard(style = PulseCardStyle.DATA, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(id = trailingIconRes),
                contentDescription = null,
                tint = LocalPulseColors.current.accentPrimary
            )
        }
    }
}

/**
 * The Theme row on Settings -- one row: "Theme" (fixed label, `onSurface`) immediately followed by
 * the current preset's name/mode (`onSurfaceVariant`, reads as the label's value, same "label then
 * value" shape every other settings row implies), then the same mini "Technical Briefing" preview
 * panel `PresetSwatchCard` shows on the full picker screen sits at the row's end, right before the
 * chevron. Reads `LocalPulseColors.current` directly (not `preset.toPulseColors()` the way
 * `PresetSwatchCard` does for presets OTHER than the active one) since the app is already rendering
 * in this exact preset right now, so the live theme IS the accurate preview -- no separate
 * resolution needed. Tapping anywhere opens the full picker (`ThemePickerScreen`).
 */
@Composable
private fun ThemePreviewRow(selectedTheme: MarketPulseTheme, onClick: () -> Unit) {
    val pulseColors = LocalPulseColors.current
    val modeLabel = if (selectedTheme.isDark) {
        stringResource(id = R.string.settings_preset_mode_dark)
    } else {
        stringResource(id = R.string.settings_preset_mode_light)
    }

    PulseCard(style = PulseCardStyle.DATA, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.settings_section_theme),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_standard)))
                Text(
                    text = "${selectedTheme.displayName} · $modeLabel",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ThemeTechnicalBriefingPreview(pulseColors = pulseColors)
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = null,
                    tint = pulseColors.accentPrimary
                )
            }
        }
    }
}

/** Same mini preview panel [PresetSwatchCard] shows for each preset on the full picker screen --
 * "Technical Briefing" label + sample Bullish/Bearish pills -- just reading the live current theme
 * instead of a specific (possibly inactive) preset's resolved colors. */
@Composable
private fun ThemeTechnicalBriefingPreview(pulseColors: PulseColors) {
    Surface(
        color = pulseColors.accentSurface,
        border = BorderStroke(dimensionResource(id = R.dimen.border_thin), pulseColors.accentSurfaceBorder),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))) {
            Text(
                text = stringResource(id = R.string.dashboard_technical_briefing),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = pulseColors.accentPrimary
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_tiny))) {
                ThemePreviewPill(
                    text = stringResource(id = R.string.settings_preset_sample_bullish),
                    textColor = pulseColors.signalBullishText,
                    pillColor = pulseColors.signalBullishPill
                )
                ThemePreviewPill(
                    text = stringResource(id = R.string.settings_preset_sample_bearish),
                    textColor = pulseColors.signalBearishText,
                    pillColor = pulseColors.signalBearishPill
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewPill(text: String, textColor: Color, pillColor: Color) {
    Surface(color = pillColor, shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.padding_small),
                vertical = dimensionResource(id = R.dimen.padding_tiny)
            )
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewSettingsScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SettingsScreen(
            uiState = SettingsUiState(selectedTheme = MarketPulseTheme.NAVY),
            onNavigateToThemePicker = {},
            onNavigateUp = {},
            onMoreItemClick = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewSettingsScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        SettingsScreen(
            uiState = SettingsUiState(selectedTheme = MarketPulseTheme.LILAC),
            onNavigateToThemePicker = {},
            onNavigateUp = {},
            onMoreItemClick = {}
        )
    }
}
