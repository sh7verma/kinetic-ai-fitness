package com.shverma.kinetic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun KineticTheme(content: @Composable () -> Unit) {
    val colors = KineticDarkColors

    val m3ColorScheme = darkColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryContainer,
        onPrimaryContainer = colors.onPrimaryFixed,
        secondary = colors.secondary,
        onSecondary = colors.onSecondary,
        tertiary = colors.tertiary,
        onTertiary = colors.onTertiary,
        background = colors.background,
        onBackground = colors.onSurface,
        surface = colors.background,
        onSurface = colors.onSurface,
        surfaceVariant = colors.surfaceContainerHigh,
        onSurfaceVariant = colors.onSurfaceVariant,
        surfaceContainer = colors.surfaceContainer,
        surfaceContainerLow = colors.surfaceContainerLow,
        surfaceContainerHigh = colors.surfaceContainerHigh,
        surfaceContainerLowest = colors.surfaceContainerLowest,
        surfaceContainerHighest = colors.surfaceContainerHighest,
        surfaceBright = colors.surfaceBright,
        outline = colors.outlineVariant.copy(alpha = 0.20f),
        outlineVariant = colors.outlineVariant.copy(alpha = 0.12f),
    )

    CompositionLocalProvider(
        LocalKineticColors provides colors,
        LocalKineticTypography provides KineticTypographyDefaults,
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content,
        )
    }
}

object KineticTheme {
    val colors: KineticColors
        @Composable get() = LocalKineticColors.current

    val typography: KineticTypography
        @Composable get() = LocalKineticTypography.current
}
