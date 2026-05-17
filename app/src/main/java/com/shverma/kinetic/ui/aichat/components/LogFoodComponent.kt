package com.shverma.kinetic.ui.aichat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.aichat.UIFoodItem
import com.shverma.kinetic.ui.aichat.UIMeal
import com.shverma.kinetic.ui.aichat.UILog
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily

@Composable
fun LogFoodComponent(
    uiLog: UILog,
    onSaveMeal: (UIMeal) -> Unit,
    onSaveAll: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        uiLog.meals.forEach { meal ->
            MealCard(
                meal = meal,
                onSave = { onSaveMeal(meal) }
            )
        }

        if (!uiLog.isSaved) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveAll,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryContainer,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SAVE", style = typography.labelSm.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        colors.error.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DISCARD", style = typography.labelSm.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun MealCard(
    meal: UIMeal,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.mealType.uppercase(),
                    style = typography.labelSm.copy(
                        fontFamily = SpaceGroteskFamily,
                        color = colors.primaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (meal.isSaved) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Done,
                            contentDescription = "Saved",
                            tint = colors.primaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "${meal.totalCalories.toInt()} kcal",
                        style = typography.bodySm.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                meal.items.forEach { item ->
                    FoodItemRow(item)
                }
            }

            Divider(color = colors.outlineVariant.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroRow(
                    protein = meal.totalProtein,
                    carbs = meal.totalCarbs,
                    fats = meal.totalFats
                )
            }
        }
    }
}

@Composable
fun FoodItemRow(item: UIFoodItem) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.name,
            style = typography.bodySm,
            color = colors.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${item.grams.toInt()}g",
            style = typography.bodySm.copy(fontFamily = SpaceGroteskFamily),
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
fun MacroRow(protein: Double, carbs: Double, fats: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MacroItem("P", protein, KineticTheme.colors.primaryContainer)
        MacroItem("C", carbs, Color(0xFF4285F4))
        MacroItem("F", fats, Color(0xFFFBBC04))
    }
}

@Composable
fun MacroItem(label: String, value: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${value.toInt()}g",
            style = KineticTheme.typography.labelSm.copy(
                fontSize = 10.sp,
                fontFamily = SpaceGroteskFamily
            ),
            color = KineticTheme.colors.onSurfaceVariant
        )
    }
}
