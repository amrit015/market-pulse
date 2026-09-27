package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Presentational announcement card -- title, body, and an optional close affordance. Same shape as
 * [NotificationEnableBannerCard] (outer card margin, the title/body column's own top inset vs. the
 * dismiss icon pinned to the row's true top) minus the primary action button, since a feature
 * announcement has nothing to enable yet -- kept visually identical to that card everywhere else so
 * the two read as one banner family, not two different ones. [onDismiss] `null` renders without the
 * close icon, for a destination the reader navigated to on purpose (the Settings "Coming Up" page)
 * where there's nothing to dismiss; non-null is for a banner sitting unprompted on top of a screen's
 * own content (the Analysis screen), which needs a way to go away.
 */
@Composable
fun ComingSoonBannerCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_large),
                vertical = dimensionResource(id = R.dimen.padding_large)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.padding_large),
                    bottom = dimensionResource(id = R.dimen.padding_medium),
                    end = dimensionResource(id = R.dimen.padding_medium)
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = dimensionResource(id = R.dimen.padding_large)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Top)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = stringResource(id = R.string.coming_soon_banner_dismiss_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large))
                    )
                }
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light — dismissible", showBackground = true)
@Composable
private fun PreviewComingSoonBannerCardDismissibleLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        ComingSoonBannerCard(
            title = "Custom lists are coming",
            body = "Soon you'll be able to build your own list of stocks to analyze.",
            onDismiss = {}
        )
    }
}

@Preview(name = "Dark — no dismiss", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewComingSoonBannerCardStaticDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        ComingSoonBannerCard(
            title = "Custom lists are coming",
            body = "Soon you'll be able to build your own list of stocks to analyze."
        )
    }
}
