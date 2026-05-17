package com.shverma.kinetic.ui.diet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.ui.components.KineticPrimaryButton
import com.shverma.kinetic.ui.components.KineticTopAppBar
import com.shverma.kinetic.ui.theme.*

@Composable
fun CreateDietPlanScreen(
    onBackClick: () -> Unit,
    viewModel: CreateDietPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            KineticTopAppBar(onBackClick = onBackClick)
        },
        containerColor = KineticBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is DietPlanUiState.Loading -> {
                    LoadingState()
                }
                is DietPlanUiState.Success -> {
                    if (state.isSaved) {
                        LaunchedEffect(Unit) {
                            onBackClick()
                        }
                    }
                    EditorState(
                        plan = state.plan,
                        isSaved = state.isSaved,
                        onSummaryChange = viewModel::updateSummary,
                        onAddFood = viewModel::addFoodItem,
                        onFoodChange = viewModel::updateFoodItem,
                        onFoodDelete = viewModel::removeFoodItem,
                        onSave = viewModel::savePlan
                    )
                }
                is DietPlanUiState.Error -> {
                    ErrorState(message = state.message, onRetry = { viewModel.createDietPlan() })
                }
            }
        }
    }
}


@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = KineticPrimaryContainer)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Generating your meal plan...",
            color = KineticOnBackground,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = Color.Red)
        Spacer(modifier = Modifier.height(16.dp))
        KineticPrimaryButton(text = "Retry", onClick = onRetry)
    }
}

@Composable
fun EditorState(
    plan: DietPlanUiModel,
    isSaved: Boolean,
    onSummaryChange: (String, String, String, String) -> Unit,
    onAddFood: (String) -> Unit,
    onFoodChange: (String, String, String, String, String) -> Unit,
    onFoodDelete: (String, String) -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SummarySection(
            calories = plan.calories,
            protein = plan.protein,
            carbs = plan.carbs,
            fat = plan.fat,
            enabled = !isSaved,
            onValueChange = onSummaryChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(plan.meals, key = { it.id }) { meal ->
                MealCard(
                    meal = meal,
                    readOnly = isSaved,
                    onAddFood = { onAddFood(meal.id) },
                    onFoodChange = { foodId, name, qty, unit ->
                        onFoodChange(meal.id, foodId, name, qty, unit)
                    },
                    onFoodDelete = { foodId -> onFoodDelete(meal.id, foodId) }
                )
            }
        }

        if (!isSaved) {
            Spacer(modifier = Modifier.height(16.dp))

            KineticPrimaryButton(
                text = "Save Plan",
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SummarySection(
    calories: String,
    protein: String,
    carbs: String,
    fat: String,
    enabled: Boolean = true,
    onValueChange: (String, String, String, String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KineticSurfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryField("Calories", calories, "kcal", enabled) { onValueChange(it, protein, carbs, fat) }
            SummaryField("Protein", protein, "g", enabled) { onValueChange(calories, it, carbs, fat) }
            SummaryField("Carbs", carbs, "g", enabled) { onValueChange(calories, protein, it, fat) }
            SummaryField("Fat", fat, "g", enabled) { onValueChange(calories, protein, carbs, it) }
        }
    }
}

@Composable
fun SummaryField(label: String, value: String, unit: String, enabled: Boolean = true, onValueChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = KineticOnSurfaceVariant, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            IntrinsicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.widthIn(min = 30.dp, max = 60.dp)
            )
            Text(text = unit, color = KineticOnSurfaceVariant, fontSize = 10.sp, modifier = Modifier.padding(start = 2.dp))
        }
    }
}

@Composable
fun IntrinsicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = if (enabled) KineticPrimary else KineticOnSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(KineticPrimary)
    )
}

@Composable
fun MealCard(
    meal: MealUiModel,
    readOnly: Boolean = false,
    onAddFood: () -> Unit,
    onFoodChange: (String, String, String, String) -> Unit,
    onFoodDelete: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KineticSurfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = meal.name,
                color = KineticPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            meal.items.forEachIndexed { index, item ->
                FoodItemCard(
                    item = item,
                    readOnly = readOnly,
                    onValueChange = { name, qty, unit -> onFoodChange(item.id, name, qty, unit) },
                    onDelete = { onFoodDelete(item.id) },
                    isNewRow = !readOnly && index == meal.items.lastIndex && item.name.isEmpty() && item.quantity.isEmpty()
                )
                if (index < meal.items.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            if (!readOnly) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onAddFood,
                    colors = ButtonDefaults.textButtonColors(contentColor = KineticPrimaryContainer)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Food")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Food")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemCard(
    item: FoodItemUiModel,
    readOnly: Boolean = false,
    onValueChange: (String, String, String) -> Unit,
    onDelete: () -> Unit,
    isNewRow: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isNewRow) {
        if (isNewRow) {
            focusRequester.requestFocus()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = KineticSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, KineticOutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = item.name,
                onValueChange = { onValueChange(it, item.quantity, item.unit) },
                readOnly = readOnly,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text("Food name", fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = KineticOnSurface,
                    unfocusedTextColor = KineticOnSurface,
                    disabledTextColor = KineticOnSurface
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            TextField(
                value = item.quantity,
                onValueChange = { onValueChange(item.name, it, item.unit) },
                readOnly = readOnly,
                modifier = Modifier.width(60.dp),
                placeholder = { Text("Qty", fontSize = 14.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = KineticOnSurface,
                    unfocusedTextColor = KineticOnSurface,
                    disabledTextColor = KineticOnSurface
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            UnitDropdown(
                selectedUnit = item.unit,
                enabled = !readOnly,
                onUnitSelected = { onValueChange(item.name, item.quantity, it) }
            )

            if (!readOnly) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UnitDropdown(selectedUnit: String, enabled: Boolean = true, onUnitSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val units = listOf("g", "ml", "pcs")

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedUnit, color = KineticPrimaryContainer, fontSize = 14.sp)
            if (enabled) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = KineticPrimaryContainer, modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(KineticSurfaceContainerHigh)
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit, color = KineticOnSurface) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Helper for BasicTextField with custom cursor and focus
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    cursorBrush: androidx.compose.ui.graphics.Brush = SolidColor(Color.White)
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        cursorBrush = cursorBrush
    )
}
