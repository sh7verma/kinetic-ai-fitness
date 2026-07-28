package com.shverma.kinetic.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Corner radii sampled from docs/redesign/kinetic-redesign-v1.html. Cards use a
 * thin border (outlineVariant at 9% alpha) plus a soft shadow in light mode only —
 * dark mode relies on the border alone, since shadows don't read on a dark surface.
 */
object KineticShape {
    val card = 16.dp
    val button = 14.dp
    val pill = 8.dp
    val chipDot = 6.dp
}
