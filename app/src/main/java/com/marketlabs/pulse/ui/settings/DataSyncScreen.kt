package com.marketlabs.pulse.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.DocumentSectionsScreen
import com.marketlabs.pulse.ui.settings.components.SettingsToggleRow
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Settings -> Data & Sync. Stateless: the "Share anonymous usage data" switch (with a link to the
 * Privacy Policy), then static copy on what is and isn't collected and where data lives.
 */
@Composable
fun DataSyncScreen(
    uiState: DataSyncUiState,
    onAnalyticsEnabledChange: (Boolean) -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val sections = listOf(
        stringResource(id = R.string.data_sync_section_1_title) to stringResource(id = R.string.data_sync_section_1_body),
        stringResource(id = R.string.data_sync_section_2_title) to stringResource(id = R.string.data_sync_section_2_body),
        stringResource(id = R.string.data_sync_section_3_title) to stringResource(id = R.string.data_sync_section_3_body),
        stringResource(id = R.string.data_sync_section_4_title) to stringResource(id = R.string.data_sync_section_4_body),
        stringResource(id = R.string.data_sync_section_5_title) to stringResource(id = R.string.data_sync_section_5_body)
    )

    DocumentSectionsScreen(
        title = stringResource(id = R.string.settings_item_data_sync),
        sections = sections,
        onNavigateUp = onNavigateUp,
        leadingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                SettingsToggleRow(
                    title = stringResource(id = R.string.data_sync_share_usage_title),
                    description = stringResource(id = R.string.data_sync_share_usage_description),
                    checked = uiState.analyticsEnabled,
                    onCheckedChange = onAnalyticsEnabledChange
                )
                TextButton(onClick = onNavigateToPrivacyPolicy) {
                    Text(text = stringResource(id = R.string.data_sync_share_usage_privacy_link))
                }
            }
        }
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewDataSyncScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DataSyncScreen(
            uiState = DataSyncUiState(),
            onAnalyticsEnabledChange = {},
            onNavigateToPrivacyPolicy = {},
            onNavigateUp = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewDataSyncScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        DataSyncScreen(
            uiState = DataSyncUiState(),
            onAnalyticsEnabledChange = {},
            onNavigateToPrivacyPolicy = {},
            onNavigateUp = {}
        )
    }
}
