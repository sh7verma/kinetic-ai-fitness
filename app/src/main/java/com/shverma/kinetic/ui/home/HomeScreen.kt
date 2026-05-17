package com.shverma.kinetic.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.shverma.kinetic.ui.components.KineticPrimaryButton
import com.shverma.kinetic.ui.theme.KineticTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.utils.UiEvent
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun HomeScreen(
    snackBarHostState: SnackbarHostState,
    onItemClick: (Int) -> Unit,
    onNavigateToDietPlan: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = KineticTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(colors.background)
    ) {
        Text(
            text = "Home Screen",
            color = colors.onSurface
        )

        state.itemList.forEachIndexed { index, item ->
            Text(
                text = item,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onItemClick(index) },
                color = colors.onSurface
            )
        }

        KineticPrimaryButton(
            text = "Diet Plan Editor",
            onClick = onNavigateToDietPlan
        )

        KineticPrimaryButton(
            text = "Refresh",
            onClick = { viewModel.onEvent(HomeScreenEvents.Refresh) }
        )
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
