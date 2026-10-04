package com.pace.calculator.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pace.calculator.PaceCalculator
import com.pace.calculator.PaceUnit
import com.pace.calculator.ui.theme.BebasNeue
import com.pace.calculator.ui.theme.LocalPaceColors
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlinx.coroutines.delay

// Shared shapes to avoid recreation
private val CardShape = RoundedCornerShape(20.dp)
private val ResultCardShape = RoundedCornerShape(16.dp)
private val InputShape = RoundedCornerShape(12.dp)
private val BadgeShape = RoundedCornerShape(8.dp)

// Shared animation spec
private val ColorAnimationSpec = tween<Color>(300)

// Shared shadow color
private val ShadowColor = Color.Black.copy(alpha = 0.08f)

// Radius of the heartbeat glow, in pixels
private const val PulseRadius = 1200f

// Pace limits for the stepper buttons, in seconds per unit
private const val MIN_PACE_SECONDS = 60L
private const val MAX_PACE_SECONDS = 59 * 60 + 59L

private const val FINE_STEP_SECONDS = 1
private const val COARSE_STEP_SECONDS = 5

// Holding a stepper button repeats the step
private const val HOLD_DELAY_MILLIS = 400L
private const val REPEAT_INTERVAL_MILLIS = 100L

// Font padding is dropped so the unit sits close under the figure
@Suppress("DEPRECATION")
private val HeroPaceStyle = TextStyle(
    fontFamily = BebasNeue,
    fontSize = 96.sp,
    lineHeight = 96.sp,
    letterSpacing = 2.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

private fun unitLabel(unit: PaceUnit) = if (unit == PaceUnit.MILE) "min/mile" else "min/km"

@Composable
fun PaceCalculatorScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    initialPaceSeconds: Double,
    initialUnit: PaceUnit,
    onPaceChange: (Double, PaceUnit) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    // Kept unrounded, so toggling units never changes the pace
    var paceSeconds by rememberSaveable { mutableStateOf(initialPaceSeconds) }
    var inputUnit by rememberSaveable { mutableStateOf(initialUnit) }

    val paceColors = LocalPaceColors.current

    val result = remember(paceSeconds, inputUnit) {
        PaceCalculator.calculate(paceSeconds, inputUnit)
    }

    fun convertPace(newUnit: PaceUnit) {
        if (newUnit == inputUnit) return

        paceSeconds = PaceCalculator.convert(paceSeconds, inputUnit, newUnit)
        inputUnit = newUnit
    }

    fun stepPace(deltaSeconds: Int) {
        val current = paceSeconds.roundToLong()
        val step = abs(deltaSeconds)

        // Steps land on multiples of the step size, so 5:17 goes to 5:20 or 5:15
        val target = if (deltaSeconds > 0) {
            (current / step + 1) * step
        } else {
            (current + step - 1) / step * step - step
        }

        paceSeconds = target
            .coerceIn(MIN_PACE_SECONDS, maxOf(current, MAX_PACE_SECONDS))
            .toDouble()
    }

    LaunchedEffect(paceSeconds, inputUnit) {
        if (paceSeconds > 0) onPaceChange(paceSeconds, inputUnit)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedPulseBackground(pulseColor = paceColors.pulseColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 20.dp)
        ) {
            Header(isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                HeroPace(
                    pace = (if (inputUnit == PaceUnit.MILE) result?.minPerMile else result?.minPerKm) ?: "—",
                    unit = unitLabel(inputUnit),
                    textMuted = paceColors.textMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                ResultsRow(result = result, inputUnit = inputUnit, textMuted = paceColors.textMuted)

                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel(text = "RACE TIMES", textMuted = paceColors.textMuted)

                Spacer(modifier = Modifier.height(8.dp))

                RaceTimesGrid(result = result, textMuted = paceColors.textMuted)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "BUILT FOR RUNNERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = paceColors.textMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            ControlPanel(
                inputUnit = inputUnit,
                onUnitChange = { convertPace(it) },
                onStep = { stepPace(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AnimatedPulseBackground(pulseColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val animationSpec = remember {
        infiniteRepeatable<Float>(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    }

    val scale = infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = animationSpec,
        label = "pulseScale"
    )

    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "pulseAlpha"
    )

    val brush = remember(pulseColor) {
        Brush.radialGradient(
            colors = listOf(pulseColor, Color.Transparent),
            radius = PulseRadius
        )
    }

    // Animated values are read in the layer block, so each frame only redraws
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
                compositingStrategy = CompositingStrategy.ModulateAlpha
            }
            // Drawn as a circle that may spill past the box, so the glow fades out
            // rather than being cut off at the box edges when it is scaled down
            .drawBehind { drawCircle(brush = brush, radius = PulseRadius) }
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
            .padding(top = 8.dp),
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
private fun HeroPace(pace: String, unit: String, textMuted: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = pace,
            style = HeroPaceStyle,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )

        Text(
            text = unit,
            style = MaterialTheme.typography.titleLarge,
            color = textMuted
        )
    }
}

@Composable
private fun ControlPanel(
    inputUnit: PaceUnit,
    onUnitChange: (PaceUnit) -> Unit,
    onStep: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = CardShape, ambientColor = ShadowColor, spotColor = ShadowColor),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UnitSelector(selectedUnit = inputUnit, onUnitChange = onUnitChange)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepButton(
                    text = "−$FINE_STEP_SECONDS",
                    description = "Subtract $FINE_STEP_SECONDS second",
                    prominent = false,
                    onStep = { onStep(-FINE_STEP_SECONDS) },
                    modifier = Modifier.weight(1f)
                )
                StepButton(
                    text = "+$FINE_STEP_SECONDS",
                    description = "Add $FINE_STEP_SECONDS second",
                    prominent = false,
                    onStep = { onStep(FINE_STEP_SECONDS) },
                    modifier = Modifier.weight(1f)
                )
            }

            // The most used buttons sit lowest, closest to the thumb
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepButton(
                    text = "−$COARSE_STEP_SECONDS",
                    description = "Subtract $COARSE_STEP_SECONDS seconds",
                    prominent = true,
                    onStep = { onStep(-COARSE_STEP_SECONDS) },
                    modifier = Modifier.weight(1f)
                )
                StepButton(
                    text = "+$COARSE_STEP_SECONDS",
                    description = "Add $COARSE_STEP_SECONDS seconds",
                    prominent = true,
                    onStep = { onStep(COARSE_STEP_SECONDS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StepButton(
    text: String,
    description: String,
    prominent: Boolean,
    onStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val step by rememberUpdatedState {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onStep()
    }

    // Set while a hold is repeating, so releasing it does not add one more step
    var repeated by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            repeated = false
            delay(HOLD_DELAY_MILLIS)
            repeated = true
            while (true) {
                step()
                delay(REPEAT_INTERVAL_MILLIS)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(InputShape)
            .background(if (prominent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = { if (repeated) repeated = false else step() }
            )
            .semantics { contentDescription = description }
            .height(if (prominent) 88.dp else 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(fontFamily = BebasNeue, fontSize = if (prominent) 44.sp else 28.sp, letterSpacing = 1.sp),
            color = if (prominent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
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
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PaceUnit.entries.forEach { unit ->
            UnitButton(
                text = unitLabel(unit),
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

    Box(
        modifier = modifier
            .clip(BadgeShape)
            .background(backgroundColor)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
            color = textColor
        )
    }
}

@Composable
private fun ResultsRow(result: PaceCalculator.PaceResult?, inputUnit: PaceUnit, textMuted: Color) {
    // The pace in the selected unit is the hero figure, so only the other one is listed
    val otherUnit = if (inputUnit == PaceUnit.MILE) PaceUnit.KM else PaceUnit.MILE
    val otherPace = if (otherUnit == PaceUnit.MILE) result?.minPerMile else result?.minPerKm

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ResultCard(
            value = otherPace ?: "—",
            unit = unitLabel(otherUnit),
            textMuted = textMuted,
            modifier = Modifier.weight(1f)
        )
        ResultCard(
            value = result?.mph?.let { "%.1f".format(it) } ?: "—",
            unit = "mph",
            textMuted = textMuted,
            modifier = Modifier.weight(1f)
        )
        ResultCard(
            value = result?.kph?.let { "%.1f".format(it) } ?: "—",
            unit = "km/h",
            textMuted = textMuted,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ResultCard(
    value: String,
    unit: String,
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
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = TextStyle(fontFamily = BebasNeue, fontSize = 36.sp, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )
        }
    }
}

@Composable
private fun RaceTimesGrid(result: PaceCalculator.PaceResult?, textMuted: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PaceCalculator.RaceDistance.entries.chunked(2).forEach { races ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                races.forEach { race ->
                    RaceTimeCard(
                        label = race.label,
                        time = result?.raceTimes?.get(race) ?: "—",
                        textMuted = textMuted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = textMuted
            )

            Text(
                text = time,
                style = TextStyle(fontFamily = BebasNeue, fontSize = 36.sp, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
