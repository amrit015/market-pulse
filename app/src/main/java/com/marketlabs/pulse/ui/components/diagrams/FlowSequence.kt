package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/** One box in a [FlowSequence]. */
data class FlowNode(val name: String, val subLabel: String? = null)

/** How a [FlowSequence]'s nodes connect -- P6 in the Learn diagram library. [Hub] draws one wider,
 * emphasized node linked to every other node with unlabeled lines (influence, not order). [Staircase]
 * numbers each node and indents it further than the last, with node 1 emphasized as the entry point. */
sealed interface FlowLayout {
    data class Hub(val hubIndex: Int) : FlowLayout
    data object Staircase : FlowLayout
}

@Composable
fun FlowSequence(nodes: List<FlowNode>, layout: FlowLayout, modifier: Modifier = Modifier) {
    when (layout) {
        is FlowLayout.Hub -> HubFlowSequence(nodes, layout.hubIndex, modifier)
        FlowLayout.Staircase -> StaircaseFlowSequence(nodes, modifier)
    }
}

@Composable
private fun HubFlowSequence(nodes: List<FlowNode>, hubIndex: Int, modifier: Modifier = Modifier) {
    val colors = rememberDiagramColors()
    val hub = nodes[hubIndex]
    val satellites = nodes.filterIndexed { index, _ -> index != hubIndex }

    Row(modifier = modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            satellites.forEach { node ->
                FlowNodeBox(node = node, isEmphasized = false, colors = colors, modifier = Modifier.fillMaxWidth())
            }
        }

        Canvas(modifier = Modifier.width(dimensionResource(id = R.dimen.diagram_flow_connector_width)).fillMaxSize()) {
            val hubX = size.width
            val hubY = size.height / 2f
            val satelliteX = 0f
            val step = size.height / satellites.size
            for (i in satellites.indices) {
                val y = step * i + step / 2f
                drawLine(color = colors.inkMuted, start = Offset(satelliteX, y), end = Offset(hubX, hubY), strokeWidth = 1.dp.toPx())
            }
        }

        FlowNodeBox(
            node = hub,
            isEmphasized = true,
            colors = colors,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StaircaseFlowSequence(nodes: List<FlowNode>, modifier: Modifier = Modifier) {
    val colors = rememberDiagramColors()
    val indent = dimensionResource(id = R.dimen.diagram_flow_staircase_indent)

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        nodes.forEachIndexed { index, node ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = indent * index),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(indent / 2)
                            .height(1.dp)
                            .background(colors.inkMuted)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.diagram_flow_number_badge_size))
                        .background(
                            color = if (index == 0) colors.emphasis else colors.card,
                            shape = CircleShape
                        )
                        .border(width = 1.dp, color = colors.edge, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (index == 0) colors.onEmphasis else colors.ink
                    )
                }
                FlowNodeBox(node = node, isEmphasized = index == 0, colors = colors, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun FlowNodeBox(node: FlowNode, isEmphasized: Boolean, colors: DiagramColors, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = if (isEmphasized) colors.emphasis else colors.card, shape = RoundedCornerShape(dimensionResource(id = R.dimen.diagram_bar_corner_radius)))
            .border(width = 1.dp, color = colors.edge, shape = RoundedCornerShape(dimensionResource(id = R.dimen.diagram_bar_corner_radius)))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isEmphasized) colors.onEmphasis else colors.ink
            )
            node.subLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEmphasized) colors.onEmphasis else colors.inkMuted
                )
            }
        }
    }
}

// ============================================================================
// Presets
// ============================================================================

@Composable
fun MacroVitalsFlow(modifier: Modifier = Modifier) {
    FlowSequence(
        nodes = listOf(
            FlowNode(stringResource(id = R.string.diagram_flow_macro_inflation)),
            FlowNode(stringResource(id = R.string.diagram_flow_macro_jobs)),
            FlowNode(stringResource(id = R.string.diagram_flow_macro_growth)),
            FlowNode(stringResource(id = R.string.diagram_flow_macro_yield))
        ),
        layout = FlowLayout.Hub(hubIndex = 3),
        modifier = modifier
    )
}

@Composable
fun StockAnalysisFlow(modifier: Modifier = Modifier) {
    FlowSequence(
        nodes = listOf(
            FlowNode(stringResource(id = R.string.diagram_flow_stock_setup)),
            FlowNode(stringResource(id = R.string.diagram_flow_stock_levels)),
            FlowNode(stringResource(id = R.string.diagram_flow_stock_forward))
        ),
        layout = FlowLayout.Staircase,
        modifier = modifier
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Macro Vitals (Hub)", showBackground = true)
@Composable
private fun PreviewMacroVitalsFlow() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        MacroVitalsFlow(modifier = Modifier.fillMaxWidth().height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}

@Preview(name = "Stock Analysis (Staircase)", showBackground = true)
@Composable
private fun PreviewStockAnalysisFlow() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        StockAnalysisFlow(modifier = Modifier.fillMaxWidth().height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}
