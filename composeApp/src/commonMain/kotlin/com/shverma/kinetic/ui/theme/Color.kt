package com.shverma.kinetic.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────
// REDESIGN — "calm, clinical trust" palette (docs/redesign/kinetic-redesign-v1.html)
// Values below are sampled directly from the approved redesign mockup via
// computed styles, not eyeballed. Confidence-tier "Low" colors are the one
// exception — the mockup never renders a low-confidence card, so those are
// derived by applying the same text/dot ratio measured on the High and
// Medium tiers to the one sampled low-confidence text color.
// ─────────────────────────────────────────

// Light mode anchors
val RedesignLightBackground = Color(0xFFF6F4EF)
val RedesignLightSurface = Color(0xFFFFFFFF)
val RedesignLightOnSurface = Color(0xFF201F1B)
val RedesignLightOnSurfaceVariant = Color(0xFF969490) // onSurface @ 45% over background
val RedesignLightOutline = Color(0xFF201F1B) // use at 9% opacity — matches sampled card border
val RedesignLightBrand = Color(0xFF414C89)
val RedesignLightOnBrand = Color(0xFFFFFFFF)

// Light mode tonal steps (anchors are exact; steps between are derived)
val RedesignLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val RedesignLightSurfaceContainerLow = Color(0xFFFBFAF7)
val RedesignLightSurfaceContainer = Color(0xFFFFFFFF)
val RedesignLightSurfaceContainerHigh = Color(0xFFF0EDE6) // sampled: progress-track background
val RedesignLightSurfaceContainerHighest = Color(0xFFEDEAE2)
val RedesignLightSurfaceBright = Color(0xFFFFFFFF)

// Dark mode anchors
val RedesignDarkBackground = Color(0xFF201F1D)
val RedesignDarkSurface = Color(0xFF2A2926)
val RedesignDarkOnSurface = Color(0xFFF2F0EA)
val RedesignDarkOnSurfaceVariant = Color(0xFF7F7D79) // onSurface @ 45% over background
val RedesignDarkOutline = Color(0xFFF2F0EA) // use at 9% opacity — matches sampled card border
val RedesignDarkBrand = Color(0xFF8892CE)
val RedesignDarkOnBrand = Color(0xFF1B1D2E)

// Dark mode tonal steps (anchors are exact; steps between are derived)
val RedesignDarkSurfaceContainerLowest = Color(0xFF171615)
val RedesignDarkSurfaceContainerLow = Color(0xFF201F1D)
val RedesignDarkSurfaceContainer = Color(0xFF2A2926)
val RedesignDarkSurfaceContainerHigh = Color(0xFF333230)
val RedesignDarkSurfaceContainerHighest = Color(0xFF3D3B38)
val RedesignDarkSurfaceBright = Color(0xFF454340)

// Confidence system — light mode (text / dot-base / pill container)
val RedesignConfidenceHighTextLight = Color(0xFF327050)
val RedesignConfidenceHighBaseLight = Color(0xFF3F8B5F)
val RedesignConfidenceHighContainerLight = Color(0xFFE6EFE9)
val RedesignConfidenceMediumTextLight = Color(0xFF9C6B22)
val RedesignConfidenceMediumBaseLight = Color(0xFFC68A34)
val RedesignConfidenceMediumContainerLight = Color(0xFFFBF0DD)
val RedesignConfidenceLowTextLight = Color(0xFFAD432A) // sampled ("Flag as wrong" uses this too)
val RedesignConfidenceLowBaseLight = Color(0xFFD85434) // derived
val RedesignConfidenceLowContainerLight = Color(0xFFFAE9E5) // derived

// Confidence system — dark mode (text / dot-base / pill container)
val RedesignConfidenceHighTextDark = Color(0xFF7FCB9C)
val RedesignConfidenceHighBaseDark = Color(0xFF5AB482)
val RedesignConfidenceHighContainerDark = Color(0xFF323F35)
val RedesignConfidenceMediumTextDark = Color(0xFFE0AC5F)
val RedesignConfidenceMediumBaseDark = Color(0xFFD6A450)
val RedesignConfidenceMediumContainerDark = Color(0xFF463D2D)
val RedesignConfidenceLowTextDark = Color(0xFFE8917A) // sampled ("Flag as wrong" uses this too)
val RedesignConfidenceLowBaseDark = Color(0xFFCC7E69) // derived
val RedesignConfidenceLowContainerDark = Color(0xFF443731) // derived

