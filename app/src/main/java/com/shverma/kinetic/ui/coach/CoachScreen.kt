package com.shverma.kinetic.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.components.KineticPrimaryButton
import com.shverma.kinetic.ui.theme.KineticTheme

@Composable
fun CoachScreen(
    onMealChatClick: () -> Unit = {}
) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "AI Coach",
                style = KineticTheme.typography.displayMd,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(32.dp))
            KineticPrimaryButton(
                text = "Nutrition Chat",
                onClick = onMealChatClick
            )
        }
    }
}
