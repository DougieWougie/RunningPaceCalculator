package com.pace.calculator

import kotlin.math.roundToLong

object PaceCalculator {
    private const val KM_PER_MILE = 1.609344

    enum class RaceDistance(val km: Double, val label: String) {
        FIVE_K(5.0, "5K"),
        TEN_K(10.0, "10K"),
        HALF_MARATHON(21.0975, "Half"),
        MARATHON(42.195, "Marathon")
    }

    data class PaceResult(
        val mph: Double,
        val kph: Double,
        val minPerMile: String,
        val minPerKm: String,
        val raceTimes: Map<RaceDistance, String>
    )

    // Pace is carried as seconds per unit and only rounded when formatted
    fun calculate(paceSeconds: Double, inputUnit: PaceUnit): PaceResult? {
        if (paceSeconds <= 0.0) return null

        val secPerMile = convert(paceSeconds, inputUnit, PaceUnit.MILE)
        val secPerKm = convert(paceSeconds, inputUnit, PaceUnit.KM)

        val raceTimes = RaceDistance.entries.associateWith { race ->
            formatDuration(secPerKm * race.km)
        }

        return PaceResult(
            mph = 3600.0 / secPerMile,
            kph = 3600.0 / secPerKm,
            minPerMile = formatDuration(secPerMile),
            minPerKm = formatDuration(secPerKm),
            raceTimes = raceTimes
        )
    }

    fun convert(paceSeconds: Double, from: PaceUnit, to: PaceUnit): Double = when {
        from == to -> paceSeconds
        to == PaceUnit.KM -> paceSeconds / KM_PER_MILE
        else -> paceSeconds * KM_PER_MILE
    }

    private fun formatDuration(totalSeconds: Double): String {
        val rounded = totalSeconds.roundToLong()
        val hours = rounded / 3600
        val mins = rounded % 3600 / 60
        val secs = (rounded % 60).toString().padStart(2, '0')

        return if (hours > 0) {
            "$hours:${mins.toString().padStart(2, '0')}:$secs"
        } else {
            "$mins:$secs"
        }
    }
}

enum class PaceUnit { MILE, KM }
