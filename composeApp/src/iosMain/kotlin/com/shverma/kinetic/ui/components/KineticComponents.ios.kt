package com.shverma.kinetic.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

actual fun Modifier.kineticGlow(
    color: Color,
    blurRadius: Dp,
    cornerRadius: Dp,
): Modifier = this.drawBehind {
    drawRoundRect(
        color = color.copy(alpha = 0.12f),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}
