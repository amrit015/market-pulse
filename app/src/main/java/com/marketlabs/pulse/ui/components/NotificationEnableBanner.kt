package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.data.notifications.NotificationPreference
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Stateful banner inviting the user to turn on one push preference, shown on the screen that
 * preference is about. Renders nothing once the user has enabled or dismissed it, or if the
 * preference is already on. "Turn on" also requests the system notification permission, since this
 * is the first moment the user has asked for notifications.
 */
@Composable
fun NotificationEnableBanner(
    preference: NotificationPreference,
    modifier: Modifier = Modifier,
    viewModel: NotificationEnablePromptViewModel = hiltViewModel()
) {
    val visiblePrompts by viewModel.visiblePrompts.collectAsStateWithLifecycle()
    val permission = rememberNotificationPermission()
    if (preference !in visiblePrompts) return

    val (titleRes, bodyRes) = when (preference) {
        NotificationPreference.DAILY_SUMMARY ->
            R.string.notifications_banner_daily_summary_title to R.string.notifications_banner_daily_summary_body

        NotificationPreference.STOCK_ANALYSIS ->
            R.string.notifications_banner_stock_analysis_title to R.string.notifications_banner_stock_analysis_body
    }
    NotificationEnableBannerCard(
        title = stringResource(id = titleRes),
        body = stringResource(id = bodyRes),
        onEnable = {
            viewModel.enable(preference)
            permission.requestIfNeeded()
        },
        onDismiss = { viewModel.dismiss(preference) },
        modifier = modifier
    )
}

@Composable
fun NotificationEnableBannerCard(
    title: String,
    body: String,
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current
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
                // Filled, unlike a TextButton: on an accent-tinted card a text-only button reads as
                // a caption rather than the card's one action.
                Button(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)),
                    onClick = onEnable,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalPulseColors.current.accentPrimary,
                        contentColor = LocalPulseColors.current.accentOn
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.notifications_banner_enable),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Top)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.notifications_banner_dismiss_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large))
                )
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewNotificationEnableBannerLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        NotificationEnableBannerCard(
            title = "Get the daily summary",
            body = "Turn on notifications to know when the evening market summary is ready.",
            onEnable = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewNotificationEnableBannerDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        NotificationEnableBannerCard(
            title = "Stay on top of stock analysis",
            body = "Turn on notifications to know when stock analysis has updated.",
            onEnable = {},
            onDismiss = {}
        )
    }
}
