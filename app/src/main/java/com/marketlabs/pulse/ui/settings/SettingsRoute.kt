package com.marketlabs.pulse.ui.settings

import androidx.compose.runtime.Composable
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R

/**
 * Stateful — `hiltViewModel()`, `collectAsStateWithLifecycle()`, per this repo convention.
 *
 * The "More" rows are real, navigable destinations (Notifications/Data & Sync/About are
 * placeholder screens; Terms & Conditions/Privacy Policy/Tutorials are their own screens) --
 * each gets its own nav callback, wired to a real route in `PulseNavGraph`, rather than a
 * Toast-only stub.
 */
@Composable
fun SettingsRoute(
    onNavigateUp: () -> Unit,
    onNavigateToThemePicker: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDataSync: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTutorials: () -> Unit,
    onNavigateToComingUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SettingsScreen(
        uiState = uiState,
        onNavigateToThemePicker = onNavigateToThemePicker,
        onNavigateUp = onNavigateUp,
        onMoreItemClick = { item ->
            when (item) {
                SettingsMoreItem.NOTIFICATIONS -> onNavigateToNotifications()
                SettingsMoreItem.DATA_SYNC -> onNavigateToDataSync()
                SettingsMoreItem.ABOUT -> onNavigateToAbout()
                SettingsMoreItem.TERMS_CONDITIONS -> onNavigateToTerms()
                SettingsMoreItem.PRIVACY_POLICY -> onNavigateToPrivacyPolicy()
                SettingsMoreItem.TUTORIALS -> onNavigateToTutorials()
                SettingsMoreItem.COMING_UP -> onNavigateToComingUp()
                SettingsMoreItem.SHARE -> shareApp(context)
            }
        }
    )
}

/** Opens the system share sheet with a short message and this app's Play Store link. */
private fun shareApp(context: Context) {
    val storeUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_message, storeUrl))
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.share_app_chooser_title)))
}
