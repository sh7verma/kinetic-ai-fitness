package com.shverma.kinetic.ui.fuel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.ui.theme.KineticTheme
import com.shverma.kinetic.ui.theme.LexendFamily
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily

@Composable
fun LogMealsScreen(
    viewModel: LogMealsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val colors = KineticTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 1. HEADER SECTION
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CURRENT SESSION",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 2.sp,
                        color = Color(0xFF888888)
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
                Text(
                    text = "MANUAL LOG",
                    style = TextStyle(
                        fontFamily = LexendFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFCCFF00)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. FOOD ITEM ROWS & 3. ADD ITEM BUTTON (in LazyColumn)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                itemsIndexed(state.items) { index, item ->
                    FoodItemRow(
                        item = item,
                        onAmountChange = { viewModel.onEvent(LogMealsEvents.UpdateItemAmount(index, it)) },
                        onNameChange = { viewModel.onEvent(LogMealsEvents.UpdateItemName(index, it)) },
                        onDeleteClick = { viewModel.onEvent(LogMealsEvents.DeleteItem(index)) }
                    )
                }

                item {
                    AddItemButton(onClick = { viewModel.onEvent(LogMealsEvents.AddItem) })
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onEvent(LogMealsEvents.CalculateMacros) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A1A1A),
                            contentColor = Color(0xFFCCFF00)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CALCULATE MACROS",
                            style = TextStyle(
                                fontFamily = LexendFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    MacroSummaryCard(state = state)
                }
            }
        }

        // 5. FLOATING ACTION BUTTON (AI Scan)
        FloatingActionButton(
            onClick = { /* AI Scan */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp)
                .size(56.dp),
            containerColor = Color(0xFF00E3FD),
            contentColor = Color.Black,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "AI Scan",
                modifier = Modifier.size(24.dp)
            )
        }

        // 6. SAVE MEAL BUTTON
        Button(
            onClick = { 
                viewModel.onEvent(LogMealsEvents.SaveMeal)
                onBackClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCCFF00),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "SAVE MEAL",
                style = TextStyle(
                    fontFamily = LexendFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                )
            )
        }
    }
}

@Composable
fun FoodItemRow(
    item: MealItemData,
    onAmountChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AMOUNT
        Column(modifier = Modifier.weight(0.3f)) {
            Text(
                text = "AMOUNT",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 9.sp,
                    color = Color(0xFF888888)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                BasicTextField(
                    value = item.amount,
                    onValueChange = onAmountChange,
                    textStyle = TextStyle(
                        color = Color(0xFFCCFF00),
                        fontFamily = LexendFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFFCCFF00)),
                    modifier = Modifier.fillMaxWidth()
                )
                if (item.amount.isEmpty()) {
                    Text(
                        text = "0g",
                        style = TextStyle(
                            color = Color(0xFFCCFF00).copy(alpha = 0.3f),
                            fontFamily = LexendFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        // ITEM
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ITEM",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 9.sp,
                    color = Color(0xFF888888)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                BasicTextField(
                    value = item.name,
                    onValueChange = onNameChange,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = LexendFamily,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
                if (item.name.isEmpty()) {
                    Text(
                        text = "e.g. Oats",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.3f),
                            fontFamily = LexendFamily,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        // DELETE
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color(0xFFFF4444),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AddItemButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawRoundRect(
                    color = Color(0xFF333333),
                    style = stroke,
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ ADD ANOTHER ITEM",
            style = TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontSize = 13.sp,
                color = Color(0xFF888888),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun MacroSummaryCard(state: LogMealsState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131313), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MACRO SUMMARY",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            )
            Text(
                text = "ESTIMATED",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 10.sp,
                    color = Color(0xFF888888)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MacroValueItem(
                modifier = Modifier.weight(1f),
                label = "CALORIES",
                value = state.totalCalories,
                unit = "KCAL",
                valueSize = 40.sp,
                color = Color(0xFFCCFF00)
            )
            MacroValueItem(
                modifier = Modifier.weight(1f),
                label = "PROTEIN",
                value = state.totalProtein,
                unit = "G",
                valueSize = 40.sp,
                color = Color(0xFF00E3FD)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MacroValueItem(
                modifier = Modifier.weight(1f),
                label = "CARBS",
                value = state.totalCarbs,
                unit = "G",
                valueSize = 32.sp,
                color = Color(0xFFFF6B6B)
            )
            MacroValueItem(
                modifier = Modifier.weight(1f),
                label = "FATS",
                value = state.totalFats,
                unit = "G",
                valueSize = 32.sp,
                color = Color(0xFFFF6B6B)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PROGRESS BAR
        val proteinRatio = state.proteinRatio.coerceIn(0f, 1f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF1A1A1A))
        ) {
            if (proteinRatio > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(proteinRatio)
                        .background(Color(0xFF00E3FD))
                )
            }
            if (proteinRatio < 1f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f - proteinRatio)
                        .background(Color(0xFFFF6B6B))
                )
            }
        }
    }
}

@Composable
fun MacroValueItem(
    modifier: Modifier,
    label: String,
    value: String,
    unit: String,
    valueSize: androidx.compose.ui.unit.TextUnit,
    color: Color
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontSize = 9.sp,
                color = Color(0xFF888888)
            )
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = LexendFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = valueSize,
                    color = color
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = if (valueSize == 40.sp) 12.sp else 11.sp,
                    color = color
                ),
                modifier = Modifier.padding(bottom = if (valueSize == 40.sp) 8.dp else 6.dp)
            )
        }
    }
}
