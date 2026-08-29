package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.widgets.MetricInfoAction
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Title + info-icon row for every Posture and Positioning metric card. The info icon used to sit
 * in the card's footer next to the "as of" date; it now sits directly after the title instead, so
 * the "what is this metric" explanation reads as attached to the card's name, not to its
 * timestamp. `description` is the same backend-authored text `MetricInfoAction` always uses --
 * this is a layout change only, not a new data source.
 */
@Composable
fun MetricCardTitleRow(title: String, description: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
        MetricInfoAction(title = title, description = description)
    }
}

/**
 * "<short field label> [STATUS PILL]" row -- sits just below a card's title now instead of at the
 * far right of the title row, so the reader sees what KIND of signal the pill is (Status, Trend,
 * Sentiment, Positioning) before the pill's own value, rather than a bare badge with no context.
 */
@Composable
fun MetricStatusRow(label: String, statusText: String, pillColor: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
        SignalPill(text = statusText, pillColor = pillColor, contentColor = contentColor)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewMetricCardHeader() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        val pulseColors = LocalPulseColors.current
        Column {
            MetricCardTitleRow(title = "NAAIM Exposure", description = "Tracks manager equity exposure.")
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            MetricStatusRow(label = "Status", statusText = "BULLISH", pillColor = pulseColors.signalBullishPill, contentColor = pulseColors.signalBullishText)
        }
    }
}
