package com.marketlabs.pulse.ui.screens.stocks.detail.sections

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.extensions.toLongDateString
import com.marketlabs.pulse.utils.extensions.toShortDateString

/**
 * The "DEEP DIVE AVAILABLE, {date}" and "NEXT: {date}" parts, in display order -- shared by Stock
 * Detail's chrome-level [DeepDiveCard] (both parts, joined) and, via [deepDivePreviewText], the
 * preview card (only ever one part -- see that function's own doc comment for why it doesn't reuse
 * this list directly). Each part's label half is pre-uppercased in its own string resource (not
 * runtime-`.uppercase()`'d here) so the interpolated date stays natural case when joined --
 * "DEEP DIVE AVAILABLE, Sep 4", not "DEEP DIVE AVAILABLE, SEP 4". Empty when both dates are null
 * (caller omits the row entirely, never "Deep Dive: never"). Only `nextDeepDiveTriggerDate`
 * present (every tracked symbol gets one nightly, even before its first-ever deep dive) ->
 * "NEXT DEEP DIVE: {date}" alone, spelling out "Deep Dive" since there's no preceding
 * "Deep Dive Available, ..." part for it to pair with in that case. Dates render abbreviated
 * ("Sep 4", `toShortDateString()`) -- the preview card's own compact touchpoint uses the full
 * month name instead, see [deepDivePreviewText].
 */
@Composable
fun deepDiveDisplayParts(deepAnalysisDate: String?, nextDeepDiveTriggerDate: String?): List<String> {
    val datedPart = deepAnalysisDate?.let {
        stringResource(id = R.string.stock_detail_deep_dive_dated, it.toShortDateString())
    }
    val nextPart = nextDeepDiveTriggerDate?.let {
        val nextRes = if (deepAnalysisDate == null) R.string.stock_detail_deep_dive_next_standalone else R.string.stock_detail_deep_dive_next
        stringResource(id = nextRes, it.toShortDateString())
    }
    return listOfNotNull(datedPart, nextPart)
}

/**
 * The preview card's own single-line text -- deliberately **not** [deepDiveDisplayParts]
 * joined: this compact touchpoint shows exactly one of "DEEP DIVE AVAILABLE, {date}" (when a deep
 * dive has actually run) or "NEXT DEEP DIVE: {date}" (cold start), never both together even when
 * both dates are present -- Stock Detail's own chrome (`DeepDiveCard`) is where the fuller
 * "available, ... • next: ..." combined line lives. Dates render with the full month name
 * (`toLongDateString()`, "September 4") rather than the abbreviated form `deepDiveDisplayParts`
 * uses elsewhere -- this is the one Deep Dive touchpoint reached from a dense list row rather than
 * a full card/screen, so it reads less like shorthand.
 */
@Composable
fun deepDivePreviewText(deepAnalysisDate: String?, nextDeepDiveTriggerDate: String?): String? = when {
    deepAnalysisDate != null -> stringResource(id = R.string.stock_detail_deep_dive_dated, deepAnalysisDate.toLongDateString())
    nextDeepDiveTriggerDate != null -> stringResource(id = R.string.stock_detail_deep_dive_next_standalone, nextDeepDiveTriggerDate.toLongDateString())
    else -> null
}

/**
 * Small icon + bold-accent label — the compact Deep Dive touchpoint used on the preview list card.
 * When [isFlashing] is true, renders a continuous noticeable flashing/pulsing highlight until clicked.
 */
@Composable
fun DeepDiveLabel(
    deepAnalysisDate: String?,
    nextDeepDiveTriggerDate: String?,
    modifier: Modifier = Modifier,
    isFlashing: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val text = deepDivePreviewText(deepAnalysisDate, nextDeepDiveTriggerDate) ?: return
    val pulseColors = LocalPulseColors.current

    val flashAlpha = if (isFlashing) {
        val infiniteTransition = rememberInfiniteTransition(label = "DeepDiveLabelFlash")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1300, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "DeepDiveLabelFlashAlpha"
        )
        alpha
    } else 1.0f

    val contentColor = pulseColors.accentPrimary.copy(alpha = flashAlpha)
    val containerModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Row(modifier = containerModifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.ic_deep_dive),
            contentDescription = stringResource(id = R.string.stock_analysis_ai_glyph_content_description),
            tint = contentColor,
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large))
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}

@Preview(name = "Available", showBackground = true)
@Composable
private fun PreviewDeepDiveLabelAvailable() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DeepDiveLabel(deepAnalysisDate = "2026-08-28", nextDeepDiveTriggerDate = "2026-09-11")
    }
}

@Preview(name = "Cold start", showBackground = true)
@Composable
private fun PreviewDeepDiveLabelColdStart() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DeepDiveLabel(deepAnalysisDate = null, nextDeepDiveTriggerDate = "2026-09-18")
    }
}
