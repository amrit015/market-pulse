package com.marketlabs.pulse.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.DocumentSectionsScreen
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Settings -> About Market Pulse. Three static copy sections, then an "App info" card (version,
 * build, contact) and links to Privacy Policy / Terms & Conditions as their own tappable cards.
 */
@Composable
fun AboutScreen(
    versionName: String,
    versionCode: String,
    onNavigateUp: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTerms: () -> Unit
) {
    val sections = listOf(
        stringResource(id = R.string.about_section_1_title) to stringResource(id = R.string.about_section_1_body),
        stringResource(id = R.string.about_section_2_title) to stringResource(id = R.string.about_section_2_body),
        stringResource(id = R.string.about_section_3_title) to stringResource(id = R.string.about_section_3_body)
    )

    DocumentSectionsScreen(
        title = stringResource(id = R.string.settings_item_about),
        sections = sections,
        onNavigateUp = onNavigateUp,
        trailingContent = {
            AboutAppInfo(
                versionName = versionName,
                versionCode = versionCode,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                onNavigateToTerms = onNavigateToTerms
            )
        }
    )
}

@Composable
private fun AboutAppInfo(
    versionName: String,
    versionCode: String,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTerms: () -> Unit
) {
    val rowSpacing = dimensionResource(id = R.dimen.padding_medium)

    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Text(
                text = stringResource(id = R.string.about_app_info_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = stringResource(id = R.string.about_app_version, versionName, versionCode),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = stringResource(id = R.string.about_contact_email),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalPulseColors.current.onSurfaceMuted
            )
        }
    }
    Spacer(modifier = Modifier.height(rowSpacing))
    AboutLinkRow(label = stringResource(id = R.string.privacy_policy_screen_title), onClick = onNavigateToPrivacyPolicy)
    Spacer(modifier = Modifier.height(rowSpacing))
    AboutLinkRow(label = stringResource(id = R.string.terms_conditions_screen_title), onClick = onNavigateToTerms)
    Spacer(modifier = Modifier.height(rowSpacing))
}

@Composable
private fun AboutLinkRow(label: String, onClick: () -> Unit) {
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
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = LocalPulseColors.current.accentPrimary
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewAboutScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        AboutScreen(
            versionName = "1.0",
            versionCode = "1",
            onNavigateUp = {},
            onNavigateToPrivacyPolicy = {},
            onNavigateToTerms = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewAboutScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        AboutScreen(
            versionName = "1.0",
            versionCode = "1",
            onNavigateUp = {},
            onNavigateToPrivacyPolicy = {},
            onNavigateToTerms = {}
        )
    }
}
