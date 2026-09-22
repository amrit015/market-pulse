package com.marketlabs.pulse.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.DocumentSectionsScreen
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/** Settings -> Data & Sync. Static copy only: what is and isn't collected, and where data lives. */
@Composable
fun DataSyncScreen(onNavigateUp: () -> Unit) {
    val sections = listOf(
        stringResource(id = R.string.data_sync_section_1_title) to stringResource(id = R.string.data_sync_section_1_body),
        stringResource(id = R.string.data_sync_section_2_title) to stringResource(id = R.string.data_sync_section_2_body),
        stringResource(id = R.string.data_sync_section_3_title) to stringResource(id = R.string.data_sync_section_3_body),
        stringResource(id = R.string.data_sync_section_4_title) to stringResource(id = R.string.data_sync_section_4_body)
    )

    DocumentSectionsScreen(
        title = stringResource(id = R.string.settings_item_data_sync),
        sections = sections,
        onNavigateUp = onNavigateUp
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewDataSyncScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DataSyncScreen(onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewDataSyncScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        DataSyncScreen(onNavigateUp = {})
    }
}
