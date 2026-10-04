package com.pace.calculator

import com.pace.calculator.PaceCalculator.RaceDistance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaceCalculatorTest {

    private fun calculate(minutes: Int, seconds: Int, unit: PaceUnit) =
        PaceCalculator.calculate((minutes * 60 + seconds).toDouble(), unit)!!

    private fun format(totalSeconds: Int) =
        "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"

    @Test
    fun zeroPaceHasNoResult() {
        assertNull(PaceCalculator.calculate(0.0, PaceUnit.MILE))
        assertNull(PaceCalculator.calculate(0.0, PaceUnit.KM))
    }

    @Test
    fun everyPaceEchoesUnchangedInItsOwnUnit() {
        for (totalSeconds in 1..59 * 60 + 59) {
            val pace = totalSeconds.toDouble()
            assertEquals(format(totalSeconds), PaceCalculator.calculate(pace, PaceUnit.MILE)!!.minPerMile)
            assertEquals(format(totalSeconds), PaceCalculator.calculate(pace, PaceUnit.KM)!!.minPerKm)
        }
    }

    @Test
    fun convertsBetweenUnitsRoundingToNearestSecond() {
        // 510 s/mile is 316.9 s/km
        assertEquals("5:17", calculate(8, 30, PaceUnit.MILE).minPerKm)
        // 300 s/km is 482.8 s/mile
        assertEquals("8:03", calculate(5, 0, PaceUnit.KM).minPerMile)
    }

    @Test
    fun calculatesSpeed() {
        val result = calculate(5, 0, PaceUnit.KM)
        assertEquals(12.0, result.kph, 1e-9)
        assertEquals(7.456454, result.mph, 1e-6)

        assertEquals(10.0, calculate(6, 0, PaceUnit.MILE).mph, 1e-9)
    }

    @Test
    fun calculatesRaceTimes() {
        val raceTimes = calculate(5, 0, PaceUnit.KM).raceTimes
        assertEquals("25:00", raceTimes[RaceDistance.FIVE_K])
        assertEquals("50:00", raceTimes[RaceDistance.TEN_K])
        assertEquals("1:45:29", raceTimes[RaceDistance.HALF_MARATHON])
    }

    @Test
    fun raceTimesRoundToNearestSecond() {
        // 2:01/km over 10 km is exactly 20:10
        assertEquals("20:10", calculate(2, 1, PaceUnit.KM).raceTimes[RaceDistance.TEN_K])
        // 4:16/km over a marathon is 10801.9 s
        assertEquals("3:00:02", calculate(4, 16, PaceUnit.KM).raceTimes[RaceDistance.MARATHON])
    }

    @Test
    fun secondsRoundingUpCarryIntoMinutes() {
        // 299.7 s must show as 5:00, not 4:60
        assertEquals("5:00", PaceCalculator.calculate(299.7, PaceUnit.KM)!!.minPerKm)
    }

    @Test
    fun togglingUnitsBackAndForthNeverChangesThePace() {
        for (totalSeconds in 3 * 60..15 * 60) {
            var pace = totalSeconds.toDouble()
            var unit = PaceUnit.MILE
            val original = PaceCalculator.calculate(pace, unit)

            repeat(20) {
                val newUnit = if (unit == PaceUnit.MILE) PaceUnit.KM else PaceUnit.MILE
                pace = PaceCalculator.convert(pace, unit, newUnit)
                unit = newUnit

                val result = PaceCalculator.calculate(pace, unit)!!
                assertEquals(original!!.minPerMile, result.minPerMile)
                assertEquals(original.minPerKm, result.minPerKm)
                assertEquals(original.raceTimes, result.raceTimes)
            }
        }
    }
}
