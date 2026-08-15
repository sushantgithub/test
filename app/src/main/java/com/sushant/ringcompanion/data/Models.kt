package com.sushant.ringcompanion.data

data class RingSnapshot(
    val sourceLabel: String,
    val usingSampleData: Boolean,
    val healthConnectStatus: String,
    val energyScore: Int,
    val energyLabel: String,
    val sleepHours: Double,
    val sleepQuality: String,
    val restingHr: Int?,
    val latestHr: Int?,
    val steps: Long,
    val calories: Int,
    val spo2: Int?,
    val skinTempDeltaC: Double?,
    val notes: List<String>
)

object SampleRingData {
    val snapshot = RingSnapshot(
        sourceLabel = "Sample data",
        usingSampleData = true,
        healthConnectStatus = "Not connected",
        energyScore = 78,
        energyLabel = "Ready",
        sleepHours = 7.4,
        sleepQuality = "Solid night",
        restingHr = 56,
        latestHr = 62,
        steps = 6_420,
        calories = 1_860,
        spo2 = 97,
        skinTempDeltaC = 0.2,
        notes = listOf(
            "Pair Galaxy Ring in Galaxy Wearable first.",
            "Samsung Health stores ring data.",
            "Turn on Health Connect sharing to replace this sample."
        )
    )
}
