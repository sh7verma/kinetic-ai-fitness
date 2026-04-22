package com.shverma.kinetic.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.data.model.*
import com.shverma.kinetic.ui.theme.*
import com.shverma.kinetic.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SaveState {
    IDLE, SAVING, SAVED
}

data class MealAccent(val color: Color)

@Composable
fun mealAccent(name: String): MealAccent {
    val colors = KineticTheme.colors
    val color = when (name.lowercase()) {
        "breakfast" -> KineticSecondary
        "lunch" -> colors.primaryContainer
        "dinner" -> KineticTertiary
        else -> MealVolt
    }
    return MealAccent(color)
}

/**
 * Case 1 — AI generated a full day meal plan.
 */
@Composable
fun MealPlanCard(
    plan: MealPlan,
    onSaveMeal: suspend (Meal) -> Unit,
    onSaveAll: suspend () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val colors = KineticTheme.colors
    val mealStates = remember { mutableStateMapOf<String, SaveState>().apply { plan.meals.forEach { put(it.name, SaveState.IDLE) } } }
    var allSaved by remember { mutableStateOf(false) }
    var savingAll by remember { mutableStateOf(false) }
    var expandedMeal by remember { mutableStateOf<String?>(null) }
    var toastState by remember { mutableStateOf<Pair<Boolean, String>>(false to "") }

    fun flash(text: String) {
        scope.launch {
            toastState = true to text
            delay(2500)
            toastState = false to text
        }
    }

    fun saveMeal(meal: Meal) {
        if (mealStates[meal.name] != SaveState.IDLE) return
        scope.launch {
            mealStates[meal.name] = SaveState.SAVING
            onSaveMeal(meal)
            mealStates[meal.name] = SaveState.SAVED
            flash("${meal.name.uppercase()} ADDED")
        }
    }

    fun saveAll() {
        if (savingAll || allSaved) return
        scope.launch {
            savingAll = true
            plan.meals.forEachIndexed { i, meal ->
                delay(150L + i * 160L)
                mealStates[meal.name] = SaveState.SAVED
            }
            onSaveAll()
            savingAll = false
            allSaved = true
            flash("FULL DAY PLAN SAVED")
        }
    }

    val savedCount = mealStates.values.count { it == SaveState.SAVED }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AiCoachLabel(text = "AI Coach · Meal Plan")
            AiTextBubble(
                text = "Here's your full day plan for muscle gain — ${plan.totalCalories.formatCal()} kcal across ${plan.meals.size} meals. Tap a meal to review, then add individually or save the whole day.",
            )

            // ── Day summary ──────────────────────────────────────────────────
            DaySummaryCard(plan = plan)

            // ── Meal list ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainer)
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp)),
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${plan.meals.size} MEALS",
                        color = MealDescription,
                        fontSize = 11.sp,
                        fontFamily = SpaceGroteskFamily,
                        letterSpacing = 0.9.sp,
                    )
                    // Progress dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        plan.meals.forEach { meal ->
                            val dotColor by animateColorAsState(
                                targetValue = if (mealStates[meal.name] == SaveState.SAVED) MealVolt else colors.surfaceContainerHighest,
                                animationSpec = tween(300),
                                label = "dot_${meal.name}",
                            )
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(dotColor),
                            )
                        }
                        Text(
                            "$savedCount/${plan.meals.size}",
                            color = MealGray,
                            fontSize = 10.sp,
                            fontFamily = SpaceGroteskFamily,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }

                // Meal rows
                plan.meals.forEach { meal ->
                    KPDivider()
                    MealRow(
                        meal = meal,
                        state = mealStates[meal.name] ?: SaveState.IDLE,
                        expanded = expandedMeal == meal.name,
                        onToggle = { expandedMeal = if (expandedMeal == meal.name) null else meal.name },
                        onSave = { saveMeal(meal) },
                    )
                }

                // Save all footer
                KPDivider()
                SaveAllFooter(allSaved = allSaved, saving = savingAll, onClick = { saveAll() })
            }

            DiscardLink(label = "Discard Plan", onClick = onDiscard)
        }

        KPToast(
            visible = toastState.first,
            text = toastState.second,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Case 2 — User typed a single food entry e.g. "had 100g of chicken breast".
 */
@Composable
fun QuickLogCard(
    entry: SingleLogResponse,
    onSave: suspend () -> Unit,
    onEdit: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val colors = KineticTheme.colors
    var saveState by remember { mutableStateOf(SaveState.IDLE) }
    var showToast by remember { mutableStateOf(false) }

    val accent = mealAccent(entry.entry.mealTime)

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AiCoachLabel(text = "AI Coach · Quick Log", color = MealVolt)
            AiTextBubble(text = "Got it — logged. Quick protein hit. Review below and confirm.")

            // ── Main card ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainer)
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp)),
            ) {
                // Header: calorie ring + food name + tags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CalorieRing(calories = entry.entry.calories)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = entry.entry.food,
                            color = MealWhite,
                            fontSize = 17.sp,
                            fontFamily = LexendFamily,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Quantity tag
                            QuietTag(entry.entry.quantity)
                            // Meal time tag (coloured)
                            MealTimeTag(entry.entry.mealTime, accent)
                        }
                    }
                }
                KPDivider()

                // Macro chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        Triple("Protein", entry.entry.proteinG, MealCyan),
                        Triple("Carbs",   entry.entry.carbsG,   MealVolt),
                        Triple("Fat",     entry.entry.fatsG,    KineticTertiary),
                    ).forEach { (label, value, color) ->
                        MacroChip(label, value, color, Modifier.weight(1f))
                    }
                }
                KPDivider()

                // Confidence + note
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceContainerHigh)
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ConfidencePips(confidence = entry.entry.confidence)
                    Text(
                        text = entry.entry.note,
                        color = MealGray,
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSansFamily,
                        lineHeight = 18.sp,
                    )
                }
                KPDivider()

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GhostButton(label = "Edit", onClick = onEdit, modifier = Modifier.weight(1f))
                    SaveButton(
                        state = saveState,
                        idleLabel = "+ Log This",
                        savedLabel = "Logged",
                        accentColor = MealVolt,
                        onClick = {
                            scope.launch {
                                saveState = SaveState.SAVING
                                onSave()
                                saveState = SaveState.SAVED
                                showToast = true
                                delay(2500)
                                showToast = false
                            }
                        },
                        modifier = Modifier.weight(2f),
                    )
                }
            }

            DiscardLink(label = "Discard", onClick = onDiscard)
        }

        KPToast(
            visible = showToast,
            text = "MEAL LOGGED",
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Case 3 — User typed a message containing multiple meals in one go.
 */
@Composable
fun MultiLogCard(
    data: MultiLogResponse,
    onSaveEntry: suspend (LogEntry) -> Unit,
    onSaveAll: suspend () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val entryStates = remember {
        mutableStateMapOf<Int, SaveState>().apply { data.entries.indices.forEach { put(it, SaveState.IDLE) } }
    }
    var savingAll by remember { mutableStateOf(false) }
    var allSaved by remember { mutableStateOf(false) }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var toastState by remember { mutableStateOf(false to "") }

    fun flash(text: String) {
        scope.launch {
            toastState = true to text
            delay(2500)
            toastState = false to text
        }
    }

    fun saveEntry(index: Int) {
        if (entryStates[index] != SaveState.IDLE) return
        scope.launch {
            entryStates[index] = SaveState.SAVING
            onSaveEntry(data.entries[index])
            entryStates[index] = SaveState.SAVED
            flash("${data.entries[index].mealTime.uppercase()} LOGGED")
        }
    }

    fun saveAll() {
        if (savingAll || allSaved) return
        scope.launch {
            savingAll = true
            data.entries.forEachIndexed { i, entry ->
                delay(150L + i * 200L)
                entryStates[i] = SaveState.SAVED
            }
            onSaveAll()
            savingAll = false
            allSaved = true
            flash("ALL MEALS LOGGED")
        }
    }

    val allDone = entryStates.values.all { it == SaveState.SAVED }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AiCoachLabel(text = "AI Coach · Multi-Meal Log", color = Color(0xFFFFBF00)) // Amber
            AiTextBubble(
                text = "I found ${data.entries.size} meals in your message. Review each one below — save individually or log everything at once.",
            )

            // Parsed-from quote
            ParsedFromBubble(text = data.parsedFrom)

            // ── Summary strip + entries ──────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                // Summary header (top rounded)
                MultiLogSummaryHeader(data = data)

                // Entry cards (square top, square bottom — sandwiched)
                data.entries.forEachIndexed { index, entry ->
                    LogEntryRow(
                        entry = entry,
                        index = index,
                        state = entryStates[index] ?: SaveState.IDLE,
                        expanded = expandedIndex == index,
                        onToggle = { expandedIndex = if (expandedIndex == index) null else index },
                        onSave = { saveEntry(index) },
                    )
                }

                // Save all footer (bottom rounded)
                MultiLogSaveAllFooter(
                    allDone = allDone,
                    saving = savingAll,
                    onClick = { saveAll() },
                )
            }

            DiscardLink(label = "Discard All", onClick = onDiscard)
        }

        KPToast(
            visible = toastState.first,
            text = toastState.second,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─── Day Summary Card ────────────────────────────────────────────────────────

@Composable
fun DaySummaryCard(plan: MealPlan) {
    val colors = KineticTheme.colors
    val totalMacro = (plan.macros.proteinG + plan.macros.carbsG + plan.macros.fatsG).toDouble()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp)),
    ) {
        // Calories + goal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = plan.date,
                    color = MealGray,
                    fontSize = 10.sp,
                    fontFamily = SpaceGroteskFamily,
                    letterSpacing = 0.9.sp,
                )
                Text(
                    text = "Full Day Plan",
                    color = MealWhite,
                    fontSize = 18.sp,
                    fontFamily = LexendFamily,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(Modifier.size(5.dp).clip(RoundedCornerShape(1.dp)).background(MealVolt))
                    Text(plan.goal.uppercase().replace("_", " "), color = MealVolt, fontSize = 11.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 0.7.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${plan.totalCalories}",
                    color = MealVolt,
                    fontSize = 30.sp,
                    fontFamily = LexendFamily,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp,
                )
                Text("KCAL / DAY", color = MealGray, fontSize = 10.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 0.8.sp)
            }
        }
        KPDivider()

        // Macro chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MacroChip("Protein", plan.macros.proteinG, MealCyan, Modifier.weight(1f))
            MacroChip("Carbs",   plan.macros.carbsG,   MealVolt, Modifier.weight(1f))
            MacroChip("Fats",    plan.macros.fatsG,    KineticTertiary, Modifier.weight(1f))
        }
    }
}

// ─── Meal Row ────────────────────────────────────────────────────────────────

@Composable
fun MealRow(
    meal: Meal,
    state: SaveState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSave: () -> Unit,
) {
    val colors = KineticTheme.colors
    val accent = mealAccent(meal.name)
    val mTotal = meal.items.sumOf { it.proteinG + it.carbsG + it.fatsG }.toDouble()
    val chevron by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "chevron_${meal.name}",
    )

    Column {
        // Header row — always visible, tappable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle,
                )
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
                        Text(meal.time, color = MealGray,   fontSize = 10.sp, fontFamily = SpaceGroteskFamily)
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
            Text("▼", color = MealGray, fontSize = 10.sp, modifier = Modifier.rotate(chevron))
        }

        // Expanded content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(300, easing = FastOutSlowInEasing)),
            exit  = shrinkVertically(tween(280, easing = FastOutSlowInEasing)),
        ) {
            Column(modifier = Modifier.padding(bottom = 13.dp)) {
                // Items list
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceContainerHigh)
                        .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp)),
                ) {
                    meal.items.forEachIndexed { i, item ->
                        FoodItemRow(item = item, accentColor = accent.color)
                        if (i < meal.items.lastIndex) KPDivider()
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GhostButton(label = "Edit", onClick = {}, modifier = Modifier.weight(1f))
                    SaveButton(
                        state = state,
                        idleLabel = "+ Add ${meal.name}",
                        savedLabel = "${meal.name} Added",
                        accentColor = accent.color,
                        onClick = onSave,
                        modifier = Modifier.weight(2f),
                    )
                }
            }
        }
    }
}

// ─── Helper Components ────────────────────────────────────────────────────────

@Composable
fun AiCoachLabel(text: String, color: Color = MealVolt) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(text.uppercase(), color = color, fontSize = 10.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
    }
}

@Composable
fun AiTextBubble(text: String) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(14.dp)
    ) {
        Text(text = text, color = MealWhite, fontSize = 13.sp, fontFamily = PlusJakartaSansFamily, lineHeight = 20.sp)
    }
}

@Composable
fun MacroChip(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    val colors = KineticTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceContainerHigh)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label.uppercase(), color = MealGray, fontSize = 8.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 0.5.sp)
        Text(value.formatMacroGramsOneDecimal().uppercase(), color = color, fontSize = 13.sp, fontFamily = LexendFamily, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MacroBar(label: String, value: Double, total: Double, color: Color, slim: Boolean = false, modifier: Modifier = Modifier) {
    val colors = KineticTheme.colors
    val progress = if (total > 0) (value / total).toFloat() else 0f
    
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(if (slim) 3.dp else 5.dp)) {
        if (!slim) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = MealDescription, fontSize = 9.sp, fontFamily = SpaceGroteskFamily)
                Text(value.formatMacroGramsOneDecimal(), color = MealWhite, fontSize = 9.sp, fontFamily = SpaceGroteskFamily)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (slim) 3.dp else 4.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun KPDivider() {
    val colors = KineticTheme.colors
    HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)
}

@Composable
fun GhostButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KineticTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = MealWhite, fontSize = 12.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SaveButton(
    state: SaveState,
    idleLabel: String,
    savedLabel: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KineticTheme.colors
    val isSaving = state == SaveState.SAVING
    val isSaved = state == SaveState.SAVED
    
    val bg = when {
        isSaved -> accentColor.copy(alpha = 0.1f)
        isSaving -> accentColor.copy(alpha = 0.2f)
        else -> accentColor
    }
    val contentColor = if (isSaved || isSaving) accentColor else Color(0xFF111111)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .then(if (isSaved || isSaving) Modifier.border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(11.dp)) else Modifier)
            .clickable(enabled = !isSaving && !isSaved, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isSaving) "Saving..." else if (isSaved) savedLabel else idleLabel,
            color = contentColor,
            fontSize = 12.sp,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FoodItemRow(item: MealItem, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(accentColor))
        Column(Modifier.weight(1f)) {
            Text(item.food, color = MealWhite, fontSize = 13.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium)
            Text(item.quantity, color = MealGray, fontSize = 10.sp, fontFamily = SpaceGroteskFamily)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${item.calories.formatCalories()} kcal", color = MealDescription, fontSize = 11.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("P: ${item.proteinG.formatMacroGrams()}", color = MealCyan, fontSize = 9.sp, fontFamily = SpaceGroteskFamily)
                Text("C: ${item.carbsG.formatMacroGrams()}", color = MealVolt, fontSize = 9.sp, fontFamily = SpaceGroteskFamily)
                Text("F: ${item.fatsG.formatMacroGrams()}", color = KineticTertiary, fontSize = 9.sp, fontFamily = SpaceGroteskFamily)
            }
        }
    }
}

@Composable
fun DiscardLink(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = MealGray, fontSize = 11.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 0.5.sp)
    }
}

@Composable
fun SaveAllFooter(allSaved: Boolean, saving: Boolean, onClick: () -> Unit) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
    ) {
        if (allSaved) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(MealVolt))
                Spacer(Modifier.width(8.dp))
                Text("FULL DAY PLAN SAVED", color = MealVolt, fontSize = 12.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 1.sp)
            }
        } else {
            val bg by animateColorAsState(if (saving) MealVolt.copy(alpha = 0.15f) else MealVolt, tween(250), label = "saveAllBg")
            val textColor = if (saving) MealVolt else Color(0xFF111111)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(bg)
                    .border(1.dp, if (saving) MealVolt.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(11.dp))
                    .clickable(enabled = !saving, onClick = onClick)
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (saving) "Saving Plan…" else "⚡ Save Full Day Plan",
                    color = textColor,
                    fontSize = 13.sp,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

@Composable
fun KPToast(visible: Boolean, text: String, modifier: Modifier = Modifier) {
    val colors = KineticTheme.colors
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.padding(bottom = 28.dp),
        enter = fadeIn(tween(250)) + slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 2 },
        exit  = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 },
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceContainerHigh)
                .border(1.dp, MealVolt.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(MealVolt))
            Text(text, color = MealVolt, fontSize = 11.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 0.7.sp)
        }
    }
}

@Composable
private fun CalorieRing(calories: Double) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(MealVolt.copy(alpha = 0.1f))
            .border(2.dp, MealVolt.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = calories.formatCalories(),
                color = MealVolt,
                fontSize = 16.sp,
                fontFamily = LexendFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
            )
            Text(
                text = "KCAL",
                color = MealGray,
                fontSize = 8.sp,
                fontFamily = SpaceGroteskFamily,
                letterSpacing = 0.6.sp,
            )
        }
    }
}

@Composable
private fun QuietTag(text: String) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            color = MealDescription,
            fontSize = 10.sp,
            fontFamily = SpaceGroteskFamily,
            letterSpacing = 0.6.sp,
        )
    }
}

@Composable
private fun MealTimeTag(text: String, accent: MealAccent) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(accent.color.copy(alpha = 0.1f))
            .border(1.dp, accent.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text.uppercase(),
            color = accent.color,
            fontSize = 9.sp,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
        )
    }
}

@Composable
fun ConfidencePips(confidence: Int, pipColor: Color = MealVolt) {
    val filledPips = ((confidence / 100f) * 5).toInt().coerceIn(0, 5)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(5) { i ->
                Box(
                    Modifier
                        .size(width = 12.dp, height = 3.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (i < filledPips) pipColor else KineticTheme.colors.surfaceContainerHighest)
                )
            }
        }
        Text("${confidence}% MATCH", color = MealGray, fontSize = 9.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun ParsedFromBubble(text: String) {
    val colors = KineticTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 13.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "PARSED",
            color = MealGray,
            fontSize = 10.sp,
            fontFamily = SpaceGroteskFamily,
            letterSpacing = 0.7.sp,
            modifier = Modifier.padding(top = 1.dp),
        )
        Text(
            text = "\"$text\"",
            color = MealDescription,
            fontSize = 12.sp,
            fontFamily = PlusJakartaSansFamily,
            lineHeight = 18.sp,
            fontStyle = FontStyle.Italic,
        )
    }
}

@Composable
private fun MultiLogSummaryHeader(data: MultiLogResponse) {
    val colors = KineticTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(colors.surfaceContainer)
            .border(
                1.dp, colors.outlineVariant,
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${data.entries.size} MEALS DETECTED",
                color = MealDescription,
                fontSize = 11.sp,
                fontFamily = SpaceGroteskFamily,
                letterSpacing = 0.8.sp,
            )
            ConfidencePips(confidence = data.confidence, pipColor = Color(0xFFFFBF00))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${data.totalCalories}",
                color = MealVolt,
                fontSize = 26.sp,
                fontFamily = LexendFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp,
            )
            Text("TOTAL KCAL", color = MealGray, fontSize = 9.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 0.8.sp)
        }
    }
}

@Composable
private fun LogEntryRow(
    entry: LogEntry,
    index: Int,
    state: SaveState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSave: () -> Unit,
) {
    val colors = KineticTheme.colors
    val accent = mealAccent(entry.mealTime)
    val mTotal = entry.totalProtein + entry.totalCarbs + entry.totalFats
    val chevron by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "chevron_$index",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer)
            .border(width = 1.dp, color = colors.outlineVariant, shape = RoundedCornerShape(0.dp))
    ) {
        // ── Entry header ─────────────────────────────────────────────────
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Accent bar
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accent.color),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        MealTimeTag(entry.mealTime, accent)
                        Text(
                            text = entry.items.joinToString(" · ") { it.food },
                            color = MealGray,
                            fontSize = 10.sp,
                            fontFamily = SpaceGroteskFamily,
                            letterSpacing = 0.4.sp,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${entry.totalCalories}",
                        color = MealVolt,
                        fontSize = 20.sp,
                        fontFamily = LexendFamily,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp,
                    )
                    Text("KCAL", color = MealGray, fontSize = 9.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 0.8.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Macros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(MealCyan))
                    Text("P: ${entry.totalProtein.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(MealVolt))
                    Text("C: ${entry.totalCarbs.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(KineticTertiary))
                    Text("F: ${entry.totalFats.formatMacroGrams()}", color = MealWhite, fontSize = 11.sp, fontFamily = SpaceGroteskFamily)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Expand toggle button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(7.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle,
                    )
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "▼",
                        color = MealGray,
                        fontSize = 9.sp,
                        modifier = Modifier.rotate(chevron),
                    )
                    Text(
                        text = if (expanded) "Hide items" else "${entry.items.size} items",
                        color = MealGray,
                        fontSize = 10.sp,
                        fontFamily = SpaceGroteskFamily,
                        letterSpacing = 0.7.sp,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
        }

        // ── Expanded items list ───────────────────────────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(300, easing = FastOutSlowInEasing)),
            exit  = shrinkVertically(tween(280, easing = FastOutSlowInEasing)),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceContainerHigh)
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp)),
            ) {
                entry.items.forEachIndexed { i, item ->
                    FoodItemRow(item = item, accentColor = accent.color)
                    if (i < entry.items.lastIndex) KPDivider()
                }
            }
        }

        // ── Per-entry actions ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GhostButton(label = "Edit", onClick = {}, modifier = Modifier.weight(1f))
            SaveButton(
                state = state,
                idleLabel = "+ Log ${entry.mealTime}",
                savedLabel = "${entry.mealTime} Logged",
                accentColor = accent.color,
                onClick = onSave,
                modifier = Modifier.weight(2f),
            )
        }

        KPDivider()
    }
}

@Composable
private fun MultiLogSaveAllFooter(allDone: Boolean, saving: Boolean, onClick: () -> Unit) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(colors.surfaceContainerHigh)
            .border(
                1.dp, colors.outlineVariant,
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            )
            .padding(16.dp),
    ) {
        if (allDone) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(MealVolt))
                Spacer(Modifier.width(8.dp))
                Text("ALL MEALS LOGGED", color = MealVolt, fontSize = 12.sp, fontFamily = SpaceGroteskFamily, letterSpacing = 1.sp)
            }
        } else {
            val bg by animateColorAsState(
                targetValue = if (saving) MealVolt.copy(alpha = 0.15f) else MealVolt,
                animationSpec = tween(250),
                label = "saveAllBg",
            )
            val textColor = if (saving) MealVolt else Color(0xFF111111)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(bg)
                    .border(1.dp, if (saving) MealVolt.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(11.dp))
                    .clickable(enabled = !saving, onClick = onClick)
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (saving) "Logging…" else "⚡ Log All Meals",
                    color = textColor,
                    fontSize = 13.sp,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

private fun Double.formatCal() = "%,.0f".format(this)
