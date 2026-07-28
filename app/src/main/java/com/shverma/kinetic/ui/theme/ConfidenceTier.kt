package com.shverma.kinetic.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The AI's confidence in a single parsed food item's estimate — the core trust
 * signal surfaced throughout meal logging (see docs/redesign/kinetic-redesign-v1.html).
 * Applies per food item, not per meal: a mixed dish can drag one item's tier down
 * even when the rest of the meal is High.
 */
enum class ConfidenceTier {
    High,
    Medium,
    Low;

    companion object {
        /**
         * Collapses the AI's 0.0-1.0 per-item confidence score (see
         * AIPrompts.logMealSystemPrompt's per-item confidence bands) into the
         * 3-tier system the UI shows. >=0.7 covers both "exact input" and
         * "minor estimation" from the prompt — both read as trustworthy to a user.
         */
        fun fromScore(score: Double): ConfidenceTier = when {
            score >= 0.7 -> High
            score >= 0.4 -> Medium
            else -> Low
        }
    }
}

data class ConfidenceColors(
    val text: Color,
    val base: Color,
    val container: Color,
)

@Composable
fun ConfidenceTier.colors(): ConfidenceColors {
    val colors = KineticTheme.colors
    return when (this) {
        ConfidenceTier.High -> ConfidenceColors(
            text = colors.confidenceHighText,
            base = colors.confidenceHighBase,
            container = colors.confidenceHighContainer,
        )
        ConfidenceTier.Medium -> ConfidenceColors(
            text = colors.confidenceMediumText,
            base = colors.confidenceMediumBase,
            container = colors.confidenceMediumContainer,
        )
        ConfidenceTier.Low -> ConfidenceColors(
            text = colors.confidenceLowText,
            base = colors.confidenceLowBase,
            container = colors.confidenceLowContainer,
        )
    }
}
