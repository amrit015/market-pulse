package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Bare back arrow with the screen's own [title] directly to its right -- the header for pushed
 * screens that don't use a `TopAppBar` (Settings' sub-screens, Tutorials, Terms/Privacy). The title
 * always names the current screen, never the one the back arrow returns to, so every screen reads
 * the same way. No background or elevation, so it sits flush on the screen background.
 */
@Composable
fun PulseBackTitleRow(
    title: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 💡 Same metrics as a Material `TopAppBar` (standard height, arrow inset from the start edge,
    // title one step in from the arrow) so this row and the screens that do use a real `TopAppBar`
    // (Settings, News, ...) put the arrow and title at identical positions.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TopAppBarDefaults.TopAppBarExpandedHeight)
            .padding(start = dimensionResource(id = R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateUp) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = stringResource(id = R.string.nav_back_content_description),
                tint = LocalPulseColors.current.onSurfaceMuted
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            // 💡 4dp, not more: Material's `TopAppBar` puts its title 4dp after the 48dp icon slot
            // (which itself starts 4dp in), so the title begins 56dp from the edge. This was 12dp,
            // which left every screen using this row 8dp further from its back arrow than Settings.
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.padding_small),
                end = dimensionResource(id = R.dimen.padding_large)
            )
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewPulseBackTitleRowLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PulseBackTitleRow(title = "Learn", onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewPulseBackTitleRowDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PulseBackTitleRow(title = "Learn", onNavigateUp = {})
    }
}
