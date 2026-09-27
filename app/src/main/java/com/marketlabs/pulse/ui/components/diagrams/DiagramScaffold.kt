package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Density
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors

/** A diagram's fixed height, for the geometric preset library -- D1-D4 and the real-widget mock
 * previews keep managing their own natural height instead (pass `size = null`). */
enum class DiagramSize { DEFAULT, TALL }

/** Which "not real data" line, if any, a diagram shows below its caption. */
enum class IllustrativeNote { NONE, SCHEMATIC, LIVE_WIDGET_PREVIEW }

private const val DIAGRAM_FONT_SCALE_CAP = 1.3f

/**
 * Shared frame for every Learn/Tutorials diagram -- hand-drawn conceptual diagrams, real-widget
 * mock previews, and the geometric preset library alike. Owns accessibility (one merged node over
 * the whole diagram, since a screen reader should announce it as a single image, not element by
 * element), the caption/illustrative-note text, and a cap on in-diagram font scaling (the fixed
 * [DiagramSize] heights can't grow with arbitrarily large system text; the caption below is
 * ordinary text and scales uncapped).
 */
@Composable
fun DiagramScaffold(
    contentDescription: String,
    illustrativeNote: IllustrativeNote,
    modifier: Modifier = Modifier,
    size: DiagramSize? = null,
    caption: String? = null,
    content: @Composable () -> Unit
) {
    val baseDensity = LocalDensity.current
    val cappedDensity = remember(baseDensity) {
        Density(density = baseDensity.density, fontScale = baseDensity.fontScale.coerceAtMost(DIAGRAM_FONT_SCALE_CAP))
    }
    val noteText = when (illustrativeNote) {
        IllustrativeNote.NONE -> null
        IllustrativeNote.SCHEMATIC -> stringResource(id = R.string.tutorial_diagram_illustrative_note)
        IllustrativeNote.LIVE_WIDGET_PREVIEW -> stringResource(id = R.string.tutorial_gauge_illustrative_caption)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { this.contentDescription = contentDescription }
    ) {
        CompositionLocalProvider(LocalDensity provides cappedDensity) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { boxModifier ->
                        when (size) {
                            DiagramSize.DEFAULT -> boxModifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height))
                            DiagramSize.TALL -> boxModifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height_tall))
                            null -> boxModifier
                        }
                    }
            ) {
                content()
            }
        }
        if (caption != null) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            Text(
                text = caption,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (noteText != null) {
            if (caption != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            }
            Text(
                text = noteText,
                style = MaterialTheme.typography.labelSmall,
                color = LocalPulseColors.current.onSurfaceMuted
            )
        }
    }
}
