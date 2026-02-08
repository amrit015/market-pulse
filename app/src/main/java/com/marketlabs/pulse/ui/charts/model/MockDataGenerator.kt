package com.marketlabs.pulse.ui.charts.model

import kotlin.random.Random

object MockDataGenerator {

    // Generates a "Random Walk" that looks like a stock chart
    fun generateSPYData(count: Int = 100, startPrice: Float = 50f): List<ChartPoint> {
        val points = mutableListOf<ChartPoint>()
        var currentPrice = startPrice

        for (i in 0 until count) {
            val change = (Random.nextFloat() - 0.5f) * 4
            currentPrice += change

            points.add(ChartPoint(x = i.toFloat(), y = currentPrice))
        }
        return points
    }

    fun generateIntradayData(): List<ChartPoint> {
        val points = mutableListOf<ChartPoint>()
        var currentPrice = 4500f

        for (i in 0 until 100) {
            val change =
                if (i % 2 == 0) (Random.nextFloat() - 10f) * 15 else (Random.nextFloat() + 10f) * 15
            currentPrice += change
            points.add(ChartPoint(x = i.toFloat(), y = currentPrice))
        }
        return points
    }

    fun generateRandomData(count: Int = 7): List<ChartPoint> {
        val points = mutableListOf<ChartPoint>()

        for (i in 1..count) {
            // Generates a random float between 0.0 and 100.0
            val randomY = Random.nextFloat() * 100f

            points.add(ChartPoint(x = i.toFloat(), y = randomY))
        }
        return points
    }
}