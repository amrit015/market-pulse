package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * One persistent, universal string --
 * deliberately generic since it has to be true on both DATA and SYNTHESIS screens alike. NOT shared
 * chrome in `MainActivity`'s `Scaffold` -- each screen appends this as the last item in its OWN
 * scrollable content (the bottom of a `LazyColumn`/`Column`), so it naturally covers pushed
 * destinations (News, Stock Detail, Glossary Detail, Indicator Horizons, etc.) the same way tab
 * screens do, without any shared-chrome wiring. Excluded per-screen at the call site, not centrally
 * -- omitted from Settings, Legal (Terms & Conditions/Privacy Policy), and onboarding/acceptance,
 * every other screen includes it.
 *
 * Plain text, no background fill or shadow -- a shadow here only ever drew as a visible rectangle
 * around the text's bounds (most noticeably in light mode), and since the footer is inline content
 * at the end of a screen rather than an overlay above scrolling content, nothing needs it to stand
 * out from what's behind it.
 *
 * No `windowInsetsPadding` of its own -- as inline scrollable content now (not bottom-of-screen
 * chrome), it doesn't need to individually clear the system navigation bar; each screen's own
 * `scaffoldPadding`/`Scaffold` already accounts for that.
 *
 * [showAiDisclosure]: Summary and Analysis are the two screens where nearly every card is
 * AI-authored -- repeating a per-card "AI-generated · Not advice" label on each one would be pure
 * noise there, so those cards carry none at all (see `SynthesisHeroCard`/`MarketSentimentCard`/
 * `StockPreviewCard` etc.) and this screen-level footer becomes their one AI disclosure instead:
 * the same "AI-generated · Not advice" text as its own first line, directly above the existing
 * "For informational purposes..." line -- just bold to read as the disclosure rather than the finer
 * print underneath it. Every other screen
 * keeps the plain single-line footer (`showAiDisclosure = false`, the default) since their AI cards
 * already disclose themselves individually.
 */
@Composable
fun DisclaimerFooter(modifier: Modifier = Modifier, showAiDisclosure: Boolean = false) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.padding_tiny)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showAiDisclosure) {
                Text(
                    text = stringResource(id = R.string.ai_generated_badge_label),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LocalPulseColors.current.onSurfaceMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.padding_medium),
                        vertical = dimensionResource(id = R.dimen.padding_tiny)
                    )
                )
            }
            Text(
                text = stringResource(id = R.string.footer_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = LocalPulseColors.current.onSurfaceMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                    vertical = dimensionResource(id = R.dimen.padding_tiny)
                )
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewDisclaimerFooterLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DisclaimerFooter()
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewDisclaimerFooterDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        DisclaimerFooter()
    }
}

@Preview(name = "With AI disclosure (Summary/Analysis)", showBackground = true)
@Composable
private fun PreviewDisclaimerFooterWithAiDisclosure() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DisclaimerFooter(showAiDisclosure = true)
    }
}
