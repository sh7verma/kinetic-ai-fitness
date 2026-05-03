package com.shverma.kinetic.ui.components

import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily

// ─────────────────────────────────────────
// KineticPrimaryButton
// ─────────────────────────────────────────

@Composable
fun KineticPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    val gradient = Brush.linearGradient(
        colors = listOf(colors.primaryDim, colors.primaryContainer),
    )
    val disabledBg = Brush.linearGradient(
        colors = listOf(colors.surfaceContainerHigh, colors.surfaceContainerHighest),
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(brush = if (enabled) gradient else disabledBg)
            .clickable(
                enabled = enabled,
                indication = ripple(color = colors.primaryContainer.copy(alpha = 0.2f)),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            style = typography.labelMd,
            color = if (enabled) colors.onPrimaryFixed else colors.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────
// KineticSecondaryButton
// ─────────────────────────────────────────

@Composable
fun KineticSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surfaceContainerHighest)
            .then(

                    Modifier.background(colors.secondary.copy(alpha = 0.15f))

            )
            .background(colors.secondary.copy(alpha = 0.10f))
            .clickable(
                enabled = enabled,
                indication = ripple(color = colors.secondary.copy(alpha = 0.15f)),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            style = typography.labelMd,
            color = if (enabled) colors.secondary else colors.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────
// KineticDataCard
// ─────────────────────────────────────────

@Composable
fun KineticDataCard(
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    metric: (@Composable () -> Unit)? = null,
    unit: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = KineticTheme.colors

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (title != null || trailing != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box { title?.invoke() }
                    Box { trailing?.invoke() }
                }
            }
            metric?.invoke()
            unit?.invoke()
        }
    }
}

// ─────────────────────────────────────────
// KineticProgressBar
// ─────────────────────────────────────────

@Composable
fun KineticProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 6.dp,
    color: Color? = null,
) {
    val colors = KineticTheme.colors
    val isOverTarget = progress > 1f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "kinetic_progress",
    )

    val baseColor = color ?: colors.primary
    val dimColor = color?.copy(alpha = 0.5f) ?: colors.primaryDim

    val fillBrush = if (isOverTarget) {
        Brush.horizontalGradient(
            colors = listOf(colors.error, colors.error.copy(alpha = 0.5f)),
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(dimColor, baseColor),
        )
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight),
        ) {
            val cornerRadius = CornerRadius(size.height / 2)

            // Track
            drawRoundRect(
                color = colors.surfaceContainerHigh,
                cornerRadius = cornerRadius,
            )

            // Fill gradient
            if (animatedProgress > 0f) {
                drawRoundRect(
                    brush = fillBrush,
                    size = Size(width = size.width * animatedProgress, height = size.height),
                    cornerRadius = cornerRadius,
                )
            }
        }

        // Trailing edge glow — composited blur layer
        if (animatedProgress > 0.05f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(trackHeight)
                    .blur(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.7f to Color.Transparent,
                                1.0f to baseColor.copy(alpha = 0.65f),
                            ),
                        ),
                        shape = RoundedCornerShape(trackHeight / 2),
                    ),
            )
        }
    }
}

// ─────────────────────────────────────────
// Modifier.kineticGlow
// FAB / floating element ambient glow (API 24+)
// ─────────────────────────────────────────

fun Modifier.kineticGlow(
    color: Color,
    blurRadius: Dp = 24.dp,
    cornerRadius: Dp = 16.dp,
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val glowPaint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                this.color = android.graphics.Color.TRANSPARENT
                maskFilter = BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
        }
        val shadowColor = color.copy(alpha = 0.12f)
        glowPaint.color = shadowColor

        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadius.toPx(),
            radiusY = cornerRadius.toPx(),
            paint = glowPaint,
        )
    }
}

// ─────────────────────────────────────────
// KineticTopAppBar
// ─────────────────────────────────────────

@Composable
fun KineticTopAppBar(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    val colors = KineticTheme.colors
    val typography = KineticTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Profile Avatar or Back Button
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
            ) {
                Text(
                    text = "←",
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, colors.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .background(colors.surfaceContainerLow)
            )
        }

        // Title
        Text(
            text = "KINETIC",
            style = typography.titleLg.copy(
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            ),
            color = colors.primaryContainer
        )

        // Right side: Notification
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = colors.onSurface,
                modifier = Modifier.size(24.dp)
            )
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(colors.primaryContainer, CircleShape)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .border(1.5.dp, colors.background, CircleShape)
            )
        }
    }
}

@Composable
fun TonalActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingDot: Boolean = false,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(10.dp))
            .clickable(
                indication = ripple(color = Color(0x18CAFD00)),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingDot) {
                // Minimal Google "G" indicator
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(8.dp)
                        .background(Color(0xFF4285F4), RoundedCornerShape(4.dp)),
                )
            }
            Text(
                text = text,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = Color(0xFFADADAD),
            )
        }
    }
}

@Composable
fun KineticExerciseHeader(
    tag: String,
    title: String,
    modifier: Modifier = Modifier
) {
    val typography = KineticTheme.typography
    val colors = KineticTheme.colors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = colors.primaryContainer,
            shape = CircleShape
        ) {
            Text(
                text = tag,
                style = typography.labelSm.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color.Black
            )
        }
        Text(
            text = title,
            style = typography.displayMd.copy(
                fontSize = 38.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Black,
                lineHeight = 44.sp
            ),
            color = Color.White
        )
    }
}

@Composable
fun KineticMetricCard(
    label: String,
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = KineticTheme.typography
    val colors = KineticTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = typography.labelSm.copy(
                        fontFamily = SpaceGroteskFamily,
                        color = colors.onSurfaceVariant
                    )
                )
                Text(
                    text = value,
                    style = typography.titleLg.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KineticIncrementButton(icon = Icons.Default.Remove, onClick = onDecrement)
                KineticIncrementButton(icon = Icons.Default.Add, onClick = onIncrement)
            }
        }
    }
}

@Composable
private fun KineticIncrementButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = KineticTheme.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(colors.surfaceContainer)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun KineticUpNextCard(
    setNumber: Int,
    totalSets: Int,
    targetReps: Int,
    targetWeight: Float,
    modifier: Modifier = Modifier
) {
    val typography = KineticTheme.typography
    val colors = KineticTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "UP NEXT »",
                style = typography.labelSm.copy(
                    fontFamily = SpaceGroteskFamily,
                    color = colors.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SET $setNumber OF $totalSets",
                        style = typography.titleMd.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "TARGET: $targetReps REPS · ${if (targetWeight % 1 == 0f) targetWeight.toInt() else targetWeight} KG",
                        style = typography.bodyMd.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = colors.onSurfaceVariant
                        )
                    )
                }

                // Optional icon
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = colors.surfaceContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────
// DotLoader
// ─────────────────────────────────────────

@Composable
fun DotLoader(
    modifier: Modifier = Modifier,
    dotSize: Dp = 6.dp,
    dotColor: Color = Color.Black,
    spacing: Dp = 4.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_loader")
    
    @Composable
    fun animateDot(delay: Int): State<Float> {
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    0f at delay with LinearEasing
                    1f at delay + 300 with LinearEasing
                    0f at delay + 600 with LinearEasing
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "dot_alpha"
        )
    }

    val dot1Alpha by animateDot(0)
    val dot2Alpha by animateDot(150)
    val dot3Alpha by animateDot(300)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(dotSize)
                .graphicsLayer { alpha = dot1Alpha }
                .background(dotColor, CircleShape)
        )
        Box(
            Modifier
                .size(dotSize)
                .graphicsLayer { alpha = dot2Alpha }
                .background(dotColor, CircleShape)
        )
        Box(
            Modifier
                .size(dotSize)
                .graphicsLayer { alpha = dot3Alpha }
                .background(dotColor, CircleShape)
        )
    }
}