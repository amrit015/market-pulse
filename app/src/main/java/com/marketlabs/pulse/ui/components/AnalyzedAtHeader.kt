package com.marketlabs.pulse.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Top-of-content timestamp, outside any card -- the one shared "Analyzed as of" element every
 * report-style screen's `LazyColumn` starts with (Indicators, Summary), so it renders at the exact
 * same style and vertical offset on both rather than each screen hand-rolling its own copy that
 * could drift apart. Callers are responsible for their own spacing after it -- this composable is
 * just the text itself, no trailing `Spacer`.
 */
@Composable
fun AnalyzedAtHeader(timestamp: Long) {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

    Text(
        text = stringResource(id = R.string.analyzed_at, format.format(date)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AnalyzedAtHeaderPreview() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        AnalyzedAtHeader(timestamp = System.currentTimeMillis())
    }
}
