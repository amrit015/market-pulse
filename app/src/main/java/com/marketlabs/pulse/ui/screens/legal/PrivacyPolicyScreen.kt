package com.marketlabs.pulse.ui.screens.legal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.data.legal.LegalDocumentProvider
import com.marketlabs.pulse.data.legal.toBodyText
import com.marketlabs.pulse.ui.components.DocumentSectionsScreen
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Boilerplate — NOT LEGAL ADVICE, needs
 * real legal review before shipping. Assumes Firebase Analytics, Crashlytics, Firestore,
 * Authentication (anonymous only), and App Check (Play Integrity) are the active Firebase
 * products, plus Google AdMob for ads (all are dependencies of this app and all are disclosed in
 * the Third-Party Services section). The text itself lives in `assets/legal_privacy.json`, which
 * is also the copy the public web page serves.
 */
@Composable
fun PrivacyPolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val document = remember { LegalDocumentProvider.privacy(context) }
    val sections = document.sections.map { it.heading to it.toBodyText() }

    DocumentSectionsScreen(
        title = stringResource(id = R.string.privacy_policy_screen_title),
        lastUpdatedLabel = legalLastUpdatedLabel(document.lastUpdated),
        sections = sections,
        onNavigateUp = onNavigateUp
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewPrivacyPolicyScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PrivacyPolicyScreen(onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewPrivacyPolicyScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PrivacyPolicyScreen(onNavigateUp = {})
    }
}
