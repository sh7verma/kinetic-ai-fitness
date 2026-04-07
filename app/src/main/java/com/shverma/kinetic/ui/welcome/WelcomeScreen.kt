package com.shverma.kinetic.ui.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shverma.kinetic.R
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shverma.kinetic.ui.components.TonalActionButton
import com.shverma.kinetic.ui.theme.KineticColors
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.LexendFamily
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily

// ─────────────────────────────────────────
// WelcomeScreen — Kinetic Volt entry point
// ─────────────────────────────────────────

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    val colors = KineticTheme.colors
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onGetStarted()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Layer 0 — Hero background image
        Image(
            painter = painterResource(R.drawable.bg_welcome),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Layer 1 — Top-to-bottom atmospheric overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xCC0A0A0A),
                            0.30f to Color(0x440A0A0A),
                            0.55f to Color(0x660A0A0A),
                            1.00f to Color(0xFF0A0A0A),
                        ),
                    ),
                ),
        )

        // Layer 2 — Brand + value prop pinned to top
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
        ) {
            Spacer(Modifier.height(72.dp))

            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(500)) +
                        slideInVertically(tween(500, easing = FastOutSlowInEasing)) { -32 },
            ) {
                BrandIdentity(colors = colors)
            }

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(600, delayMillis = 100)) +
                        slideInVertically(tween(600, delayMillis = 100, easing = FastOutSlowInEasing)) { 24 },
            ) {
                ValueProposition(colors = colors)
            }

            Spacer(Modifier.weight(1f))
        }

        // Layer 3 — Glassmorphism action hub pinned to bottom
        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(500, delayMillis = 300)) +
                    slideInVertically(tween(500, delayMillis = 300, easing = FastOutSlowInEasing)) { 80 },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ActionHub(
                colors = colors,
                onGetStarted = onGetStarted,
                onSignIn = onSignIn,
                onGoogleSignIn = { viewModel.signInWithGoogle(context as Activity) },
                isLoading = authState is AuthState.Loading,
            )
        }
    }
}

// ─────────────────────────────────────────
// BrandIdentity — "KINETIC VOLT" hero headline
// ─────────────────────────────────────────

@Composable
private fun BrandIdentity(colors: KineticColors) {
    val voltGlow = Color(0xFFCAFD00)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Micro badge — app category
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(voltGlow, RoundedCornerShape(1.dp)),
            )
            Text(
                text = "AI GYM COACH",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 3.sp,
                color = colors.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(10.dp))

        // KINETIC — white, italic, large
        Text(
            text = "KINETIC",
            fontFamily = LexendFamily,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            fontSize = 58.sp,
            lineHeight = 58.sp,
            letterSpacing = (-1).sp,
            color = Color.White,
        )

        // VOLT — neon lime with soft glow halo
        Box {
            // Glow layer — rendered behind via drawBehind blur sim
            Text(
                text = "VOLT",
                fontFamily = LexendFamily,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = 58.sp,
                lineHeight = 58.sp,
                letterSpacing = (-1).sp,
                color = voltGlow.copy(alpha = 0.35f),
                modifier = Modifier.drawBehind {
                    // Simulate neon bloom with expanded semi-transparent draw
                    drawRect(
                        color = voltGlow.copy(alpha = 0.04f),
                        topLeft = Offset(-24f, -8f),
                        size = this.size.copy(
                            width = this.size.width + 48f,
                            height = this.size.height + 16f,
                        ),
                    )
                },
            )
            // Crisp volt text on top
            Text(
                text = "VOLT",
                fontFamily = LexendFamily,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = 58.sp,
                lineHeight = 58.sp,
                letterSpacing = (-1).sp,
                color = voltGlow,
            )
        }
    }
}

// ─────────────────────────────────────────
// ValueProposition — tagline under brand
// ─────────────────────────────────────────

@Composable
private fun ValueProposition(colors: KineticColors) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White)) {
                append("Unlock peak ")
            }
            withStyle(SpanStyle(color = Color(0xFFCAFD00))) {
                append("physiological potential")
            }
            withStyle(SpanStyle(color = Color.White)) {
                append(" through AI-driven data harvesting\nand precision kinetic training.")
            }
        },
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = colors.onSurface,
    )
}

// ─────────────────────────────────────────
// ActionHub — glassmorphism bottom container
// ─────────────────────────────────────────

@Composable
private fun ActionHub(
    colors: KineticColors,
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    isLoading: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xE50E0E0E),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x30FFFFFF), Color(0x08FFFFFF)),
                ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            )
            .padding(horizontal = 28.dp, vertical = 32.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Primary CTA — solid volt fill, maximum contrast
            GetStartedButton(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth(),
            )

            // Divider + "or" label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0x20FFFFFF),
                )
                Text(
                    text = "or",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0x20FFFFFF),
                )
            }

            // Secondary row — Login + Google
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TonalActionButton(
                    text = "LOGIN",
                    onClick = onSignIn,
                    modifier = Modifier.weight(1f),
                )
                TonalActionButton(
                    text = if (isLoading) "..." else "GOOGLE",
                    onClick = onGoogleSignIn,
                    modifier = Modifier.weight(1f),
                    leadingDot = !isLoading,
                )
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

// ─────────────────────────────────────────
// GetStartedButton — solid volt, primary CTA
// ─────────────────────────────────────────

@Composable
private fun GetStartedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFCAFD00))
            .clickable(
                indication = ripple(color = Color(0x33000000)),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                append("GET STARTED")
                withStyle(SpanStyle(letterSpacing = 4.sp)) { append("  ") }
                append("→")
            },
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            color = Color(0xFF0E1A00),
        )
    }
}