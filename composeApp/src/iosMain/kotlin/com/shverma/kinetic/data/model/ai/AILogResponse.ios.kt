package com.shverma.kinetic.data.model.ai

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSDate

actual fun currentHour(): Int =
    NSCalendar.currentCalendar.components(NSCalendarUnitHour, fromDate = NSDate()).hour.toInt()
