package com.shverma.kinetic.ui.onboarding.screens

import com.shverma.kinetic.ui.onboarding.OnboardingStep
import com.shverma.kinetic.ui.onboarding.OnboardingViewModel
import com.shverma.kinetic.ui.onboarding.components.OnboardingStepHeader
import com.shverma.kinetic.ui.onboarding.components.OnboardingBottomNavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shverma.kinetic.ui.theme.BarlowCondensedFamily
import com.shverma.kinetic.ui.theme.LocalKineticTypography
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily
import com.shverma.kinetic.ui.theme.KineticSecondary
import com.shverma.kinetic.ui.theme.KineticTertiary

@Composable
fun OnboardingFlavorProtocolStep(
    viewModel: OnboardingViewModel,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val typography = LocalKineticTypography.current
    val currentStep = OnboardingStep.FLAVOR_PROTOCOL

    var searchQuery by remember { mutableStateOf("") }
    val filteredCuisines = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            flavorCuisines
        } else {
            flavorCuisines.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D))
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {

            OnboardingStepHeader(
                title = currentStep.title,
                subtitle = currentStep.subtitle,
                description = currentStep.description
            )


            Spacer(modifier = Modifier.height(32.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = {
                    Text(
                        "SEARCH CUISINES",
                        style = typography.labelMd.copy(color = Color.Gray)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFC8F135),
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color(0xFFC8F135)
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = typography.bodyMd
            )

            Spacer(modifier = Modifier.height(24.dp))

            CuisineGridSection(
                cuisines = filteredCuisines,
                selectedCuisines = uiState.selectedCuisines,
                onCuisineToggled = { viewModel.toggleCuisine(it) }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Bottom Navigation Buttons
        OnboardingBottomNavigation(
            onContinue = {
                viewModel.saveOnboardingProgress()
                onContinue()
            },
            continueText = "FINISH",
            onBack = { viewModel.previousStep() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

data class FlavorCuisine(
    val id: String,
    val title: String,
    val macroLabel: String,
    val imageUrl: String
)

val flavorCuisines = listOf(
    FlavorCuisine(
        "MEDITERRANEAN",
        "MEDITERRANEAN",
        "OPTIMAL MACRO DISTRIBUTION",
        "https://images.unsplash.com/photo-1544124499-58912cbddaad?auto=format&fit=crop&w=400&q=80"
    ),
    FlavorCuisine(
        "JAPANESE",
        "JAPANESE",
        "HIGH LEAN PROTEIN",
        "https://images.unsplash.com/photo-1580442151529-343f2f5e0e37?auto=format&fit=crop&w=400&q=80"
    ),
    FlavorCuisine(
        "INDIAN",
        "INDIAN",
        "COMPLEX CARB DENSITY",
        "https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=400&q=80"
    ),
    FlavorCuisine(
        "MEXICAN",
        "MEXICAN",
        "FIBER OPTIMIZED",
        "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?auto=format&fit=crop&w=400&q=80"
    ),
    FlavorCuisine(
        "THAI",
        "THAI",
        "METABOLIC ACCELERATOR",
        "https://images.unsplash.com/photo-1559314809-0d155014e29e?auto=format&fit=crop&w=400&q=80"
    ),
    FlavorCuisine(
        "CHINESE",
        "CHINESE",
        "ENERGY EQUILIBRIUM",
        "https://images.unsplash.com/photo-1552611052-33e04de081de?auto=format&fit=crop&w=400&q=80"
    ),
    FlavorCuisine(
        "ITALIAN",
        "ITALIAN",
        "GLYCOGEN REPLENISHMENT",
        "https://images.unsplash.com/photo-1498579150354-977475b7ea0b?auto=format&fit=crop&w=400&q=80"
    ),
    FlavorCuisine(
        "KOREAN",
        "KOREAN",
        "PROBIOTIC DENSITY",
        "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?auto=format&fit=crop&w=400&q=80"
    )
)

@Composable
private fun CuisineGridSection(
    cuisines: List<FlavorCuisine>,
    selectedCuisines: Set<String>,
    onCuisineToggled: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        cuisines.chunked(2).forEach { rowCuisines ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowCuisines.forEach { cuisine ->
                    FlavorCuisineTile(
                        cuisine = cuisine,
                        isSelected = selectedCuisines.contains(cuisine.id),
                        onClick = { onCuisineToggled(cuisine.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCuisines.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun FlavorCuisineTile(
    cuisine: FlavorCuisine,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorFilter = if (isSelected) null else ColorFilter.colorMatrix(ColorMatrix().apply {
        setToSaturation(0f)
    })

    val borderColor = if (isSelected) {
        when (cuisine.id) {
            "BALANCED", "GOURMET" -> KineticSecondary
            "HIGH_PROTEIN" -> KineticTertiary
            else -> Color(0xFFC8F135)
        }
    } else Color.Transparent

    val badgeColor = if (isSelected) {
        when (cuisine.id) {
            "BALANCED", "GOURMET" -> KineticSecondary
            "HIGH_PROTEIN" -> KineticTertiary
            else -> Color(0xFFC8F135)
        }
    } else Color.Transparent

    Box(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .then(
                if (isSelected) Modifier.border(
                    2.5.dp,
                    borderColor,
                    RoundedCornerShape(12.dp)
                )
                else Modifier
            )
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = cuisine.imageUrl,
            contentDescription = cuisine.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = colorFilter
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0f),
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 160.dp.value * 0.4f // Approximate top 40% transparent
                    )
                )
        )

        // Selected Badge
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(badgeColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SELECTED",
                    style = TextStyle(
                        fontFamily = BarlowCondensedFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold, // weight 800
                        color = Color(0xFF111111),
                        letterSpacing = 1.sp
                    )
                )
            }

            // Re-aligning badge to match "Top-center badge" more precisely if needed, 
            // but design says "border-radius 4dp" for badge.
        }

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = cuisine.macroLabel,
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 9.sp,
                    color = if (isSelected) borderColor else Color(0xFFC8F135),
                    fontWeight = FontWeight.SemiBold, // weight 600
                    letterSpacing = 1.5.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cuisine.title,
                style = TextStyle(
                    fontFamily = BarlowCondensedFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }

        // Checkmark Icon
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C896)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
