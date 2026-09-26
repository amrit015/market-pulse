package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlin.math.min

/** One cell of a [CorrelationPair] grid. [DIAGONAL] (an asset against itself) is never meaningful,
 * so it draws as a plain slash rather than either correlation state. */
enum class CorrelationCellState { TOGETHER, INDEPENDENT, DIAGONAL }

/** A square NxN grid of [CorrelationCellState] for one condition (e.g. "Normal" or "Stress"),
 * [cells]\[row\]\[column\] ordered to match the shared row/column labels. */
data class CorrelationGrid(val title: String, val cells: List<List<CorrelationCellState>>)

/**
 * Two small correlation grids side by side, sharing one set of row labels -- O1 in the Learn
 * diagram library. No numbers anywhere: a cell's state is filled-vs-outlined-vs-diagonal, never a
 * correlation coefficient, since the point is a qualitative shift (independent -> together), not a
 * precise reading.
 */
@Composable
fun CorrelationPair(
    rowLabels: List<String>,
    columnLabels: List<String>,
    gridA: CorrelationGrid,
    gridB: CorrelationGrid,
    modifier: Modifier = Modifier
) {
    val colors = rememberDiagramColors()
    val rowLabelWidth = dimensionResource(id = R.dimen.diagram_correlation_row_label_width)
    val columnLabelHeight = dimensionResource(id = R.dimen.diagram_correlation_column_label_height)

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.width(rowLabelWidth)) {
                Spacer(modifier = Modifier.height(columnLabelHeight))
                rowLabels.forEach { label ->
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            CorrelationGridColumn(grid = gridA, columnLabels = columnLabels, colors = colors, columnLabelHeight = columnLabelHeight, modifier = Modifier.weight(1f).fillMaxHeight())
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))
            CorrelationGridColumn(grid = gridB, columnLabels = columnLabels, colors = colors, columnLabelHeight = columnLabelHeight, modifier = Modifier.weight(1f).fillMaxHeight())
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            LegendSwatch(filled = true, colors = colors)
            Text(
                text = stringResource(id = R.string.diagram_correlation_legend_together),
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkMuted,
                modifier = Modifier.padding(start = 4.dp, end = 16.dp)
            )
            LegendSwatch(filled = false, colors = colors)
            Text(
                text = stringResource(id = R.string.diagram_correlation_legend_independent),
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkMuted,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun CorrelationGridColumn(
    grid: CorrelationGrid,
    columnLabels: List<String>,
    colors: DiagramColors,
    columnLabelHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = grid.title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth().height(columnLabelHeight)) {
            columnLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkMuted,
                        maxLines = 1,
                        modifier = Modifier.rotate(-40f)
                    )
                }
            }
        }
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val n = grid.cells.size
            val cellSize = min(size.width, size.height) / n
            val gridWidth = cellSize * n
            val startX = (size.width - gridWidth) / 2f
            val padFraction = 0.15f

            grid.cells.forEachIndexed { row, cols ->
                cols.forEachIndexed { col, state ->
                    val left = startX + col * cellSize
                    val top = row * cellSize
                    val pad = cellSize * padFraction
                    when (state) {
                        CorrelationCellState.TOGETHER -> drawRoundRect(
                            color = colors.emphasis,
                            topLeft = Offset(left + pad, top + pad),
                            size = Size(cellSize - pad * 2, cellSize - pad * 2),
                            cornerRadius = CornerRadius(pad, pad)
                        )
                        CorrelationCellState.INDEPENDENT -> drawRoundRect(
                            color = colors.edge,
                            topLeft = Offset(left + pad, top + pad),
                            size = Size(cellSize - pad * 2, cellSize - pad * 2),
                            cornerRadius = CornerRadius(pad, pad),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        CorrelationCellState.DIAGONAL -> drawLine(
                            color = colors.rule,
                            start = Offset(left + pad, top + cellSize - pad),
                            end = Offset(left + cellSize - pad, top + pad),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendSwatch(filled: Boolean, colors: DiagramColors) {
    Box(modifier = Modifier.size(10.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (filled) {
                drawRoundRect(color = colors.emphasis, cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
            } else {
                drawRoundRect(color = colors.edge, cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
            }
        }
    }
}

// ============================================================================
// Presets
// ============================================================================

@Composable
fun CorrelationBreakdownPair(modifier: Modifier = Modifier) {
    val rowLabels = listOf(
        stringResource(id = R.string.diagram_correlation_asset_stocks),
        stringResource(id = R.string.diagram_correlation_asset_bonds),
        stringResource(id = R.string.diagram_correlation_asset_credit),
        stringResource(id = R.string.diagram_correlation_asset_gold)
    )
    val normal = CorrelationGrid(
        title = stringResource(id = R.string.diagram_correlation_normal_title),
        cells = listOf(
            listOf(CorrelationCellState.DIAGONAL, CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT),
            listOf(CorrelationCellState.INDEPENDENT, CorrelationCellState.DIAGONAL, CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT),
            listOf(CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT, CorrelationCellState.DIAGONAL, CorrelationCellState.INDEPENDENT),
            listOf(CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT, CorrelationCellState.DIAGONAL)
        )
    )
    val stress = CorrelationGrid(
        title = stringResource(id = R.string.diagram_correlation_stress_title),
        cells = listOf(
            listOf(CorrelationCellState.DIAGONAL, CorrelationCellState.INDEPENDENT, CorrelationCellState.TOGETHER, CorrelationCellState.INDEPENDENT),
            listOf(CorrelationCellState.INDEPENDENT, CorrelationCellState.DIAGONAL, CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT),
            listOf(CorrelationCellState.TOGETHER, CorrelationCellState.INDEPENDENT, CorrelationCellState.DIAGONAL, CorrelationCellState.INDEPENDENT),
            listOf(CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT, CorrelationCellState.INDEPENDENT, CorrelationCellState.DIAGONAL)
        )
    )
    CorrelationPair(rowLabels = rowLabels, columnLabels = rowLabels, gridA = normal, gridB = stress, modifier = modifier)
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Correlation Breakdown", showBackground = true)
@Composable
private fun PreviewCorrelationBreakdownPair() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        CorrelationBreakdownPair(modifier = Modifier.fillMaxWidth().height(dimensionResource(id = R.dimen.tutorial_diagram_height_tall)))
    }
}
