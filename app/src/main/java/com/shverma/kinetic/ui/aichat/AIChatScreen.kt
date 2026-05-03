package com.shverma.kinetic.ui.aichat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.data.model.MealPlan
import com.shverma.kinetic.data.network.ChatType
import com.shverma.kinetic.ui.components.*
import com.shverma.kinetic.ui.theme.KineticTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    viewModel: AIChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val colors = KineticTheme.colors
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val chatTypeOptions = listOf(
        ChatTypeOption(ChatType.MEALS, "Meals", colors.secondary),
        ChatTypeOption(ChatType.LOG_MEAL, "Log Meal", Color(0xFF00BFAE)),
        ChatTypeOption(ChatType.WORKOUT, "Workout", colors.primaryContainer),
        ChatTypeOption(ChatType.LOG_WORKOUT, "Log Workout", colors.tertiary)
    )

    // Scroll to bottom when messages change or typing starts
    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty() || state.isTyping) {
            coroutineScope.launch {
                val lastIndex = if (state.isTyping) state.messages.size else state.messages.size - 1
                if (lastIndex >= 0) {
                    listState.animateScrollToItem(lastIndex)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("AI Coach", style = KineticTheme.typography.titleMd)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState,
                reverseLayout = false,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(state.messages) { message ->
                    ChatBubble(
                        message = message,
                        colors = colors,
                        onSaveMealPlan = { viewModel.saveMealPlan(it) },
                        onSaveWorkoutPlan = { viewModel.saveWorkoutPlan(it) },
                        onSaveSingleLog = { message.singleLog?.let { viewModel.saveSingleLog(it) } },
                        onSaveMultiLogEntry = { viewModel.saveMultiLogEntry(it) },
                        onSaveAllMultiLog = { message.multiLog?.let { viewModel.saveAllMultiLog(it) } },
                        onDiscard = { viewModel.discardMessage(message) }
                    )
                }

                if (state.isTyping) {
                    item {
                        TypingBubble(colors = colors)
                    }
                }
            }

            ChatInput(
                text = state.inputText,
                onTextChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage,
                chatTypes = chatTypeOptions,
                selectedType = state.chatType,
                onTypeClick = viewModel::onChatTypeChange,
                colors = colors
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    colors: com.shverma.kinetic.ui.theme.KineticColors,
    onSaveMealPlan: (MealPlan) -> Unit,
    onSaveWorkoutPlan: (com.shverma.kinetic.data.model.WorkoutPlan) -> Unit,
    onSaveSingleLog: () -> Unit,
    onSaveMultiLogEntry: (com.shverma.kinetic.data.model.LogEntry) -> Unit,
    onSaveAllMultiLog: () -> Unit,
    onDiscard: () -> Unit,
) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) colors.primaryContainer else colors.surfaceContainer
    val textColor = if (message.isUser) colors.onPrimary else colors.onSurface

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier
                .fillMaxWidth(if (message.mealPlan != null || message.singleLog != null || message.multiLog != null) 0.95f else 0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 16.dp
                    )
                )
                .background(if (message.mealPlan != null || message.singleLog != null || message.multiLog != null) Color.Transparent else bgColor)
                .padding(if (message.mealPlan != null || message.singleLog != null || message.multiLog != null) 0.dp else 12.dp)
        ) {
            if (message.mealPlan == null && message.singleLog == null && message.multiLog == null) {
                Text(
                    text = message.text,
                    style = KineticTheme.typography.bodyMd,
                    color = textColor
                )
            }

            message.workoutPlan?.let { workout ->
                Spacer(modifier = Modifier.height(8.dp))
                workout.exercises.forEach { exercise ->
                    Text(
                        text = "• ${exercise.name} (${exercise.sets}x${exercise.reps})",
                        style = KineticTheme.typography.bodySm,
                        color = textColor
                    )
                }
            }

            message.mealPlan?.let { mealPlan ->
                MealPlanCard(
                    plan = mealPlan,
                    onSaveMeal = { /* Injected via callback if needed, but here we have saveAll */ },
                    onSaveAll = { onSaveMealPlan(mealPlan) },
                    onDiscard = onDiscard
                )
            }

            message.singleLog?.let { log ->
                QuickLogCard(
                    entry = log,
                    onSave = { onSaveSingleLog() },
                    onDiscard = onDiscard
                )
            }

            message.multiLog?.let { log ->
                MultiLogCard(
                    data = log,
                    onSaveEntry = { onSaveMultiLogEntry(it) },
                    onSaveAll = { onSaveAllMultiLog() },
                    onDiscard = onDiscard
                )
            }
        }
    }
}


@Composable
fun TypingBubble(colors: com.shverma.kinetic.ui.theme.KineticColors) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp))
                .background(colors.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TypingIndicator(dotColor = colors.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun TypingIndicator(
    dotColor: Color,
    dotSize: Dp = 6.dp,
    spacing: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 0
                1f at 200
                0.2f at 400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1"
    )

    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 100
                1f at 300
                0.2f at 500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2"
    )

    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 200
                1f at 400
                0.2f at 600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(dotColor.copy(alpha = dot1Alpha), dotSize)
        Dot(dotColor.copy(alpha = dot2Alpha), dotSize)
        Dot(dotColor.copy(alpha = dot3Alpha), dotSize)
    }
}

@Composable
private fun Dot(color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    chatTypes: List<ChatTypeOption> = emptyList(),
    selectedType: ChatType? = null,
    onTypeClick: (ChatType) -> Unit = {},
    colors: com.shverma.kinetic.ui.theme.KineticColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        color = colors.background,
        tonalElevation = 8.dp
    ) {
        Column {
            if (chatTypes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    chatTypes.forEach { option ->
                        val isSelected = option.type == selectedType
                        Surface(
                            onClick = { onTypeClick(option.type) },
                            color = if (isSelected) option.color else colors.surfaceContainer,
                            shape = CircleShape,
                            border = if (isSelected) null else borderStroke(colors)
                        ) {
                            Text(
                                text = option.label,
                                style = KineticTheme.typography.labelSm,
                                color = if (isSelected) Color.Black else colors.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask anything...", color = colors.onSurfaceVariant) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceContainer,
                        unfocusedContainerColor = colors.surfaceContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                FloatingActionButton(
                    onClick = onSend,
                    containerColor = colors.primaryContainer,
                    contentColor = colors.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun borderStroke(colors: com.shverma.kinetic.ui.theme.KineticColors) = 
    androidx.compose.foundation.BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.1f))

data class ChatTypeOption(
    val type: ChatType,
    val label: String,
    val color: Color
)
