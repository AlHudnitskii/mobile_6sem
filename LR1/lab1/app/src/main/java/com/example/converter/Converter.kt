package com.example.converter

object Converter {

    val categories = listOf("Distance", "Weight", "Currency")

    val units = mapOf(
        "Distance" to listOf("km", "m", "cm", "mi", "ft"),
        "Weight" to listOf("kg", "g", "lb", "oz", "t"),
        "Currency" to listOf("USD", "EUR", "UAH", "GBP", "JPY")
    )

    private val rates = mapOf(
        "Distance" to mapOf(
            "km" to 1.0, "m" to 1000.0, "cm" to 100000.0,
            "mi" to 0.621371, "ft" to 3280.84
        ),
        "Weight" to mapOf(
            "kg" to 1.0, "g" to 1000.0, "lb" to 2.20462,
            "oz" to 35.274, "t" to 0.001
        ),
        "Currency" to mapOf(
            "USD" to 1.0, "EUR" to 0.92, "UAH" to 38.5,
            "GBP" to 0.79, "JPY" to 149.5
        )
    )

    fun convert(value: Double, from: String, to: String, category: String): Double {
        val categoryRates = rates[category] ?: return 0.0
        val fromRate = categoryRates[from] ?: return 0.0
        val toRate = categoryRates[to] ?: return 0.0
        return (value / fromRate) * toRate
    }
}
