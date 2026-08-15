package com.sushant.ringcompanion.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class HealthRepository(private val context: Context) {

    val client: HealthConnectClient?
        get() = if (sdkStatus() == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context)

    fun statusLabel(): String = when (sdkStatus()) {
        HealthConnectClient.SDK_AVAILABLE -> "Health Connect ready"
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "Update Health Connect"
        else -> "Install Health Connect"
    }

    suspend fun hasAllPermissions(): Boolean {
        val hc = client ?: return false
        return hc.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    suspend fun loadSnapshot(): RingSnapshot {
        val hc = client ?: return SampleRingData.snapshot.copy(
            healthConnectStatus = statusLabel(),
            notes = SampleRingData.snapshot.notes + statusLabel()
        )
        if (!hasAllPermissions()) {
            return SampleRingData.snapshot.copy(
                healthConnectStatus = "Permissions needed",
                notes = listOf("Tap Connect Health Connect and allow sleep, heart, and activity.")
            )
        }

        val end = Instant.now()
        val start = end.minus(Duration.ofHours(36))
        val filter = TimeRangeFilter.between(start, end)

        val sleep: List<SleepSessionRecord> = runCatching {
            hc.readRecords(ReadRecordsRequest(SleepSessionRecord::class, timeRangeFilter = filter)).records
        }.getOrDefault(emptyList())
        val hr: List<HeartRateRecord> = runCatching {
            hc.readRecords(ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter = filter)).records
        }.getOrDefault(emptyList())
        val rhr: List<RestingHeartRateRecord> = runCatching {
            hc.readRecords(ReadRecordsRequest(RestingHeartRateRecord::class, timeRangeFilter = filter)).records
        }.getOrDefault(emptyList())
        val steps: List<StepsRecord> = runCatching {
            hc.readRecords(ReadRecordsRequest(StepsRecord::class, timeRangeFilter = todayFilter())).records
        }.getOrDefault(emptyList())
        val spo2: List<OxygenSaturationRecord> = runCatching {
            hc.readRecords(ReadRecordsRequest(OxygenSaturationRecord::class, timeRangeFilter = filter)).records
        }.getOrDefault(emptyList())
        val activeCal: List<ActiveCaloriesBurnedRecord> = runCatching {
            hc.readRecords(ReadRecordsRequest(ActiveCaloriesBurnedRecord::class, timeRangeFilter = todayFilter())).records
        }.getOrDefault(emptyList())
        val totalCal: List<TotalCaloriesBurnedRecord> = runCatching {
            hc.readRecords(ReadRecordsRequest(TotalCaloriesBurnedRecord::class, timeRangeFilter = todayFilter())).records
        }.getOrDefault(emptyList())

        val sleepSeconds = sleep.sumOf { Duration.between(it.startTime, it.endTime).seconds }
        val sleepHours = sleepSeconds / 3600.0
        val latestHr = hr.flatMap { it.samples }.maxByOrNull { it.time }?.beatsPerMinute?.toInt()
        val resting = rhr.maxByOrNull { it.time }?.beatsPerMinute?.toInt()
        val stepCount = steps.sumOf { it.count }
        val latestSpo2 = spo2.maxByOrNull { it.time }?.percentage?.value?.roundToInt()
        var kcal = 0.0
        if (totalCal.isNotEmpty()) {
            for (rec in totalCal) {
                kcal += rec.energy.inKilocalories
            }
        } else {
            for (rec in activeCal) {
                kcal += rec.energy.inKilocalories
            }
        }
        val calories = kcal.roundToInt()

        val energy = computeEnergy(sleepHours, resting)
        return RingSnapshot(
            sourceLabel = "Health Connect · Galaxy Ring / Samsung Health",
            usingSampleData = false,
            healthConnectStatus = "Connected",
            energyScore = energy.first,
            energyLabel = energy.second,
            sleepHours = sleepHours,
            sleepQuality = if (sleepHours >= 7) "Restored" else if (sleepHours >= 5.5) "Okay" else "Short night",
            restingHr = resting,
            latestHr = latestHr,
            steps = stepCount,
            calories = calories,
            spo2 = latestSpo2,
            skinTempDeltaC = null,
            notes = listOf(
                "Data comes from Samsung Health via Health Connect.",
                "Galaxy Ring pairing stays in Galaxy Wearable."
            )
        )
    }

    private fun todayFilter(): TimeRangeFilter {
        val start = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        return TimeRangeFilter.between(start, Instant.now())
    }

    private fun computeEnergy(sleepHours: Double, restingHr: Int?): Pair<Int, String> {
        var score = 55
        score += when {
            sleepHours >= 8 -> 25
            sleepHours >= 7 -> 20
            sleepHours >= 6 -> 10
            sleepHours > 0 -> 0
            else -> -5
        }
        if (restingHr != null) {
            score += when {
                restingHr <= 55 -> 15
                restingHr <= 65 -> 10
                restingHr <= 75 -> 4
                else -> -4
            }
        }
        score = score.coerceIn(1, 99)
        val label = when {
            score >= 80 -> "Peak"
            score >= 65 -> "Ready"
            score >= 50 -> "Pay down sleep"
            else -> "Recover"
        }
        return score to label
    }

    fun openOrInstallHealthConnect() {
        val status = sdkStatus()
        val intent = when (status) {
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.google.android.apps.healthdata")
            )
            HealthConnectClient.SDK_UNAVAILABLE -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.google.android.apps.healthdata")
            )
            else -> Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    fun openSamsungHealth() = openPackage(
        "com.sec.android.app.shealth",
        "market://details?id=com.sec.android.app.shealth"
    )

    fun openGalaxyWearable() = openPackage(
        "com.samsung.android.app.watchmanager",
        "market://details?id=com.samsung.android.app.watchmanager"
    )

    private fun openPackage(pkg: String, store: String) {
        val launch = context.packageManager.getLaunchIntentForPackage(pkg)
        val intent = launch ?: Intent(Intent.ACTION_VIEW, Uri.parse(store))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
