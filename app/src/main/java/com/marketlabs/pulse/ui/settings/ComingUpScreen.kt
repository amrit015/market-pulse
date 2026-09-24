package com.marketlabs.pulse.ui.settings

import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.ComingSoonBannerCard
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Settings -> Coming Up: the same short announcement card the Analysis screen shows (without a
 * dismiss affordance -- a reader who navigated here on purpose already knows why it's here), plus
 * a detail card with a bit more on what's changing. The one place this stays visible after the
 * Analysis banner has been dismissed for good.
 */
@Composable
fun ComingUpScreen(onNavigateUp: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        PulseBackTitleRow(title = stringResource(id = R.string.settings_item_coming_up), onNavigateUp = onNavigateUp)
        // 💡 The banner card supplies its own horizontal/vertical margin (see
        // `ComingSoonBannerCard`'s doc comment), so it sits directly here rather than inside the
        // padded Column below -- nesting it in there too would double its side margins.
        ComingSoonBannerCard(
            title = stringResource(id = R.string.coming_soon_custom_list_title),
            body = stringResource(id = R.string.coming_soon_custom_list_body)
        )
        Column(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_large))) {
            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Text(
                        text = stringResource(id = R.string.coming_up_detail_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                    Text(
                        text = stringResource(id = R.string.coming_up_detail_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewComingUpScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        ComingUpScreen(onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewComingUpScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        ComingUpScreen(onNavigateUp = {})
    }
}
