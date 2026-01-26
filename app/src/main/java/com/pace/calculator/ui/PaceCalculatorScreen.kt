package com.pace.calculator.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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

// Shared shapes to avoid recreation
private val CardShape = RoundedCornerShape(20.dp)
private val ResultCardShape = RoundedCornerShape(16.dp)
private val InputShape = RoundedCornerShape(12.dp)
private val BadgeShape = RoundedCornerShape(8.dp)

// Shared animation spec
private val ColorAnimationSpec = tween<Color>(300)

// Shared shadow color
private val ShadowColor = Color.Black.copy(alpha = 0.08f)

private data class ResultItem(
    val label: String,
    val getValue: (PaceCalculator.PaceResult?) -> String,
    val unit: String
)

private val resultItems = listOf(
    ResultItem("Speed", { it?.mph?.let { v -> "%.2f".format(v) } ?: "—" }, "mph"),
    ResultItem("Speed", { it?.kph?.let { v -> "%.2f".format(v) } ?: "—" }, "km/h"),
    ResultItem("Pace", { it?.minPerMile ?: "—" }, "min/mi"),
    ResultItem("Pace", { it?.minPerKm ?: "—" }, "min/km")
)

private const val MILES_PER_KM = 0.621371
private const val KM_PER_MILE = 1.60934

@Composable
fun PaceCalculatorScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    var minutes by remember { mutableStateOf("8") }
    var seconds by remember { mutableStateOf("30") }
    var inputUnit by remember { mutableStateOf(PaceUnit.MILE) }

    val paceColors = LocalPaceColors.current

    val result by remember(minutes, seconds, inputUnit) {
        derivedStateOf {
            val mins = minutes.toIntOrNull() ?: 0
            val secs = seconds.toIntOrNull() ?: 0
            PaceCalculator.calculate(mins, secs, inputUnit)
        }
    }

    fun convertPace(newUnit: PaceUnit) {
        if (newUnit == inputUnit) return

        val mins = minutes.toIntOrNull() ?: 0
        val secs = seconds.toIntOrNull() ?: 0
        val totalSeconds = mins * 60 + secs

        val convertedSeconds = if (newUnit == PaceUnit.KM) {
            // Converting from min/mile to min/km (shorter distance = faster pace)
            (totalSeconds * MILES_PER_KM).toInt()
        } else {
            // Converting from min/km to min/mile (longer distance = slower pace)
            (totalSeconds * KM_PER_MILE).toInt()
        }

        minutes = (convertedSeconds / 60).toString()
        seconds = (convertedSeconds % 60).toString()
        inputUnit = newUnit
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedPulseBackground(pulseColor = paceColors.pulseColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Header(isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)

            Spacer(modifier = Modifier.height(48.dp))

            SectionLabel(text = "ENTER YOUR PACE", textMuted = paceColors.textMuted)

            Spacer(modifier = Modifier.height(12.dp))

            InputCard(
                minutes = minutes,
                onMinutesChange = { minutes = it },
                seconds = seconds,
                onSecondsChange = { seconds = it },
                inputUnit = inputUnit,
                onUnitChange = { convertPace(it) },
                textMuted = paceColors.textMuted
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionLabel(text = "CONVERTED PACES", textMuted = paceColors.textMuted)

            Spacer(modifier = Modifier.height(12.dp))

            ResultsGrid(result = result, textMuted = paceColors.textMuted)

            Spacer(modifier = Modifier.height(32.dp))

            SectionLabel(text = "RACE TIMES", textMuted = paceColors.textMuted)

            Spacer(modifier = Modifier.height(12.dp))

            RaceTimesGrid(result = result, textMuted = paceColors.textMuted)

            Spacer(modifier = Modifier.height(32.dp))

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

    val animationSpec = remember {
        infiniteRepeatable<Float>(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    }

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = animationSpec,
        label = "pulseScale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = animationSpec,
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
            style = MaterialTheme.typography.headlineLarge.copy(letterSpacing = 6.sp),
            color = MaterialTheme.colorScheme.primary
        )

        ThemeToggle(isDarkTheme = isDarkTheme, onToggle = onToggleTheme)
    }
}

@Composable
private fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Dark",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = isDarkTheme,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun SectionLabel(text: String, textMuted: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = textMuted
    )
}

@Composable
private fun InputCard(
    minutes: String,
    onMinutesChange: (String) -> Unit,
    seconds: String,
    onSecondsChange: (String) -> Unit,
    inputUnit: PaceUnit,
    onUnitChange: (PaceUnit) -> Unit,
    textMuted: Color
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = ColorAnimationSpec,
        label = "borderColor"
    )

    val shadowColor = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else ShadowColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFocused) 16.dp else 8.dp,
                shape = CardShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            ),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, borderColor, CardShape)
                .padding(28.dp)
        ) {
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
                        onValueChange = { if (it.length <= 2) onMinutesChange(it.filter(Char::isDigit)) },
                        onFocusChanged = { isFocused = it },
                        modifier = Modifier.width(72.dp)
                    )

                    Text(
                        text = ":",
                        style = TextStyle(fontFamily = BebasNeue, fontSize = 40.sp),
                        color = textMuted
                    )

                    TimeInput(
                        value = seconds,
                        onValueChange = { input ->
                            if (input.length <= 2) {
                                val filtered = input.filter(Char::isDigit)
                                if ((filtered.toIntOrNull() ?: 0) <= 59) onSecondsChange(filtered)
                            }
                        },
                        onFocusChanged = { isFocused = it },
                        modifier = Modifier.width(72.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(BadgeShape)
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

            UnitSelector(selectedUnit = inputUnit, onUnitChange = onUnitChange)
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
        animationSpec = ColorAnimationSpec,
        label = "inputBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = ColorAnimationSpec,
        label = "inputBorder"
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(64.dp)
            .clip(InputShape)
            .background(backgroundColor)
            .border(2.dp, borderColor, InputShape)
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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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
            .clip(InputShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PaceUnit.entries.forEach { unit ->
            UnitButton(
                text = if (unit == PaceUnit.MILE) "min/mile" else "min/km",
                isSelected = selectedUnit == unit,
                onClick = { onUnitChange(unit) },
                modifier = Modifier.weight(1f)
            )
        }
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
        animationSpec = ColorAnimationSpec,
        label = "unitBtnBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = ColorAnimationSpec,
        label = "unitBtnText"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(BadgeShape)
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}

@Composable
private fun ResultsGrid(result: PaceCalculator.PaceResult?, textMuted: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        resultItems.forEach { item ->
            ResultCard(
                label = item.label,
                value = item.getValue(result),
                unit = item.unit,
                textMuted = textMuted
            )
        }
    }
}

@Composable
private fun ResultCard(
    label: String,
    value: String,
    unit: String,
    textMuted: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = ResultCardShape, ambientColor = ShadowColor, spotColor = ShadowColor),
        shape = ResultCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, ResultCardShape)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = textMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    style = TextStyle(fontFamily = BebasNeue, fontSize = 36.sp, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(BadgeShape)
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

@Composable
private fun RaceTimesGrid(result: PaceCalculator.PaceResult?, textMuted: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RaceTimeCard(
                label = PaceCalculator.RaceDistance.FIVE_K.label,
                time = result?.raceTimes?.get(PaceCalculator.RaceDistance.FIVE_K) ?: "—",
                textMuted = textMuted,
                modifier = Modifier.weight(1f)
            )
            RaceTimeCard(
                label = PaceCalculator.RaceDistance.TEN_K.label,
                time = result?.raceTimes?.get(PaceCalculator.RaceDistance.TEN_K) ?: "—",
                textMuted = textMuted,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RaceTimeCard(
                label = PaceCalculator.RaceDistance.HALF_MARATHON.label,
                time = result?.raceTimes?.get(PaceCalculator.RaceDistance.HALF_MARATHON) ?: "—",
                textMuted = textMuted,
                modifier = Modifier.weight(1f)
            )
            RaceTimeCard(
                label = PaceCalculator.RaceDistance.MARATHON.label,
                time = result?.raceTimes?.get(PaceCalculator.RaceDistance.MARATHON) ?: "—",
                textMuted = textMuted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RaceTimeCard(
    label: String,
    time: String,
    textMuted: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = ResultCardShape, ambientColor = ShadowColor, spotColor = ShadowColor),
        shape = ResultCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, ResultCardShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = textMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = time,
                style = TextStyle(fontFamily = BebasNeue, fontSize = 28.sp, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
