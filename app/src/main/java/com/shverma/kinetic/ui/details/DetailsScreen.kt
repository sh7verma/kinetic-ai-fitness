package com.shverma.kinetic.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import com.shverma.kinetic.ui.components.KineticSecondaryButton
import com.shverma.kinetic.ui.theme.KineticTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun DetailScreen(
    itemId: Int,
    snackBarHostState: SnackbarHostState,
    onBackClick: () -> Unit
) {
    val colors = KineticTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(colors.background)
    ) {
        Text(
            text = "Detail Screen for Item ID: $itemId",
            modifier = Modifier.padding(bottom = 16.dp),
            color = colors.onSurface
        )

        KineticSecondaryButton(
            text = "Go Back",
            onClick = onBackClick
        )
    }
}
