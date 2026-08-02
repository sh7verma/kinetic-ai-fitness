package com.shverma.kinetic.utils

import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.abs

fun Double.formatCalories(): String = roundToLong().formatWithGrouping()

fun Double.formatPercentage(): String = "${roundToInt()}%"

fun formatMacroPair(current: Double, target: Double): String =
    "${current.roundToInt()}/${target.roundToInt()}g"

private fun Long.formatWithGrouping(): String {
    val negative = this < 0
    val digits = abs(this).toString()
    return buildString {
        if (negative) append('-')
        digits.forEachIndexed { index, digit ->
            if (index > 0 && (digits.length - index) % 3 == 0) append(',')
            append(digit)
        }
    }
}
