package com.shverma.kinetic.ui.fuel

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FuelScreen(
    viewModel: FuelViewModel = koinViewModel(),
    onAIChatClick: () -> Unit = {},
    onEnergyCardClick: () -> Unit = {},
) {
    FuelContent(
        actions = viewModel,
        onAIChatClick = onAIChatClick,
        onEnergyCardClick = onEnergyCardClick,
    )
}
