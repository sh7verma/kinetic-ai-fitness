package com.shverma.kinetic.ui.diet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.data.model.ai.AIDietPlan
import com.shverma.kinetic.data.model.ai.AIMeal
import com.shverma.kinetic.ui.aichat.UIMeal
import com.shverma.kinetic.ui.components.KineticPrimaryButton
import com.shverma.kinetic.ui.components.KineticSecondaryButton
import com.shverma.kinetic.ui.components.KineticTopAppBar
import com.shverma.kinetic.ui.components.EditMealBottomSheet
import com.shverma.kinetic.ui.components.InlineMacro
import com.shverma.kinetic.ui.fuel.MacroCard
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.LocalKineticTypography
import com.shverma.kinetic.utils.formatGoalName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietPlanScreen(
    onBackClick: () -> Unit,
    onCreatePlanClick: () -> Unit,
    viewModel: DietPlanViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("ACTIVE PLAN", "HISTORY")
    val dietPlan by viewModel.dietPlan.collectAsState(initial = null)
    val userProfile by viewModel.userProfile.collectAsState(initial = null)
    val foodLogs by viewModel.foodLogs.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedMeal by remember { mutableStateOf<UIMeal?>(null) }

    Scaffold(
        topBar = { KineticTopAppBar(onBackClick = onBackClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePlanClick,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Diet Plan")
            }
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = colors.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = colors.onSurface,
                        height = 2.dp
                    )
                },
                divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.08f)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = typography.labelLg.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedTab == index) colors.onSurface else colors.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> DietPlanTab(
                    dietPlan = dietPlan,
                    userProfile = userProfile,
                    onCreatePlanClick = onCreatePlanClick,
                    onMealClick = { meal ->
                        selectedMeal = meal
                        showBottomSheet = true
                    }
                )
                1 -> HistoryTab(
                    selectedDate = selectedDate,
                    foodLogs = foodLogs,
                    onDateSelected = { viewModel.onDateSelected(it) }
                )
            }
        }
    }

    if (showBottomSheet && selectedMeal != null) {
        EditMealBottomSheet(
            meal = selectedMeal!!,
            onDismiss = { showBottomSheet = false },
            onSave = { updatedMeal ->
                viewModel.logMeal(updatedMeal)
                showBottomSheet = false
            }
        )
    }
}


@Composable
fun HistoryTab(
    selectedDate: Long,
    foodLogs: List<UIMeal>,
    onDateSelected: (Long) -> Unit
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    // Compute totals from the day's logs
    val totalCal = foodLogs.sumOf { it.totalCalories.toInt() }
    val totalP = foodLogs.sumOf { it.totalProtein.toInt() }
    val totalC = foodLogs.sumOf { it.totalCarbs.toInt() }
    val totalF = foodLogs.sumOf { it.totalFats.toInt() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DatePickerHorizontal(selectedDate, onDateSelected)
        }

        if (foodLogs.isNotEmpty()) {
            item {
                TotalIntakeCard(
                    totalCalories = totalCal,
                    protein = totalP,
                    carbs = totalC,
                    fats = totalF,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        if (foodLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No food logs for this date",
                        style = typography.bodyLg,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        } else {
            itemsIndexed(foodLogs) { index, meal ->
                val accent = when (index % 4) {
                    0 -> colors.primary
                    1 -> colors.secondary
                    2 -> colors.tertiary
                    else -> Color(0xFF00E3FD)
                }
                MealLogCard(
                    meal = meal,
                    accentColor = accent,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun TotalIntakeCard(
    totalCalories: Int,
    protein: Int,
    carbs: Int,
    fats: Int,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOTAL INTAKE",
                    style = typography.labelSm,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%,d".format(totalCalories),
                        style = typography.displaySm.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "KCAL",
                        style = typography.labelMd.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                MacroSummaryLine("P", "${protein}g", colors.onSurfaceVariant, colors.protein)
                Spacer(Modifier.height(4.dp))
                MacroSummaryLine("C", "${carbs}g", colors.onSurfaceVariant, colors.carbs)
                Spacer(Modifier.height(4.dp))
                MacroSummaryLine("F", "${fats}g", colors.onSurfaceVariant, colors.fats)
            }
        }
    }
}

@Composable
private fun MacroSummaryLine(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    val typography = KineticTheme.typography
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = typography.labelMd, color = labelColor)
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = typography.bodyMd.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}




@Composable
fun MealLogCard(
    meal: UIMeal,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
    ) {
        // Left edge accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Header row: meal type + kcal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = meal.mealType.uppercase(),
                    style = typography.titleLg.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.onSurface
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${meal.totalCalories.toInt()}",
                        style = typography.titleLg.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "KCAL",
                        style = typography.labelSm.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Nested items card (darker bg, thin dividers between rows)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.background)
                    .padding(horizontal = 16.dp)
            ) {
                meal.items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            style = typography.bodyLg,
                            color = colors.onSurface
                        )
                        Text(
                            text = "${item.grams.toInt()}g",
                            style = typography.bodyMd,
                            color = colors.onSurfaceVariant
                        )
                    }
                    if (index < meal.items.size - 1) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bottom macro row: label above value, left-aligned
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                MacroStat("PROTEIN", "${meal.totalProtein.toInt()}g", colors.protein)
                MacroStat("CARBS", "${meal.totalCarbs.toInt()}g", colors.carbs)
                MacroStat("FAT", "${meal.totalFats.toInt()}g", colors.fats)
            }
        }
    }
}

@Composable
private fun MacroStat(label: String, value: String, valueColor: Color = KineticTheme.colors.onSurface) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    Column {
        Text(
            text = label,
            style = typography.labelSm,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = typography.titleSm.copy(fontWeight = FontWeight.ExtraBold),
            color = valueColor
        )
    }
}


@Composable
fun DatePickerHorizontal(selectedDate: Long, onDateSelected: (Long) -> Unit) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    val dates = remember {
        (0..30).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        val index = dates.indexOf(selectedDate)
        if (index >= 0) {
            listState.scrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val dateObj = Date(date)
            val dayFormat = SimpleDateFormat("EEE", LocalLocale.current.platformLocale)
            val dateFormat = SimpleDateFormat("d", LocalLocale.current.platformLocale)
            val monthFormat = SimpleDateFormat("MMM", LocalLocale.current.platformLocale)

            Column(
                modifier = Modifier
                    .size(width = 72.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) colors.primary else colors.surfaceContainerHigh)
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = monthFormat.format(dateObj).uppercase(),
                    style = typography.labelSm.copy(fontWeight = FontWeight.Bold, fontSize = typography.labelSm.fontSize * 0.8),
                    color = if (isSelected) Color.Black else colors.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(dateObj),
                    style = typography.titleLg.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isSelected) Color.Black else colors.onSurface
                )
                Text(
                    text = dayFormat.format(dateObj).uppercase(),
                    style = typography.labelSm.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color.Black else colors.onSurfaceVariant
                )
            }
        }
    }
}



@Composable
fun DietPlanTab(
    dietPlan: DietPlanUi?,
    userProfile: UserProfileData?,
    onCreatePlanClick: () -> Unit,
    onMealClick: (UIMeal) -> Unit
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    if (dietPlan == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            KineticPrimaryButton(
                text = "Create Diet Plan",
                onClick = onCreatePlanClick
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily targets summary — mirrors the TOTAL INTAKE card structure
        userProfile?.targetCaloriesData?.let { targets ->
            item {
                DailyTargetsCard(
                    targetCalories = targets.targetCalories.toInt(),
                    protein = targets.proteinG.toInt(),
                    carbs = targets.carbsG.toInt(),
                    fats = targets.fatsG.toInt()
                )
            }
        }

        itemsIndexed(dietPlan.meals) { index, meal ->
            val accent = when (index % 4) {
                0 -> colors.primary
                1 -> colors.secondary
                2 -> colors.tertiary
                else -> Color(0xFF00E3FD)
            }
            MealPlanCard(
                meal = meal,
                accentColor = accent,
                modifier = Modifier.clickable { onMealClick(meal) }
            )
        }
    }
}

@Composable
fun DailyTargetsCard(
    targetCalories: Int,
    protein: Int,
    carbs: Int,
    fats: Int,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DAILY TARGET",
                    style = typography.labelSm,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%,d".format(targetCalories),
                        style = typography.displaySm.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "KCAL",
                        style = typography.labelMd.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                MacroSummaryLine("P", "${protein}g", colors.onSurfaceVariant, colors.protein)
                Spacer(Modifier.height(4.dp))
                MacroSummaryLine("C", "${carbs}g", colors.onSurfaceVariant, colors.carbs)
                Spacer(Modifier.height(4.dp))
                MacroSummaryLine("F", "${fats}g", colors.onSurfaceVariant, colors.fats)
            }
        }
    }
}

@Composable
fun MealPlanCard(
    meal: UIMeal,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .height(IntrinsicSize.Min)
    ) {
        // Left edge accent
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Header: meal type + total kcal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = meal.mealType.uppercase(),
                    style = typography.titleLg.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.onSurface
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${meal.totalCalories.toInt()}",
                        style = typography.titleLg.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "KCAL",
                        style = typography.labelSm.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Item count subtitle (replaces the time field used in history)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${meal.items.size} ITEMS",
                style = typography.labelSm,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // Nested items card with per-item macros
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.background)
                    .padding(horizontal = 16.dp)
            ) {
                meal.items.forEachIndexed { index, item ->
                    Column(modifier = Modifier.padding(vertical = 14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                style = typography.bodyLg,
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InlineMacro("P", "${item.protein.toInt()}g", colors.protein)
                            InlineMacro("C", "${item.carbs.toInt()}g", colors.carbs)
                            InlineMacro("F", "${item.fats.toInt()}g", colors.fats)
                            InlineMacro("K", "${item.calories.toInt()}")
                        }
                    }
                    if (index < meal.items.size - 1) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bottom totals — label above value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                MacroStat("PROTEIN", "${meal.totalProtein.toInt()}g", colors.protein)
                MacroStat("CARBS", "${meal.totalCarbs.toInt()}g", colors.carbs)
                MacroStat("FAT", "${meal.totalFats.toInt()}g", colors.fats)
            }
        }
    }
}


