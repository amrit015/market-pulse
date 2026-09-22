package com.marketlabs.pulse.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.data.notifications.NotificationPreference
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Settings -> Notifications. Stateless: two opt-in toggles, plus a hint card that appears only when
 * a toggle is on but the system isn't allowing this app to post notifications (permission denied
 * or notifications switched off for the app), linking to the app's notification settings.
 * No `Scaffold`/`TopAppBar`, same bare-back-button chrome as every other Settings sub-screen.
 */
@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    showPermissionHint: Boolean,
    onToggle: (NotificationPreference, Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onNavigateUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        PulseBackTitleRow(
            title = stringResource(id = R.string.settings_item_notifications),
            onNavigateUp = onNavigateUp
        )
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            if (showPermissionHint) {
                PermissionHintCard(onOpenNotificationSettings = onOpenNotificationSettings)
            }
            NotificationToggleRow(
                title = stringResource(id = R.string.notifications_daily_summary_title),
                description = stringResource(id = R.string.notifications_daily_summary_description),
                checked = uiState.dailySummaryEnabled,
                onCheckedChange = { onToggle(NotificationPreference.DAILY_SUMMARY, it) }
            )
            NotificationToggleRow(
                title = stringResource(id = R.string.notifications_stock_analysis_title),
                description = stringResource(id = R.string.notifications_stock_analysis_description),
                checked = uiState.stockAnalysisEnabled,
                onCheckedChange = { onToggle(NotificationPreference.STOCK_ANALYSIS, it) }
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val pulseColors = LocalPulseColors.current
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        // The whole row is the tap target; the Switch itself is display-only so the pair reads as
        // one control to TalkBack.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                // Every state is set from the pulse tokens: the Material defaults for "off" (a
                // pale surface-container track) nearly vanish against the card's tinted background.
                // Off is an outlined track with a muted thumb; on is the accent fill.
                colors = SwitchDefaults.colors(
                    checkedThumbColor = pulseColors.accentOn,
                    checkedTrackColor = pulseColors.accentPrimary,
                    checkedBorderColor = pulseColors.accentPrimary,
                    uncheckedThumbColor = pulseColors.onSurfaceMuted,
                    uncheckedTrackColor = Color.Transparent,
                    uncheckedBorderColor = pulseColors.onSurfaceMuted
                )
            )
        }
    }
}

@Composable
private fun PermissionHintCard(onOpenNotificationSettings: () -> Unit) {
    PulseCard(style = PulseCardStyle.SYNTHESIS, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Text(
                text = stringResource(id = R.string.notifications_permission_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)),
                onClick = onOpenNotificationSettings,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalPulseColors.current.accentPrimary,
                    contentColor = LocalPulseColors.current.accentOn
                )
            ) {
                Text(
                    text = stringResource(id = R.string.notifications_permission_open_settings),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewNotificationsScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        NotificationsScreen(
            uiState = NotificationsUiState(dailySummaryEnabled = true),
            showPermissionHint = true,
            onToggle = { _, _ -> },
            onOpenNotificationSettings = {},
            onNavigateUp = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewNotificationsScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        NotificationsScreen(
            uiState = NotificationsUiState(),
            showPermissionHint = false,
            onToggle = { _, _ -> },
            onOpenNotificationSettings = {},
            onNavigateUp = {}
        )
    }
}
