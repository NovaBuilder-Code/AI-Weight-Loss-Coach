package com.novaai.calorietracker.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.ZonedDateTime

/**
 * Reads today's total steps from Health Connect (READ_STEPS only).
 * Fail closed: any missing permission, unavailable SDK, or API error is 0.
 */
object HealthConnectStepsReader {

    val PERMISSIONS: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    fun permissionContract() = PermissionController.createRequestPermissionResultContract()

    fun availability(context: Context): HealthConnectAvailability {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.UNAVAILABLE
        }
    }

    suspend fun readToday(context: Context): TodayStepsRead {
        val avail = availability(context)
        if (avail != HealthConnectAvailability.AVAILABLE) {
            return HealthConnectSteps.result(avail, permissionGranted = false)
        }
        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (_: Exception) {
            return HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = true,
                readError = true
            )
        }
        val granted = try {
            client.permissionController.getGrantedPermissions().containsAll(PERMISSIONS)
        } catch (_: Exception) {
            return HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = true,
                readError = true
            )
        }
        if (!granted) {
            return HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = false
            )
        }
        return try {
            val (start, end) = HealthConnectSteps.todayRange(ZonedDateTime.now())
            if (!start.isBefore(end)) {
                return HealthConnectSteps.result(
                    HealthConnectAvailability.AVAILABLE,
                    permissionGranted = true,
                    aggregateCount = 0L
                )
            }
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = true,
                aggregateCount = response[StepsRecord.COUNT_TOTAL]
            )
        } catch (_: SecurityException) {
            HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = false
            )
        } catch (_: Exception) {
            HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = true,
                readError = true
            )
        }
    }
}