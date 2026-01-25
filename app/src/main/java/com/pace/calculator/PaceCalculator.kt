package com.pace.calculator

object PaceCalculator {
    private const val MILES_TO_KM = 1.60934

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

    fun calculate(minutes: Int, seconds: Int, inputUnit: PaceUnit): PaceResult? {
        val totalMinutes = minutes + seconds / 60.0
        if (totalMinutes == 0.0) return null

        val (minPerMile, minPerKm) = when (inputUnit) {
            PaceUnit.MILE -> totalMinutes to totalMinutes / MILES_TO_KM
            PaceUnit.KM -> totalMinutes * MILES_TO_KM to totalMinutes
        }

        val raceTimes = RaceDistance.entries.associateWith { race ->
            formatRaceTime(minPerKm * race.km)
        }

        return PaceResult(
            mph = 60.0 / minPerMile,
            kph = 60.0 / minPerKm,
            minPerMile = formatTime(minPerMile),
            minPerKm = formatTime(minPerKm),
            raceTimes = raceTimes
        )
    }

    private fun formatTime(totalMinutes: Double): String {
        val mins = totalMinutes.toInt()
        val secs = ((totalMinutes - mins) * 60).toInt()
        return "$mins:${secs.toString().padStart(2, '0')}"
    }

    private fun formatRaceTime(totalMinutes: Double): String {
        val hours = (totalMinutes / 60).toInt()
        val mins = (totalMinutes % 60).toInt()
        val secs = ((totalMinutes - totalMinutes.toInt()) * 60).toInt()

        return if (hours > 0) {
            "$hours:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
        } else {
            "$mins:${secs.toString().padStart(2, '0')}"
        }
    }
}

enum class PaceUnit { MILE, KM }
