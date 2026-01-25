package com.pace.calculator

object PaceCalculator {
    private const val MILES_TO_KM = 1.60934

    data class PaceResult(
        val mph: Double,
        val kph: Double,
        val minPerMile: String,
        val minPerKm: String
    )

    fun calculate(minutes: Int, seconds: Int, inputUnit: PaceUnit): PaceResult? {
        val totalMinutes = minutes + seconds / 60.0
        if (totalMinutes == 0.0) return null

        val (minPerMile, minPerKm) = when (inputUnit) {
            PaceUnit.MILE -> totalMinutes to totalMinutes / MILES_TO_KM
            PaceUnit.KM -> totalMinutes * MILES_TO_KM to totalMinutes
        }

        return PaceResult(
            mph = 60.0 / minPerMile,
            kph = 60.0 / minPerKm,
            minPerMile = formatTime(minPerMile),
            minPerKm = formatTime(minPerKm)
        )
    }

    private fun formatTime(totalMinutes: Double): String {
        val mins = totalMinutes.toInt()
        val secs = ((totalMinutes - mins) * 60).toInt()
        return "$mins:${secs.toString().padStart(2, '0')}"
    }
}

enum class PaceUnit { MILE, KM }
