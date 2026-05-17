package com.shverma.kinetic.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class KineticColors(
    val background: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val surfaceBright: Color,
    val outlineVariant: Color,
    val primary: Color,
    val primaryContainer: Color,
    val primaryDim: Color,
    val onPrimaryFixed: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color,
    val onSurfaceVariant: Color,
    val onSurface: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onTertiary: Color,
    val protein: Color,
    val carbs: Color,
    val fats: Color,
)

val KineticDarkColors = KineticColors(
    background = KineticBackground,
    surfaceContainerLowest = KineticSurfaceContainerLowest,
    surfaceContainerLow = KineticSurfaceContainerLow,
    surfaceContainer = KineticSurfaceContainer,
    surfaceContainerHigh = KineticSurfaceContainerHigh,
    surfaceContainerHighest = KineticSurfaceContainerHighest,
    surfaceBright = KineticSurfaceBright,
    outlineVariant = KineticOutlineVariant,
    primary = KineticPrimary,
    primaryContainer = KineticPrimaryContainer,
    primaryDim = KineticPrimaryDim,
    onPrimaryFixed = KineticOnPrimaryFixed,
    secondary = KineticSecondary,
    tertiary = KineticTertiary,
    error = KineticTertiary,
    onSurfaceVariant = KineticOnSurfaceVariant,
    onSurface = KineticOnSurface,
    onPrimary = KineticOnPrimary,
    onSecondary = KineticOnSecondary,
    onTertiary = KineticOnTertiary,
    protein = KineticPrimaryContainer,
    carbs = KineticSecondary,
    fats = KineticTertiary,
)

val LocalKineticColors = staticCompositionLocalOf<KineticColors> {
    error("No KineticColors provided — wrap your UI in KineticTheme { }")
}
