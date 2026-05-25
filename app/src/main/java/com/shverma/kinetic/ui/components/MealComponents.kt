package com.shverma.kinetic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.aichat.UIMeal
import com.shverma.kinetic.ui.theme.KineticTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMealBottomSheet(
    meal: UIMeal,
    onDismiss: () -> Unit,
    onSave: (UIMeal) -> Unit
) {
    var editedMeal by remember(meal) { mutableStateOf(meal) }

    // Change itemDensities to mutableState to allow updates on deletion
    var itemDensities by remember(meal) {
        mutableStateOf(meal.items.map { item ->
            if (item.grams > 0) {
                listOf(
                    item.calories / item.grams,
                    item.protein / item.grams,
                    item.carbs / item.grams,
                    item.fats / item.grams
                )
            } else {
                listOf(0.0, 0.0, 0.0, 0.0)
            }
        })
    }

    val sheetState = rememberModalBottomSheetState()
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.onSurfaceVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log ${meal.mealType}",
                    style = typography.titleLg,
                    color = colors.onSurface
                )
                // Display updated total calories for the entire meal
                Text(
                    text = "${editedMeal.totalCalories.toInt()} kcal",
                    style = typography.titleMd,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Edit details below to match what you ate",
                style = typography.bodyMd,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(editedMeal.items) { index, item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceContainer, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        // Top Row: Food Name and Delete Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = item.name,
                                onValueChange = { newName ->
                                    val updatedItems = editedMeal.items.toMutableList()
                                    updatedItems[index] = item.copy(name = newName)
                                    editedMeal = editedMeal.copy(items = updatedItems)
                                },
                                label = { Text("Food Item") },
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    val updatedItems = editedMeal.items.toMutableList()
                                    updatedItems.removeAt(index)

                                    val updatedDensities = itemDensities.toMutableList()
                                    updatedDensities.removeAt(index)
                                    itemDensities = updatedDensities

                                    editedMeal = editedMeal.copy(
                                        items = updatedItems,
                                        totalCalories = updatedItems.sumOf { it.calories },
                                        totalProtein = updatedItems.sumOf { it.protein },
                                        totalCarbs = updatedItems.sumOf { it.carbs },
                                        totalFats = updatedItems.sumOf { it.fats }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Item",
                                    tint = colors.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = if (item.grams == 0.0) "" else item.grams.toInt().toString(),
                            onValueChange = { newGramsStr ->
                                val newGrams = newGramsStr.toDoubleOrNull() ?: 0.0
                                val updatedItems = editedMeal.items.toMutableList()

                                // Recalculate macros for this item based on new weight
                                val density = itemDensities[index]
                                val updatedItem = item.copy(
                                    grams = newGrams,
                                    calories = density[0] * newGrams,
                                    protein = density[1] * newGrams,
                                    carbs = density[2] * newGrams,
                                    fats = density[3] * newGrams
                                )
                                updatedItems[index] = updatedItem

                                // Update meal totals for UI refresh
                                editedMeal = editedMeal.copy(
                                    items = updatedItems,
                                    totalCalories = updatedItems.sumOf { it.calories },
                                    totalProtein = updatedItems.sumOf { it.protein },
                                    totalCarbs = updatedItems.sumOf { it.carbs },
                                    totalFats = updatedItems.sumOf { it.fats }
                                )
                            },
                            label = { Text("Weight (grams)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Show macros for individual food item
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InlineMacro("P", "${item.protein.toInt()}g", colors.protein)
                            InlineMacro("C", "${item.carbs.toInt()}g", colors.carbs)
                            InlineMacro("F", "${item.fats.toInt()}g", colors.fats)
                            InlineMacro("K", "${item.calories.toInt()}")
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KineticSecondaryButton(
                    text = "Discard",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                KineticPrimaryButton(
                    text = "Log Meal",
                    onClick = { onSave(editedMeal) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun InlineMacro(label: String, value: String, valueColor: Color = KineticTheme.colors.onSurface) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = typography.labelSm.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            style = typography.labelSm,
            color = valueColor
        )
    }
}
