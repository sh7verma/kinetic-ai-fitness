package com.shverma.kinetic.ui.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.theme.KineticShape
import com.shverma.kinetic.ui.theme.KineticSpacing
import com.shverma.kinetic.ui.theme.KineticTheme

@Composable
fun WelcomeContent(
    authState: AuthState,
    snackBarHostState: SnackbarHostState? = null,
    onSignInWithGoogle: () -> Unit,
    onClearError: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLanding: () -> Unit,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                if (state.hasProfile) onNavigateToLanding() else onNavigateToOnboarding()
            }
            is AuthState.Error -> {
                snackBarHostState?.showSnackbar(state.message)
                onClearError()
            }
            else -> Unit
        }
    }

    AnimatedVisibility(visible = true, enter = fadeIn()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(KineticSpacing.xxl),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(KineticSpacing.lg),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "K", style = typography.titleLg, color = colors.onPrimary)
                }
                Text(text = "Kinetic", style = typography.displaySm, color = colors.onSurface)
                Text(
                    text = "Log your meals in plain English. Trust the numbers.",
                    style = typography.bodyLg,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(KineticSpacing.md),
            ) {
                ContinueWithGoogleButton(
                    isLoading = authState is AuthState.Loading,
                    onClick = onSignInWithGoogle,
                )
                Text(
                    text = "Estimates, not medical advice.",
                    style = typography.bodySm,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ContinueWithGoogleButton(
    onClick: () -> Unit,
    isLoading: Boolean,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.button))
            .background(colors.surfaceContainerLowest)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.09f), RoundedCornerShape(KineticShape.button))
            .clickable(
                enabled = !isLoading,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(vertical = 16.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.primary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = "G   Continue with Google",
                style = typography.titleSm,
                color = colors.onSurface,
            )
        }
    }
}
