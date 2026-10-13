package com.marketlabs.pulse.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.pm.PackageInfoCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R

/**
 * Reads the installed app's version name/code from `PackageInfo` (never hardcoded) and hands them
 * to the stateless [AboutScreen], along with the anonymous App ID and a copy action for it.
 */
@Composable
fun AboutRoute(
    onNavigateUp: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val appId by viewModel.appId.collectAsStateWithLifecycle()
    val appIdLabel = stringResource(id = R.string.about_app_id_label)
    val appIdCopiedMessage = stringResource(id = R.string.about_app_id_copied)
    val (versionName, versionCode) = remember(context) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            (info.versionName ?: "") to PackageInfoCompat.getLongVersionCode(info).toString()
        }.getOrDefault("" to "")
    }

    AboutScreen(
        versionName = versionName,
        versionCode = versionCode,
        onNavigateUp = onNavigateUp,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        onNavigateToTerms = onNavigateToTerms,
        appId = appId,
        onCopyAppId = { id ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(appIdLabel, id))
            // Android 13+ shows its own "copied" confirmation; earlier versions show nothing.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(context, appIdCopiedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    )
}
