package com.kharcha.tracker.util

import java.text.DecimalFormat

object CurrencyFormatter {

    private val indianFormat = DecimalFormat("##,##,##,##,###.##")

    /**
     * Formats amount in Indian number system: ₹1,00,000
     */
    fun format(amount: Double): String {
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)

        val parts = absAmount.toString().split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1 && parts[1] != "0") ".${parts[1].take(2)}" else ""

        val formatted = formatIndianInteger(integerPart)
        val prefix = if (isNegative) "-₹" else "₹"
        return "$prefix$formatted$decimalPart"
    }

    /**
     * Formats integer part using Indian grouping: 1,00,00,000
     */
    private fun formatIndianInteger(number: String): String {
        if (number.length <= 3) return number

        val lastThree = number.takeLast(3)
        val remaining = number.dropLast(3)

        val groups = mutableListOf<String>()
        var i = remaining.length
        while (i > 0) {
            val start = maxOf(0, i - 2)
            groups.add(0, remaining.substring(start, i))
            i = start
        }
        groups.add(lastThree)
        return groups.joinToString(",")
    }

    /**
     * Short format: ₹1.5L, ₹2.3Cr
     */
    fun formatShort(amount: Double): String {
        val absAmount = kotlin.math.abs(amount)
        val prefix = if (amount < 0) "-₹" else "₹"
        return when {
            absAmount >= 10_000_000 -> "${prefix}${String.format("%.1f", absAmount / 10_000_000)}Cr"
            absAmount >= 100_000 -> "${prefix}${String.format("%.1f", absAmount / 100_000)}L"
            absAmount >= 1_000 -> "${prefix}${String.format("%.1f", absAmount / 1_000)}K"
            else -> format(amount)
        }
    }
}
