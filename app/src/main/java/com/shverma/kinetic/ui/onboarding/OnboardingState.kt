package com.shverma.kinetic.ui.onboarding

enum class OnboardingStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String
) {
    BIOMETRICS(
        1,
        "BIOMETRICS",
        "ESTABLISH BASELINE",
        "Calibrate your metabolic training zones and precision output targets for optimal performance."
    ),
    WORKOUT_SETUP(
        2,
        "WORKOUT SETUP",
        "DEFINE MISSION",
        "Tailor your training protocol to your specific goals, schedule, and available equipment."
    ),
    MEAL_SETUP(
        3,
        "MEAL SETUP",
        "FUEL PROTOCOL",
        "Optimize your nutrition to support recovery, energy levels, and body composition changes."
    ),
    ACTIVITY_SETUP(
        4,
        "ACTIVITY LEVEL",
        "ENERGY OUTPUT",
        "Calibrate your daily metabolic baseline for precise energy expenditure tracking."
    ),
    FLAVOR_PROTOCOL(
        5,
        "FLAVOR PROTOCOL",
        "PALATE SYNC",
        "Integrate your regional culinary preferences for accurate meal generation."
    );

    companion object {
        val totalSteps = OnboardingStep.entries.size
        fun fromStepNumber(stepNumber: Int) = OnboardingStep.entries.firstOrNull { it.stepNumber == stepNumber } ?: BIOMETRICS
    }
}
