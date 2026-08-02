package com.shverma.kinetic.ui.profile

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onLogout: () -> Unit = {},
) {
    ProfileContent(actions = viewModel, onLogout = onLogout)
}
