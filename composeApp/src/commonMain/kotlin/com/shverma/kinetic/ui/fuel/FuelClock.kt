package com.shverma.kinetic.ui.fuel

data class FuelTimeWindow(
    val startOfDay: Long,
    val endOfDay: Long,
    val startOfWeek: Long,
    val todayIndex: Int,
)

interface FuelClock {
    fun currentWindow(): FuelTimeWindow
    fun dayIndex(timestamp: Long): Int
    fun formatTime(timestamp: Long): String
}
