package com.shverma.kinetic.ui.fuel

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.data.model.Meal
import com.shverma.kinetic.ui.components.*
import com.shverma.kinetic.ui.theme.*
import com.shverma.kinetic.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayMealPlanScreen(
    onBackClick: () -> Unit,
    viewModel: TodayMealPlanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = KineticTheme.colors
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Meal Plan", "Today's Logs")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Today's Nutrition",
                            style = KineticTheme.typography.titleLg,
                            color = colors.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background
                    )
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = colors.background,
                    contentColor = colors.primary,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = colors.primary
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(color = colors.outlineVariant)
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = KineticTheme.typography.labelLg.copy(
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            selectedContentColor = colors.primary,
                            unselectedContentColor = colors.onSurfaceVariant
                        )
                    }
                }
            }
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 24.dp)
        ) {
            if (selectedTabIndex == 0) {
                // 1. MEAL PLAN SECTION
                item {
                    Text(
                        text = "CURRENT PLAN",
                        style = KineticTheme.typography.labelSm,
                        color = colors.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                if (state.mealPlan != null) {
                    item {
                        DaySummaryCard(plan = state.mealPlan!!)
                    }

                    items(state.mealPlan!!.meals) { meal ->
                        MealPlanItemRow(
                            meal = meal,
                            isLogged = state.loggedMealNames.contains(meal.name)
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surfaceContainer)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No meal plan saved for today.",
                                color = colors.onSurfaceVariant,
                                style = KineticTheme.typography.bodyMd
                            )
                        }
                    }
                }
            } else {
                // 2. TODAY'S LOGGED MEALS SECTION
                item {
                    Text(
                        text = "LOGGED TODAY",
                        style = KineticTheme.typography.labelSm,
                        color = colors.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                if (state.todayMeals.isNotEmpty()) {
                    item {
                        KineticDataCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = {
                                Text(
                                    text = "DAILY ENERGY",
                                    style = KineticTheme.typography.labelSm,
                                    color = colors.onSurfaceVariant
                                )
                            },
                            metric = {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = state.remainingCalories.formatCalories(),
                                        style = KineticTheme.typography.displayLg,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "KCAL REMAINING",
                                        style = KineticTheme.typography.labelMd.copy(fontWeight = FontWeight.Bold),
                                        color = colors.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    KineticProgressBar(
                                        progress = if (state.targetCalories > 0) (state.totalCalories / state.targetCalories).toFloat() else 0f,
                                        modifier = Modifier.fillMaxWidth(),
                                        trackHeight = 12.dp
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Macro progress
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        MacroBarSmall(
                                            label = "PROTEIN",
                                            current = state.totalProtein,
                                            target = state.targetProtein,
                                            color = MealCyan,
                                            modifier = Modifier.weight(1f)
                                        )
                                        MacroBarSmall(
                                            label = "CARBS",
                                            current = state.totalCarbs,
                                            target = state.targetCarbs,
                                            color = MealVolt,
                                            modifier = Modifier.weight(1f)
                                        )
                                        MacroBarSmall(
                                            label = "FATS",
                                            current = state.totalFats,
                                            target = state.targetFats,
                                            color = KineticTertiary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            },
                            unit = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "EATEN",
                                            style = KineticTheme.typography.labelSm,
                                            color = colors.onSurfaceVariant
                                        )
                                        Text(
                                            text = state.totalCalories.formatCalories(),
                                            style = KineticTheme.typography.titleMd.copy(fontWeight = FontWeight.Bold),
                                            color = colors.onSurface
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "TARGET",
                                            style = KineticTheme.typography.labelSm,
                                            color = colors.onSurfaceVariant
                                        )
                                        Text(
                                            text = state.targetCalories.formatCalories(),
                                            style = KineticTheme.typography.titleMd.copy(fontWeight = FontWeight.Bold),
                                            color = colors.secondary
                                        )
                                    }
                                }
                            }
                        )
                    }

                    items(state.todayMeals) { meal ->
                        LoggedMealRow(meal = meal)
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surfaceContainer)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No meals logged yet today.",
                                color = colors.onSurfaceVariant,
                                style = KineticTheme.typography.bodyMd
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealPlanItemRow(meal: Meal, isLogged: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val colors = KineticTheme.colors
    val accent = mealAccent(meal.name)
    val mTotal = meal.items.sumOf { it.proteinG + it.carbsG + it.fatsG }.toDouble()
    val chevron by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "chevron_${meal.name}",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainer)
            .clickable { expanded = !expanded }
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Accent bar
            Box(
                Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent.color),
            )
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(meal.name, color = MealWhite, fontSize = 14.sp, fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold)
                        Text(meal.time, color = MealGray, fontSize = 10.sp, fontFamily = SpaceGroteskFamily)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        val calories = meal.items.sumOf { it.calories }
                        Text(calories.formatCalories(), color = accent.color, fontSize = 17.sp, fontFamily = LexendFamily, fontWeight = FontWeight.Bold, lineHeight = 17.sp)
                        Text("kcal", color = MealGray, fontSize = 9.sp, fontFamily = SpaceGroteskFamily, modifier = Modifier.padding(bottom = 1.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MealCyan))
                        Text("P: ${meal.items.sumOf { it.proteinG }.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MealVolt))
                        Text("C: ${meal.items.sumOf { it.carbsG }.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(KineticTertiary))
                        Text("F: ${meal.items.sumOf { it.fatsG }.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                    }
                }
            }

            if (isLogged) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Logged",
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text("▼", color = MealGray, fontSize = 10.sp, modifier = Modifier.rotate(chevron))
        }

        // Expanded content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(300, easing = FastOutSlowInEasing)),
            exit = shrinkVertically(tween(280, easing = FastOutSlowInEasing)),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceContainerHigh)
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp)),
            ) {
                meal.items.forEachIndexed { i, item ->
                    FoodItemRow(item = item, accentColor = accent.color)
                    if (i < meal.items.lastIndex) KPDivider()
                }
            }
        }
    }
}

@Composable
fun LoggedMealRow(meal: com.shverma.kinetic.data.local.entity.MealEntity) {
    var expanded by remember { mutableStateOf(false) }
    val colors = KineticTheme.colors
    val accent = mealAccent(meal.name)
    val mTotal = meal.items.sumOf { it.proteinG + it.carbsG + it.fatsG }.toDouble()
    val chevron by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "chevron_log_${meal.id}",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerLow)
            .clickable { expanded = !expanded }
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Accent bar
            Box(
                Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent.color),
            )
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(meal.name, color = MealWhite, fontSize = 14.sp, fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold)
                        Text(meal.time, color = MealGray, fontSize = 10.sp, fontFamily = SpaceGroteskFamily)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(meal.totalCalories.formatCalories(), color = accent.color, fontSize = 17.sp, fontFamily = LexendFamily, fontWeight = FontWeight.Bold, lineHeight = 17.sp)
                        Text("kcal", color = MealGray, fontSize = 9.sp, fontFamily = SpaceGroteskFamily, modifier = Modifier.padding(bottom = 1.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MealCyan))
                        Text("P: ${meal.items.sumOf { it.proteinG }.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MealVolt))
                        Text("C: ${meal.items.sumOf { it.carbsG }.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(KineticTertiary))
                        Text("F: ${meal.items.sumOf { it.fatsG }.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                    }
                }
            }
            Text("▼", color = MealGray, fontSize = 10.sp, modifier = Modifier.rotate(chevron))
        }

        // Expanded content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(300, easing = FastOutSlowInEasing)),
            exit = shrinkVertically(tween(280, easing = FastOutSlowInEasing)),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceContainerHigh)
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp)),
            ) {
                meal.items.forEachIndexed { i, item ->
                    FoodItemRow(item = item, accentColor = accent.color)
                    if (i < meal.items.lastIndex) KPDivider()
                }
            }
        }
    }
}

@Composable
fun MacroBarSmall(
    label: String,
    current: Double,
    target: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val progress = if (target > 0) (current / target).toFloat() else 0f
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = KineticTheme.typography.labelSm,
                color = colors.onSurfaceVariant,
                fontSize = 8.sp
            )
            Text(
                text = "${current.toInt()}g",
                style = KineticTheme.typography.labelSm.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        KineticProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackHeight = 4.dp
        )
    }
}