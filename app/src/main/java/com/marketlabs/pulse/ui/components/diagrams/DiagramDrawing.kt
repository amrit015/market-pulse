package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

/** An open (unfilled) chevron -- two short strokes meeting at [center], pointing toward
 * [pointingAngleDegrees] (standard `drawArc` convention: 0 = 3 o'clock, increasing clockwise).
 * Shared by every diagram that marks a direction without an affordance-implying solid arrowhead. */
fun DrawScope.drawOpenChevron(color: Color, center: Offset, pointingAngleDegrees: Float, size: Float, strokeWidth: Float) {
    val wingAngle1 = Math.toRadians((pointingAngleDegrees + 150f).toDouble())
    val wingAngle2 = Math.toRadians((pointingAngleDegrees - 150f).toDouble())
    val wing1 = Offset(center.x + size * cos(wingAngle1).toFloat(), center.y + size * sin(wingAngle1).toFloat())
    val wing2 = Offset(center.x + size * cos(wingAngle2).toFloat(), center.y + size * sin(wingAngle2).toFloat())
    drawLine(color = color, start = center, end = wing1, strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color = color, start = center, end = wing2, strokeWidth = strokeWidth, cap = StrokeCap.Round)
}

/** A point at parameter [t] (0f-1f) along a cubic Bezier curve -- used to sample a smooth curve's
 * path when a fill or bracket needs to follow it, since `Path.cubicTo` itself doesn't expose
 * intermediate points. */
fun cubicBezierPoint(t: Float, p0: Offset, c1: Offset, c2: Offset, p3: Offset): Offset {
    val u = 1f - t
    val a = u * u * u
    val b = 3 * u * u * t
    val c = 3 * u * t * t
    val d = t * t * t
    return Offset(
        x = a * p0.x + b * c1.x + c * c2.x + d * p3.x,
        y = a * p0.y + b * c1.y + c * c2.y + d * p3.y
    )
}
