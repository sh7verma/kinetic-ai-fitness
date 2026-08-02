package com.shverma.kinetic.ui.aichat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.aichat.UIFoodItem
import com.shverma.kinetic.ui.aichat.UILog
import com.shverma.kinetic.ui.aichat.UIMeal
import com.shverma.kinetic.ui.theme.ConfidenceTier
import com.shverma.kinetic.ui.theme.KineticShape
import com.shverma.kinetic.ui.theme.KineticSpacing
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.colors

/**
 * Renders a parsed meal log as one card per food item — the core trust surface
 * of the redesign (docs/redesign/kinetic-redesign-v1.html). Each item carries its
 * own confidence tier and visible assumptions; corrections happen in place.
 */
@Composable
fun LogFoodComponent(
    uiLog: UILog,
    onSaveMeal: (UIMeal, UIMeal) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editedLog by remember(uiLog) { mutableStateOf(uiLog) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KineticSpacing.lg),
    ) {
        editedLog.meals.forEachIndexed { mealIndex, meal ->
            Column(verticalArrangement = Arrangement.spacedBy(KineticSpacing.md)) {
                meal.items.forEachIndexed { itemIndex, item ->
                    FoodItemCard(
                        item = item,
                        isSaved = meal.isSaved,
                        onGramsChange = { newGrams ->
                            val density = if (item.grams > 0) {
                                listOf(
                                    item.calories / item.grams,
                                    item.protein / item.grams,
                                    item.carbs / item.grams,
                                    item.fats / item.grams,
                                )
                            } else {
                                listOf(0.0, 0.0, 0.0, 0.0)
                            }
                            val updatedItem = item.copy(
                                grams = newGrams,
                                calories = density[0] * newGrams,
                                protein = density[1] * newGrams,
                                carbs = density[2] * newGrams,
                                fats = density[3] * newGrams,
                            )
                            val updatedItems = meal.items.toMutableList().also { it[itemIndex] = updatedItem }
                            val updatedMeal = meal.copy(
                                items = updatedItems,
                                totalCalories = updatedItems.sumOf { it.calories },
                                totalProtein = updatedItems.sumOf { it.protein },
                                totalCarbs = updatedItems.sumOf { it.carbs },
                                totalFats = updatedItems.sumOf { it.fats },
                            )
                            val updatedMeals = editedLog.meals.toMutableList().also { it[mealIndex] = updatedMeal }
                            editedLog = editedLog.copy(meals = updatedMeals)
                        },
                    )
                }

                if (meal.isSaved) {
                    SavedIndicator()
                } else {
                    SaveToLogButton(
                        onClick = { onSaveMeal(uiLog.meals[mealIndex], meal) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodItemCard(
    item: UIFoodItem,
    isSaved: Boolean,
    onGramsChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    var expanded by remember { mutableStateOf(false) }
    var flagged by remember { mutableStateOf(false) }
    val tier = remember(item.confidence) { ConfidenceTier.fromScore(item.confidence) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.card))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.09f), RoundedCornerShape(KineticShape.card))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(text = item.name, style = typography.titleMd, color = colors.onSurface)
                Text(
                    text = "${item.grams.toInt()}g",
                    style = typography.bodySm,
                    color = colors.onSurfaceVariant,
                )
            }
            ConfidencePill(tier)
        }

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(KineticSpacing.xs)) {
            Text(text = "${item.calories.toInt()}", style = typography.displaySm, color = colors.onSurface)
            Text(
                text = "kcal",
                style = typography.bodySm,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.width(KineticSpacing.xs))
            Text(
                text = "P ${item.protein.toInt()}g · C ${item.carbs.toInt()}g · F ${item.fats.toInt()}g",
                style = typography.bodySm,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        if (!item.assumed.isNullOrBlank()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(KineticSpacing.xs),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(top = 2.dp),
                )
                Text(
                    text = "Assumed: ${item.assumed}",
                    style = typography.bodySm,
                    color = colors.onSurfaceVariant,
                )
            }
        }

        if (expanded && !isSaved) {
            GramsStepper(
                grams = item.grams,
                caloriesPerGram = if (item.grams > 0) item.calories / item.grams else 0.0,
                onGramsChange = onGramsChange,
            )
        }

        if (!isSaved) {
            Row(horizontalArrangement = Arrangement.spacedBy(KineticSpacing.xl)) {
                Text(
                    text = if (expanded) "Done" else "Adjust grams",
                    style = typography.labelLg,
                    color = colors.primary,
                    modifier = Modifier.clickable { expanded = !expanded },
                )
                Text(
                    text = if (flagged) "Flagged — thanks" else "Flag as wrong",
                    style = typography.labelLg,
                    color = colors.confidenceLowText,
                    modifier = Modifier.clickable(enabled = !flagged) { flagged = true },
                )
            }
        }
    }
}

@Composable
private fun ConfidencePill(tier: ConfidenceTier, modifier: Modifier = Modifier) {
    val confidenceColors = tier.colors()
    val label = when (tier) {
        ConfidenceTier.High -> "High confidence"
        ConfidenceTier.Medium -> "Worth a glance"
        ConfidenceTier.Low -> "Needs review"
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(KineticShape.pill))
            .background(confidenceColors.container)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(KineticShape.chipDot)
                .background(confidenceColors.base, CircleShape),
        )
        Text(text = label, style = KineticTheme.typography.labelMd, color = confidenceColors.text)
    }
}

@Composable
private fun GramsStepper(
    grams: Double,
    caloriesPerGram: Double,
    onGramsChange: (Double) -> Unit,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, RoundedCornerShape(KineticShape.pill))
            .padding(KineticSpacing.md),
        verticalArrangement = Arrangement.spacedBy(KineticSpacing.sm),
    ) {
        Text(text = "ADJUST PORTION", style = typography.labelMd, color = colors.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(icon = Icons.Filled.Remove, onClick = { onGramsChange((grams - 10.0).coerceAtLeast(0.0)) })
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${grams.toInt()} g", style = typography.titleMd, color = colors.onSurface)
                Text(
                    text = "≈ ${(grams * caloriesPerGram).toInt()} kcal",
                    style = typography.bodySm,
                    color = colors.onSurfaceVariant,
                )
            }
            StepperButton(icon = Icons.Filled.Add, onClick = { onGramsChange(grams + 10.0) })
        }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, onClick: () -> Unit) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.surfaceContainerLowest)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.09f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SaveToLogButton(onClick: () -> Unit) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.button))
            .background(colors.primary)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Save to today's log", style = typography.titleSm, color = colors.onPrimary)
    }
}

@Composable
private fun SavedIndicator() {
    val colors = KineticTheme.colors
    Text(
        text = "Saved to today's log",
        style = KineticTheme.typography.labelLg,
        color = colors.confidenceHighText,
    )
}
