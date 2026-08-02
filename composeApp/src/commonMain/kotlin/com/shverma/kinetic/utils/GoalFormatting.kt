package com.shverma.kinetic.utils

fun String.formatGoalName(): String = lowercase()
    .split("_", " ")
    .filter { it.isNotEmpty() }
    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
