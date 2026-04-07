package com.shverma.kinetic.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shverma.kinetic.ui.theme.KineticTheme

@Composable
fun CoachScreen() {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Coach Screen",
            style = KineticTheme.typography.displayMd,
            color = colors.onSurface
        )
    }
}
