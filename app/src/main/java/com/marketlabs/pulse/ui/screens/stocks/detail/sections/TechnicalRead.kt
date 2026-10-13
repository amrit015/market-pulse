package com.marketlabs.pulse.ui.screens.stocks.detail.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.CardEyebrowLabel
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The jargon-free one-sentence read (`detail.plainRead`, up to ~170 chars, always carries a figure)
 * shown in Stock Detail's chrome above the tab row. `PulseCard(SYNTHESIS)` -- chrome-level, not a
 * tab card. Absent on a detail doc written before the backend moved this field from the preview,
 * in which case nothing renders.
 */
@Composable
fun PlainRead(plainRead: String?, modifier: Modifier = Modifier) {
    ReadCard(
        title = stringResource(id = R.string.stock_detail_plain_read_title),
        text = plainRead,
        style = PulseCardStyle.SYNTHESIS,
        bodyStyle = MaterialTheme.typography.bodySmall,
        modifier = modifier
    )
}

/**
 * The dense, figure-heavy read (`detail.technicalRead`, indicator names fine) -- a Technicals tab
 * card below Returns, so a `DATA` card like every other card on the tabs. It is the counterpart of
 * [PlainRead], which says the same kind of thing without jargon.
 */
@Composable
fun TechnicalRead(technicalRead: String?, modifier: Modifier = Modifier) {
    ReadCard(
        title = stringResource(id = R.string.stock_detail_technical_read_title),
        text = technicalRead,
        style = PulseCardStyle.DATA,
        // Same body style as Key Levels' commentary, the card it sits near on the Technicals tab.
        bodyStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
private fun ReadCard(
    title: String,
    text: String?,
    style: PulseCardStyle,
    bodyStyle: TextStyle,
    modifier: Modifier
) {
    if (text == null) return
    val pulseColors = LocalPulseColors.current

    PulseCard(style = style, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            CardEyebrowLabel(
                text = title,
                color = pulseColors.accentPrimary
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            Text(
                text = text,
                style = bodyStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private const val MOCK_TEXT = "Amazon's stock sits at \$274.48, trading 16.04% above its 200-day average, while its cash flow yield of 0.78% is barely above the 0.47% risk-free rate."

@Preview(name = "Plain read", showBackground = true)
@Composable
private fun PreviewPlainRead() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PlainRead(plainRead = MOCK_TEXT)
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTechnicalReadLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TechnicalRead(technicalRead = MOCK_TEXT)
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewTechnicalReadDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TechnicalRead(technicalRead = MOCK_TEXT)
    }
}
