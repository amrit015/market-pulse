package com.marketlabs.pulse.ui.screens.legal

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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * One-time, gates entry — final step of onboarding.
 * Stateless; `LegalAcceptanceRoute` owns the checkbox state and the actual `LegalRepository` write.
 */
@Composable
fun LegalAcceptanceScreen(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onAcceptClick: () -> Unit,
    onViewTerms: () -> Unit,
    onViewPrivacyPolicy: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.padding_xxlarge))
        ) {
            // 💡 The whole screen's content -- title, disclosure, checkbox, links -- is one card
            // (this app's card system); only the Accept action sits outside it.
            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Text(
                        text = stringResource(id = R.string.legal_acceptance_screen_title),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                    Text(text = stringResource(id = R.string.legal_acceptance_paragraph_1), style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                    Text(text = stringResource(id = R.string.legal_acceptance_paragraph_2), style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                    Text(text = stringResource(id = R.string.legal_acceptance_paragraph_3), style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                    Text(text = stringResource(id = R.string.legal_acceptance_paragraph_4), style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                    // 💡 The whole row toggles the agreement, not just the box -- `toggleable` on the
                    // row owns the click and the checkbox semantics, so the Checkbox itself is
                    // display-only (`onCheckedChange = null`).
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = isChecked,
                                onValueChange = onCheckedChange,
                                role = Role.Checkbox
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isChecked, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = stringResource(id = R.string.legal_acceptance_checkbox_label),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(onClick = onViewTerms) {
                            Text(text = stringResource(id = R.string.legal_acceptance_view_terms_link))
                        }
                        TextButton(onClick = onViewPrivacyPolicy) {
                            Text(text = stringResource(id = R.string.legal_acceptance_view_privacy_link))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            Button(
                onClick = onAcceptClick,
                enabled = isChecked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.legal_acceptance_accept_button))
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewLegalAcceptanceScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        LegalAcceptanceScreen(
            isChecked = false,
            onCheckedChange = {},
            onAcceptClick = {},
            onViewTerms = {},
            onViewPrivacyPolicy = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewLegalAcceptanceScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        LegalAcceptanceScreen(
            isChecked = true,
            onCheckedChange = {},
            onAcceptClick = {},
            onViewTerms = {},
            onViewPrivacyPolicy = {}
        )
    }
}
