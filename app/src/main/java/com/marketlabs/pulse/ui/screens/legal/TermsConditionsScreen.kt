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
 * Boilerplate based on common patterns
 * for financial-data apps — NOT LEGAL ADVICE, needs real legal review before shipping. The text lives in
 * `assets/legal_terms.json`, which is also the copy the public web page serves; its `[TODO: ...]`
 * markers stand in for entity name / jurisdiction not yet supplied.
 */
@Composable
fun TermsConditionsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val document = remember { LegalDocumentProvider.terms(context) }
    val sections = document.sections.map { it.heading to it.toBodyText() }

    DocumentSectionsScreen(
        title = stringResource(id = R.string.terms_conditions_screen_title),
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
private fun PreviewTermsConditionsScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TermsConditionsScreen(onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewTermsConditionsScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TermsConditionsScreen(onNavigateUp = {})
    }
}
