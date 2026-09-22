package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Plain, non-interactive, muted text --
 * not a tappable pill. Sits in the same top-right spot in a SYNTHESIS card's header every call site
 * already lays out. Disclosure only, nothing to tap -- the explainer affordance is a per-SCREEN
 * "?" (see `ScreenGuideAction`), not a per-card tap.
 */
@Composable
fun AiGeneratedLabel(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.ai_generated_badge_label),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = LocalPulseColors.current.onSurfaceMuted,
        modifier = modifier
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewAiGeneratedLabelLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        AiGeneratedLabel()
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewAiGeneratedLabelDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        AiGeneratedLabel()
    }
}
