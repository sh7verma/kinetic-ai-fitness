package com.shverma.kinetic.ui.fuel

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

@OptIn(ExperimentalForeignApi::class)
class IosFuelClock : FuelClock {
    private val calendar = NSCalendar.currentCalendar

    private fun dateFromMillis(timestamp: Long): NSDate = NSDate(
        timeIntervalSinceReferenceDate = timestamp.toDouble() / 1000.0 - 978307200.0,
    )

    private fun millisFromDate(date: NSDate): Long =
        ((date.timeIntervalSinceReferenceDate + 978307200.0) * 1000.0).toLong()

    override fun currentWindow(): FuelTimeWindow {
        val now = NSDate()
        val dateComponents = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = now,
        )
        dateComponents.hour = 0
        dateComponents.minute = 0
        dateComponents.second = 0
        dateComponents.nanosecond = 0

        val startOfDay = requireNotNull(calendar.dateFromComponents(dateComponents))
        val todayIndex = dayIndex(millisFromDate(startOfDay))
        val startOfWeek = requireNotNull(
            calendar.dateByAddingUnit(
                unit = NSCalendarUnitDay,
                value = -todayIndex.toLong(),
                toDate = startOfDay,
                options = 0u,
            ),
        )

        return FuelTimeWindow(
            startOfDay = millisFromDate(startOfDay),
            endOfDay = millisFromDate(startOfDay) + 86_399_999L,
            startOfWeek = millisFromDate(startOfWeek),
            todayIndex = todayIndex,
        )
    }

    override fun dayIndex(timestamp: Long): Int {
        val weekday = calendar.component(
            unit = NSCalendarUnitWeekday,
            fromDate = dateFromMillis(timestamp),
        ).toInt()
        return when (weekday) {
            2 -> 0
            3 -> 1
            4 -> 2
            5 -> 3
            6 -> 4
            7 -> 5
            1 -> 6
            else -> 0
        }
    }

    override fun formatTime(timestamp: Long): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = "h:mm a"
        return requireNotNull(
            formatter.stringFromDate(dateFromMillis(timestamp)),
        )
    }
}
