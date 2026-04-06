package com.shverma.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.app.utils.UiEvent
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun HomeScreen(
    snackBarHostState: SnackbarHostState,
    onItemClick: (Int) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Home Screen")

        state.itemList.forEachIndexed { index, item ->
            Text(
                text = item,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onItemClick(index) }
            )
        }

        Button(onClick = { viewModel.onEvent(HomeScreenEvents.Refresh) }) {
            Text("Refresh")
        }
    }

    LaunchedEffect(true) {
        viewModel.uiEvent.receiveAsFlow().collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    snackBarHostState.showSnackbar(event.message)
                }

                else -> {}
            }
        }
    }
}
