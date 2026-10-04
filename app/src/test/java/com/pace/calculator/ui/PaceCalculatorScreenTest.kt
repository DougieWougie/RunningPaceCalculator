package com.pace.calculator.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.pace.calculator.PaceUnit
import com.pace.calculator.ui.theme.PaceCalculatorTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaceCalculatorScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val savedPaces = mutableListOf<Pair<Double, PaceUnit>>()

    private fun showScreen(paceSeconds: Double = 510.0, unit: PaceUnit = PaceUnit.MILE) {
        composeRule.setContent {
            PaceCalculatorTheme(darkTheme = false) {
                PaceCalculatorScreen(
                    isDarkTheme = false,
                    onToggleTheme = {},
                    initialPaceSeconds = paceSeconds,
                    initialUnit = unit,
                    onPaceChange = { pace, paceUnit -> savedPaces += pace to paceUnit }
                )
            }
        }
    }

    // The unit names also appear as plain labels beside the paces
    private fun unitOption(text: String) = composeRule.onNode(hasText(text) and isSelectable())

    private fun stepButton(description: String) = composeRule.onNodeWithContentDescription(description)

    @Test
    fun showsTheSavedPaceAndUnit() {
        showScreen(paceSeconds = 510.0 / 1.609344, unit = PaceUnit.KM)

        unitOption("min/km").assertIsSelected()
        unitOption("min/mile").assertIsNotSelected()
        composeRule.onNodeWithText("5:17").assertExists()
        composeRule.onNodeWithText("8:30").assertExists()
    }

    @Test
    fun togglingUnitsKeepsThePaceAndResults() {
        showScreen()

        unitOption("min/km").performClick()

        unitOption("min/km").assertIsSelected()
        composeRule.onNodeWithText("8:30").assertExists()
        composeRule.onNodeWithText("5:17").assertExists()

        unitOption("min/mile").performClick()

        unitOption("min/mile").assertIsSelected()
        composeRule.onNodeWithText("8:30").assertExists()
        composeRule.onNodeWithText("5:17").assertExists()
    }

    @Test
    fun steppingChangesThePaceAndSavesIt() {
        showScreen()

        stepButton("Add 5 seconds").performClick()
        composeRule.onNodeWithText("8:35").assertExists()

        stepButton("Subtract 1 second").performClick()
        composeRule.onNodeWithText("8:34").assertExists()

        stepButton("Add 1 second").performClick()
        stepButton("Add 1 second").performClick()
        composeRule.onNodeWithText("8:36").assertExists()

        composeRule.runOnIdle {
            assertEquals(516.0 to PaceUnit.MILE, savedPaces.last())
        }
    }

    @Test
    fun largeStepsLandOnMultiplesOfFiveSeconds() {
        showScreen(paceSeconds = 317.0, unit = PaceUnit.KM)

        stepButton("Add 5 seconds").performClick()
        composeRule.onNodeWithText("5:20").assertExists()

        stepButton("Subtract 1 second").performClick()
        stepButton("Subtract 5 seconds").performClick()
        composeRule.onNodeWithText("5:15").assertExists()

        stepButton("Subtract 5 seconds").performClick()
        composeRule.onNodeWithText("5:10").assertExists()
    }

    @Test
    fun holdingAStepButtonRepeatsTheStep() {
        showScreen()

        composeRule.mainClock.autoAdvance = false
        stepButton("Add 5 seconds").performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(1_000)
        stepButton("Add 5 seconds").performTouchInput { up() }
        composeRule.mainClock.autoAdvance = true

        composeRule.runOnIdle {
            val (pace, _) = savedPaces.last()
            // Several steps while held, and releasing adds no extra one
            assert(pace >= 530.0) { "Expected repeated steps, pace was $pace" }
            assertEquals(0.0, pace % 5, 0.0)
        }
        val heldPace = savedPaces.last().first

        stepButton("Add 5 seconds").performClick()
        composeRule.runOnIdle {
            assertEquals(heldPace + 5, savedPaces.last().first, 0.0)
        }
    }

    @Test
    fun paceStaysWithinLimits() {
        showScreen(paceSeconds = 61.0)

        stepButton("Subtract 5 seconds").performClick()
        stepButton("Subtract 5 seconds").performClick()
        composeRule.onNodeWithText("1:00").assertExists()

        composeRule.runOnIdle {
            assertEquals(60.0, savedPaces.last().first, 0.0)
        }
    }

    @Test
    fun paceDoesNotStepPastTheMaximum() {
        showScreen(paceSeconds = 3598.0)

        stepButton("Add 5 seconds").performClick()
        stepButton("Add 5 seconds").performClick()
        composeRule.onNodeWithText("59:59").assertExists()
    }

    @Test
    fun togglingUnitsSavesTheNewUnit() {
        showScreen()

        unitOption("min/km").performClick()

        composeRule.runOnIdle {
            assertEquals(PaceUnit.KM, savedPaces.last().second)
            assertEquals(510.0 / 1.609344, savedPaces.last().first, 1e-9)
        }
    }

    @Test
    fun controlsAreLargeTapTargets() {
        showScreen()

        unitOption("min/mile").assertHasClickAction().assertHeightIsAtLeast(64.dp)
        unitOption("min/km").assertHasClickAction().assertHeightIsAtLeast(64.dp)
        stepButton("Subtract 5 seconds").assertHasClickAction().assertHeightIsAtLeast(88.dp)
        stepButton("Add 5 seconds").assertHasClickAction().assertHeightIsAtLeast(88.dp)
        stepButton("Subtract 1 second").assertHasClickAction().assertHeightIsAtLeast(56.dp)
        stepButton("Add 1 second").assertHasClickAction().assertHeightIsAtLeast(56.dp)
    }
}
