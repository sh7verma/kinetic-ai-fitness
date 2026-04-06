package com.shverma.app.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Detail Screen for Item ID: $itemId", modifier = Modifier.padding(bottom = 16.dp))

        Button(onClick = onBackClick) {
            Text("Go Back")
        }
    }
}
