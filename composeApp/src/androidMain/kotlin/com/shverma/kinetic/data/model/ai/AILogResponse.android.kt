package com.shverma.kinetic.data.model.ai

actual fun currentHour(): Int =
    java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
