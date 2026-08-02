package com.shverma.kinetic.ui.aichat

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AIChatScreen(
    viewModel: AIChatViewModel = koinViewModel(),
) {
    AIChatContent(actions = viewModel)
}
