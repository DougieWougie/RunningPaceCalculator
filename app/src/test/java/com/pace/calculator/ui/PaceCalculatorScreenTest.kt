package com.pace.calculator.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
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

    // The unit names also appear as plain labels on the result cards
    private fun unitOption(text: String) = composeRule.onNode(hasText(text) and isSelectable())

    @Test
    fun showsTheSavedPaceAndUnit() {
        showScreen(paceSeconds = 510.0 / 1.609344, unit = PaceUnit.KM)

        composeRule.onNodeWithText("5").assertExists()
        composeRule.onNodeWithText("17").assertExists()
        unitOption("min/km").assertIsSelected()
        unitOption("min/mile").assertIsNotSelected()
        composeRule.onNodeWithText("8:30").assertExists()
    }

    @Test
    fun togglingUnitsConvertsTheInputAndKeepsTheResults() {
        showScreen()

        unitOption("min/km").performClick()

        unitOption("min/km").assertIsSelected()
        composeRule.onNodeWithText("5").assertExists()
        composeRule.onNodeWithText("17").assertExists()
        composeRule.onNodeWithText("8:30").assertExists()
        composeRule.onNodeWithText("5:17").assertExists()

        unitOption("min/mile").performClick()

        unitOption("min/mile").assertIsSelected()
        composeRule.onNodeWithText("8").assertExists()
        composeRule.onNodeWithText("30").assertExists()
        composeRule.onNodeWithText("8:30").assertExists()
        composeRule.onNodeWithText("5:17").assertExists()
    }

    @Test
    fun typingAPaceUpdatesTheResultsAndSavesIt() {
        showScreen()

        composeRule.onNodeWithText("30").performTextReplacement("05")

        composeRule.onNodeWithText("8:05").assertExists()
        composeRule.runOnIdle {
            assertEquals(485.0 to PaceUnit.MILE, savedPaces.last())
        }
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
    fun unitOptionsAreLargeTapTargets() {
        showScreen()

        unitOption("min/mile").assertHasClickAction().assertHeightIsAtLeast(64.dp)
        unitOption("min/km").assertHasClickAction().assertHeightIsAtLeast(64.dp)
    }

    @Test
    fun inputUnitBadgeIsNotShown() {
        showScreen()

        composeRule.onNodeWithText("/mile").assertDoesNotExist()
    }
}
