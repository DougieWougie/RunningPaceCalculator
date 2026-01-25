package com.pace.calculator.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pace.calculator.PaceCalculator
import com.pace.calculator.PaceUnit
import com.pace.calculator.ui.theme.BebasNeue
import com.pace.calculator.ui.theme.LocalPaceColors

@Composable
fun PaceCalculatorScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var minutes by remember { mutableStateOf("8") }
    var seconds by remember { mutableStateOf("30") }
    var inputUnit by remember { mutableStateOf(PaceUnit.MILE) }

    val paceColors = LocalPaceColors.current

    val result = remember(minutes, seconds, inputUnit) {
        val mins = minutes.toIntOrNull() ?: 0
        val secs = seconds.toIntOrNull() ?: 0
        PaceCalculator.calculate(mins, secs, inputUnit)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated background pulse
        AnimatedPulseBackground(
            pulseColor = paceColors.pulseColor
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Header(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Section label
            SectionLabel(text = "ENTER YOUR PACE")

            Spacer(modifier = Modifier.height(12.dp))

            // Input card
            InputCard(
                minutes = minutes,
                onMinutesChange = { minutes = it },
                seconds = seconds,
                onSecondsChange = { seconds = it },
                inputUnit = inputUnit,
                onUnitChange = { inputUnit = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Results section
            SectionLabel(text = "CONVERTED PACES")

            Spacer(modifier = Modifier.height(12.dp))

            // Results grid
            ResultsGrid(result = result)

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = "BUILT FOR RUNNERS",
                style = MaterialTheme.typography.labelSmall,
                color = paceColors.textMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))
        }

    }
}

@Composable
private fun AnimatedPulseBackground(pulseColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        pulseColor.copy(alpha = alpha * pulseColor.alpha),
                        Color.Transparent
                    ),
                    radius = 1000f
                )
            )
    )
}

@Composable
private fun Header(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PACE",
            style = MaterialTheme.typography.headlineLarge.copy(
                letterSpacing = 6.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        ThemeToggle(
            isDarkTheme = isDarkTheme,
            onToggle = onToggleTheme
        )
    }
}

@Composable
private fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: () -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isDarkTheme) 24.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumbOffset"
    )

    Box(
        modifier = Modifier
            .width(56.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDarkTheme) "\u263E" else "\u2600",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = LocalPaceColors.current.textMuted
    )
}

@Composable
private fun InputCard(
    minutes: String,
    onMinutesChange: (String) -> Unit,
    seconds: String,
    onSecondsChange: (String) -> Unit,
    inputUnit: PaceUnit,
    onUnitChange: (PaceUnit) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFocused) 16.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.08f),
                spotColor = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(28.dp)
        ) {
            // Pace input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimeInput(
                        value = minutes,
                        onValueChange = { if (it.length <= 2) onMinutesChange(it.filter { c -> c.isDigit() }) },
                        onFocusChanged = { isFocused = it },
                        modifier = Modifier.width(72.dp)
                    )

                    Text(
                        text = ":",
                        style = TextStyle(
                            fontFamily = BebasNeue,
                            fontSize = 40.sp
                        ),
                        color = LocalPaceColors.current.textMuted
                    )

                    TimeInput(
                        value = seconds,
                        onValueChange = {
                            if (it.length <= 2) {
                                val filtered = it.filter { c -> c.isDigit() }
                                val value = filtered.toIntOrNull() ?: 0
                                if (value <= 59) onSecondsChange(filtered)
                            }
                        },
                        onFocusChanged = { isFocused = it },
                        modifier = Modifier.width(72.dp)
                    )
                }

                // Unit badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (inputUnit == PaceUnit.MILE) "/mile" else "/km",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Unit selector
            UnitSelector(
                selectedUnit = inputUnit,
                onUnitChange = onUnitChange
            )
        }
    }
}

@Composable
private fun TimeInput(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "inputBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(300),
        label = "inputBorder"
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .onFocusChanged {
                isFocused = it.isFocused
                onFocusChanged(it.isFocused)
            },
        textStyle = TextStyle(
            fontFamily = BebasNeue,
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                innerTextField()
            }
        }
    )
}

@Composable
private fun UnitSelector(
    selectedUnit: PaceUnit,
    onUnitChange: (PaceUnit) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        UnitButton(
            text = "min/mile",
            isSelected = selectedUnit == PaceUnit.MILE,
            onClick = { onUnitChange(PaceUnit.MILE) },
            modifier = Modifier.weight(1f)
        )

        UnitButton(
            text = "min/km",
            isSelected = selectedUnit == PaceUnit.KM,
            onClick = { onUnitChange(PaceUnit.KM) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UnitButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(300),
        label = "unitBtnBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "unitBtnText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
private fun ResultsGrid(result: PaceCalculator.PaceResult?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ResultCard(
            label = "Speed",
            value = result?.mph?.let { String.format("%.2f", it) } ?: "—",
            unit = "mph"
        )

        ResultCard(
            label = "Speed",
            value = result?.kph?.let { String.format("%.2f", it) } ?: "—",
            unit = "km/h"
        )

        ResultCard(
            label = "Pace",
            value = result?.minPerMile ?: "—",
            unit = "min/mi"
        )

        ResultCard(
            label = "Pace",
            value = result?.minPerKm ?: "—",
            unit = "min/km"
        )
    }
}

@Composable
private fun ResultCard(
    label: String,
    value: String,
    unit: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalPaceColors.current.textMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = BebasNeue,
                        fontSize = 36.sp,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

