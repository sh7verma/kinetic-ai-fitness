package com.shverma.kinetic.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Date formatting extensions
fun Date.formatTo(pattern: String, locale: Locale = Locale.getDefault()): String {
    return SimpleDateFormat(pattern, locale).format(this)
}

fun Long.formatDateTo(pattern: String, locale: Locale = Locale.getDefault()): String {
    return Date(this).formatTo(pattern, locale)
}

fun Date.toIsoDateString(): String = formatTo("yyyy-MM-dd")
fun Date.toTimeString(): String = formatTo("h:mm a")
fun Date.toDayOfWeekShort(): String = formatTo("E")

// Date calculation extensions
fun Date.addDays(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return calendar.time
}

fun Date.subtractDays(days: Int): Date = addDays(-days)

// Number/String formatting extensions for consistency
fun Double.formatCalories(): String = String.format("%,.0f", this)
fun Double.formatMacroGrams(): String = String.format("%.0fg", this)
fun Double.formatMacroGramsOneDecimal(): String = String.format("%.1fg", this)
fun Double.formatPercentage(): String = String.format("%.0f%%", this)

fun Float.formatPercentage(): String = this.toDouble().formatPercentage()

// Progress calculation helpers
fun calculateProgress(current: Double, target: Double): Float {
    return if (target > 0) (current / target).toFloat().coerceIn(0f, 1f) else 0f
}
