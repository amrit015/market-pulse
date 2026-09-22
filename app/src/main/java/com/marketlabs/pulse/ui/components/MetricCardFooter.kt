package com.marketlabs.pulse.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * "As of <observed/reported/settlement date>" footer line for every Posture and Positioning metric
 * card. The card's info icon lives in its title row (via `MetricInfoAction`), not here -- this is
 * its own small component because every card needs this exact date-line treatment.
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
