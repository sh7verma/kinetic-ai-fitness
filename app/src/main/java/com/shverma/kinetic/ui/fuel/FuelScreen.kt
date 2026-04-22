package com.shverma.kinetic.ui.fuel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.components.KineticDataCard
import com.shverma.kinetic.ui.components.KineticProgressBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shverma.kinetic.data.model.Meal
import com.shverma.kinetic.data.model.MealItem
import com.shverma.kinetic.data.model.MealPlan
import com.shverma.kinetic.ui.components.FoodItemRow
import com.shverma.kinetic.ui.components.KPDivider
import com.shverma.kinetic.ui.components.mealAccent
import com.shverma.kinetic.ui.theme.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import com.shverma.kinetic.ui.components.KineticTopAppBar
import com.shverma.kinetic.ui.components.kineticGlow
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.LexendFamily
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily
import com.shverma.kinetic.utils.*
import java.util.Calendar

@Composable
fun FuelScreen(
    viewModel: FuelViewModel = hiltViewModel(),
    onAIChatClick: () -> Unit = {},
    onAddMealClick: () -> Unit = {},
    onEnergyCardClick: () -> Unit = {}
) {
    val colors = KineticTheme.colors
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = colors.background,
        topBar = { KineticTopAppBar() },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = onAIChatClick,
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Chat"
                    )
                }

                FloatingActionButton(
                    onClick = onAddMealClick,
                    containerColor = colors.primaryContainer,
                    contentColor = colors.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Meal"
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
        ) {
            // 2. HERO ENERGY CARD
            item {
                KineticDataCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEnergyCardClick() },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "DAILY ENERGY" label
                            Text(
                                text = "DAILY ENERGY",
                                style = KineticTheme.typography.labelSm,
                                color = colors.onSurfaceVariant
                            )
                            // AI Sync badge (pill with sparkles icon)
                            Surface(
                                color = colors.primaryContainer.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(0.5.dp, colors.primaryContainer.copy(alpha = 0.3f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Sync",
                                        tint = colors.primaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "AI SYNC",
                                        style = KineticTheme.typography.labelSm.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primaryContainer
                                    )
                                }
                            }
                        }
                    },
                    metric = {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Large text: "1,842"
                            Text(
                                text = state.caloriesValue,
                                style = KineticTheme.typography.displayLg,
                                color = colors.onSurface
                            )
                            // Subtext: "KCAL REMAINING"
                            Text(
                                text = "KCAL REMAINING",
                                style = KineticTheme.typography.labelMd.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            // Progress Bar: height 12dp, gradient lime -> soft lime
                            KineticProgressBar(
                                progress = state.caloriesProgress,
                                modifier = Modifier.fillMaxWidth(),
                                trackHeight = 12.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    },
                    unit = {
                        // Bottom Row: EATEN (left) & TARGET (right)
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
                                    text = state.caloriesEaten,
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
                                    text = state.caloriesTarget,
                                    style = KineticTheme.typography.titleMd.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                )
            }

            // 3. AI SUMMARY CARD (Glass Effect)
            item {
                val typography = KineticTheme.typography
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surfaceContainerLow.copy(alpha = 0.7f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Left icon container (rounded box)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Coach",
                                tint = colors.primaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        // Right text column
                        Column {
                            Text(
                                text = "AI Nutrition Summary",
                                style = typography.labelLg.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                            val summaryText = if (state.caloriesProgress > 0.8f) {
                                "You're close to your calorie target. Keep it up!"
                            } else if (state.caloriesProgress > 0) {
                                "Great start on your meals today! Keep hitting your goals."
                            } else {
                                "Log your first meal to see your AI Nutrition Summary."
                            }
                            Text(
                                text = summaryText,
                                style = typography.bodySm,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. MACRO GRID
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroCard(
                        modifier = Modifier.weight(1f),
                        label = "PROTEIN",
                        value = state.proteinValue,
                        percent = state.proteinPercent,
                        color = colors.primaryContainer, // Lime
                        icon = Icons.Default.AutoAwesome
                    )
                    MacroCard(
                        modifier = Modifier.weight(1f),
                        label = "CARBS",
                        value = state.carbsValue,
                        percent = state.carbsPercent,
                        color = colors.secondary, // Cyan
                        icon = Icons.Default.AutoAwesome
                    )
                    MacroCard(
                        modifier = Modifier.weight(1f),
                        label = "FATS",
                        value = state.fatsValue,
                        percent = state.fatsPercent,
                        color = colors.tertiary, // Red/Coral
                        icon = Icons.Default.AutoAwesome
                    )
                }
            }

            // 5. WEEKLY TREND SECTION
            item {
                // Weekly Trend Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceContainerLow)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                        val typography = KineticTheme.typography
                        Column {
                            Text(
                                text = "WEEKLY TREND (KCAL)",
                                style = typography.labelSm.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                // Y-AXIS
                                Column(
                                    modifier = Modifier.fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.End
                                ) {
                                    listOf("3K", "2K", "1K", "0").forEach { label ->
                                        Text(
                                            text = label,
                                            style = typography.labelSm.copy(fontSize = 7.sp),
                                            color = colors.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))

                                // BARS & X-AXIS
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val maxCal = 3000f
                                    val calendar = Calendar.getInstance()
                                    calendar.firstDayOfWeek = Calendar.MONDAY
                                    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                                    // Calendar.MONDAY is 2, TUESDAY is 3, ..., SATURDAY is 7, SUNDAY is 1
                                    // We want 0-indexed where MONDAY is 0
                                    val todayIndex = when(currentDayOfWeek) {
                                        Calendar.MONDAY -> 0
                                        Calendar.TUESDAY -> 1
                                        Calendar.WEDNESDAY -> 2
                                        Calendar.THURSDAY -> 3
                                        Calendar.FRIDAY -> 4
                                        Calendar.SATURDAY -> 5
                                        Calendar.SUNDAY -> 6
                                        else -> 0
                                    }

                                    state.weeklyTrend.forEachIndexed { index, (day, cal) ->
                                        val h = (cal / maxCal).toFloat()
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(8.dp)
                                                    .fillMaxHeight(h.coerceAtLeast(0.01f))
                                                    .drawBehind {
                                                        // Reference line
                                                        drawLine(
                                                            color = Color.White.copy(alpha = 0.05f),
                                                            start = Offset(0f, 0f),
                                                            end = Offset(0f, -size.height / (if (h > 0) h else 1f)),
                                                            strokeWidth = 1f
                                                        )
                                                    }
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(
                                                        brush = Brush.verticalGradient(
                                                            colors = listOf(
                                                                if (index == todayIndex) colors.primaryContainer else colors.secondary,
                                                                if (index == todayIndex) colors.primaryContainer.copy(alpha = 0.5f) else colors.secondary.copy(alpha = 0.5f)
                                                            )
                                                        )
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = day,
                                                style = typography.labelSm.copy(fontSize = 7.sp),
                                                color = colors.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
            }

            // 6. DAILY MEAL PLAN
            state.mealPlan?.let { plan ->
                item {
                    Text(
                        text = "DAILY MEAL PLAN",
                        style = KineticTheme.typography.labelSm.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurfaceVariant
                    )
                }

                items(plan.meals) { meal ->
                    PlannedMealRow(
                        meal = meal,
                        isLogged = state.loggedMealNames.contains(meal.name),
                        onLogClick = { viewModel.onEvent(FuelEvents.LogPlannedMeal(meal)) }
                    )
                }
            }
            }
        }
    }


@Composable
fun PlannedMealRow(meal: Meal, isLogged: Boolean, onLogClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val colors = KineticTheme.colors
    val accent = mealAccent(meal.name)
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
            
            IconButton(
                onClick = onLogClick,
                enabled = !isLogged
            ) {
                Icon(
                    imageVector = if (isLogged) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (isLogged) "Meal Logged" else "Log Meal",
                    tint = if (isLogged) KineticTheme.colors.primary else colors.primaryContainer,
                    modifier = Modifier.size(20.dp)
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
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp)),
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
fun MacroCard(
    modifier: Modifier,
    label: String,
    value: String,
    percent: String,
    color: Color,
    icon: ImageVector
) {
    val typography = KineticTheme.typography
    val colors = KineticTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainerLow)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = percent,
                    style = typography.labelSm,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = typography.labelSm,
                color = colors.onSurfaceVariant
            )
            Text(
                text = value,
                style = typography.titleMd.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHigh)
            ) {
                // Calculate float progress from percent string
                val progressValue = try {
                    percent.replace("%", "").toFloat() / 100f
                } catch (e: Exception) {
                    0f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressValue.coerceIn(0f, 1.05f))
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun MiniCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    val typography = KineticTheme.typography
    val colors = KineticTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainerLow)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = value,
                style = typography.titleMd.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Text(
                text = label,
                style = typography.labelSm,
                color = colors.onSurfaceVariant
            )
        }
    }
}


@Composable
fun FuelItemRow(item: FuelItem, voltColor: Color) {
    val typography = KineticTheme.typography
    val colors = KineticTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceContainerHigh)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = typography.labelLg.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Text(
                text = "${item.category} • ${item.time}",
                style = typography.labelSm,
                color = colors.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${item.calories.formatCalories()} kcal",
                style = typography.labelLg.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Text(
                text = "AI Verified",
                style = typography.labelSm.copy(fontWeight = FontWeight.Bold),
                color = voltColor
            )
        }
    }
}
