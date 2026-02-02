package com.marketlabs.pulse.ui.charts.model

// Represents one point on the graph (Time, Price)
data class ChartPoint(
    val x: Float, // Index (0..99)
    val y: Float  // Price
)