package com.shverma.kinetic.ui.aichat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shverma.kinetic.ui.aichat.components.LogFoodComponent
import com.shverma.kinetic.ui.theme.KineticShape
import com.shverma.kinetic.ui.theme.KineticSpacing
import com.shverma.kinetic.ui.theme.KineticTheme
import kotlinx.coroutines.launch

private val EXAMPLE_PROMPTS = listOf(
    "2 eggs and a roti",
    "chicken curry with rice, medium bowl",
)

@Composable
fun AIChatContent(actions: AIChatActions) {
    val state by actions.state.collectAsState()
    val colors = KineticTheme.colors
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
        containerColor = colors.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Log a meal",
                style = KineticTheme.typography.titleLg,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = KineticSpacing.lg, vertical = KineticSpacing.md),
            )

            if (state.messages.isEmpty() && !state.isTyping) {
                EmptyState(
                    modifier = Modifier.weight(1f),
                    onExampleClick = actions::onInputChange,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = KineticSpacing.lg),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(KineticSpacing.lg),
                    contentPadding = PaddingValues(vertical = KineticSpacing.md)
                ) {
                    items(state.messages) { message ->
                        ChatBubble(message = message, onSaveMeal = actions::saveMeal)
                    }

                    if (state.isTyping) {
                        item { TypingBubble() }
                    }
                }
            }

            ChatInput(
                text = state.inputText,
                onTextChange = actions::onInputChange,
                onSend = actions::sendMessage,
            )
        }
    }
}

@Composable
private fun EmptyState(
    onExampleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KineticSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(KineticShape.card))
                .background(colors.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Chat,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.size(KineticSpacing.lg))
        Text(text = "Describe what you ate", style = typography.titleLg, color = colors.onSurface)
        Spacer(Modifier.size(KineticSpacing.sm))
        Text(
            text = "Plain English — portions, cooking method, anything you'd tell a friend.",
            style = typography.bodyMd,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(KineticSpacing.xl))
        EXAMPLE_PROMPTS.forEach { example ->
            Surface(
                onClick = { onExampleClick(example) },
                color = colors.surfaceContainer,
                shape = RoundedCornerShape(KineticShape.pill),
                modifier = Modifier.padding(bottom = KineticSpacing.sm),
            ) {
                Text(
                    text = "\"$example\"",
                    style = typography.bodyMd,
                    color = colors.onSurface,
                    modifier = Modifier.padding(horizontal = KineticSpacing.lg, vertical = KineticSpacing.sm),
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onSaveMeal: (UIMeal, UIMeal) -> Unit,
) {
    val colors = KineticTheme.colors

    val aiLogs = message.aiLogs
    if (aiLogs != null) {
        LogFoodComponent(uiLog = aiLogs, onSaveMeal = onSaveMeal, modifier = Modifier.fillMaxWidth())
        return
    }

    val bgColor = if (message.isUser) colors.primary.copy(alpha = 0.08f) else colors.surfaceContainer
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.card))
            .background(bgColor)
            .padding(KineticSpacing.md),
    ) {
        Text(
            text = message.text ?: "",
            style = KineticTheme.typography.bodyMd,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun TypingBubble() {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KineticShape.card))
            .background(colors.surfaceContainer)
            .padding(horizontal = KineticSpacing.lg, vertical = KineticSpacing.md),
    ) {
        TypingIndicator(dotColor = colors.onSurfaceVariant)
    }
}

@Composable
private fun TypingIndicator(
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

@Composable
private fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val colors = KineticTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        color = colors.background,
    ) {
        Row(
            modifier = Modifier
                .padding(KineticSpacing.lg)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KineticSpacing.sm)
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("e.g. 2 rotis and dal, side bowl", color = colors.onSurfaceVariant) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceContainer,
                    unfocusedContainerColor = colors.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                ),
                shape = RoundedCornerShape(KineticShape.pill)
            )
            FloatingActionButton(
                onClick = onSend,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                shape = RoundedCornerShape(KineticShape.button),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
