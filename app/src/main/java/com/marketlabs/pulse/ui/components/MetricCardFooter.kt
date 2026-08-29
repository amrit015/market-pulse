package com.marketlabs.pulse.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * "As of <observed/reported/settlement date>" footer line for every Posture and Positioning metric
 * card (2026-08-26 revamp). Used to also carry the card's info icon, but that moved up next to the
 * card's title (each card now uses `MetricInfoAction` directly in its title row instead) -- kept as
 * its own small component since every card still needs this exact date-line treatment.
 */
@Composable
fun MetricCardFooter(asOfText: String?, modifier: Modifier = Modifier) {
    if (asOfText == null) return
    Text(
        text = asOfText,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewMetricCardFooter() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        MetricCardFooter(asOfText = "As of Aug 26, 2026")
    }
}
