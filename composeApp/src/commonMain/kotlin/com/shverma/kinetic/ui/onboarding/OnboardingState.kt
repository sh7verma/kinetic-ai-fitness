package com.shverma.kinetic.ui.onboarding

/**
 * Trimmed to exactly what feeds the macro-target calculation (see
 * MacrosCalculator.calculate) or first-meal-logging activation — per
 * docs/redesign/kinetic-redesign-v1.html and docs/APP_UPDATE.md.
 */
enum class OnboardingStep(val stepNumber: Int, val title: String) {
    BIOMETRICS(1, "A little about you"),
    GOALS(2, "Your goal & activity"),
    RESULTS(3, "Your daily targets");

    companion object {
        val totalSteps = entries.size
        fun fromStepNumber(stepNumber: Int) = entries.firstOrNull { it.stepNumber == stepNumber } ?: BIOMETRICS
    }
}
