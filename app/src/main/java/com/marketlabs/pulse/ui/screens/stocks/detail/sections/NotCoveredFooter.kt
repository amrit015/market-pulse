package com.marketlabs.pulse.ui.screens.stocks.detail.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/** A single muted line from `not_covered`. Rendered as-is -- the backend's own text already reads as a complete sentence. */
@Composable
fun NotCoveredFooter(notCovered: String?, modifier: Modifier = Modifier) {
    if (notCovered == null) return

    PulseCard(style = PulseCardStyle.DATA, modifier = modifier.fillMaxWidth()) {
        Text(
            text = notCovered,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalPulseColors.current.onSurfaceMuted,
            modifier = modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_large))
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewNotCoveredFooterLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        NotCoveredFooter(notCovered = "Not covered: monitor global cloud-spending trends, regulatory antitrust developments, and competitive AI-hardware rollouts from rivals.")
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewNotCoveredFooterDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        NotCoveredFooter(notCovered = "Not covered: monitor global cloud-spending trends, regulatory antitrust developments, and competitive AI-hardware rollouts from rivals.")
    }
}
