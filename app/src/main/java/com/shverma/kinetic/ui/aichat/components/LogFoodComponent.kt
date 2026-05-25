package com.shverma.kinetic.ui.aichat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.shverma.kinetic.ui.components.InlineMacro
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily

@Composable
fun LogFoodComponent(
    uiLog: UILog,
    onLogMeal: (UIMeal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        uiLog.meals.forEach { meal ->
            MealCard(
                meal = meal,
                onLog = { onLogMeal(meal) }
            )
        }
    }
}

@Composable
fun MealCard(
    meal: UIMeal,
    onLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    val containerColor = if (meal.isSaved) colors.background else colors.surfaceContainerLow
    val borderColor = if (meal.isSaved) colors.primaryContainer.copy(alpha = 0.1f) else colors.outlineVariant.copy(alpha = 0.2f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = meal.mealType.uppercase(),
                        style = typography.labelSm.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = if (meal.isSaved) colors.onSurfaceVariant else colors.primaryContainer,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "${meal.totalCalories.toInt()} kcal",
                        style = typography.titleMd.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = colors.onSurface
                        )
                    )
                }

                if (meal.isSaved) {
                    Surface(
                        color = colors.primaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Done,
                                contentDescription = "Saved",
                                tint = colors.primaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "LOGGED",
                                style = typography.labelSm.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryContainer,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                meal.items.forEach { item ->
                    FoodItemRow(item)
                }
            }

            if (!meal.isSaved) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onLog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryContainer,
                        contentColor = colors.onPrimaryFixed
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "LOG",
                        style = typography.labelSm.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFamily
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.outlineVariant.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
fun FoodItemRow(item: UIFoodItem) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = typography.bodyMd,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${item.grams.toInt()}g",
                style = typography.bodyMd,
                color = colors.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InlineMacro("P", "${item.protein.toInt()}g", colors.protein)
            InlineMacro("C", "${item.carbs.toInt()}g", colors.carbs)
            InlineMacro("F", "${item.fats.toInt()}g", colors.fats)
            InlineMacro("K", "${item.calories.toInt()}")
        }
    }
}

@Composable
fun MacroRow(protein: Double, carbs: Double, fats: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MacroItem("P", protein, KineticTheme.colors.primaryContainer)
        MacroItem("C", carbs, KineticTheme.colors.carbs)
        MacroItem("F", fats, KineticTheme.colors.fats)
    }
}

@Composable
fun MacroItem(label: String, value: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = KineticTheme.typography.labelSm.copy(
                fontSize = 10.sp,
                color = KineticTheme.colors.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "${value.toInt()}g",
            style = KineticTheme.typography.labelSm.copy(
                fontSize = 10.sp,
                fontFamily = SpaceGroteskFamily
            ),
            color = KineticTheme.colors.onSurface
        )
    }
}
