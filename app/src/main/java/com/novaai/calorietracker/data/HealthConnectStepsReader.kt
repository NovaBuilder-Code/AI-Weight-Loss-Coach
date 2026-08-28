package com.novaai.calorietracker.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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

    private const val TAG = "NovaHealthConnect"
    private const val PROVIDER_PACKAGE = "com.google.android.apps.healthdata"

    val PERMISSIONS: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    fun permissionContract() = PermissionController.createRequestPermissionResultContract()

    fun availability(context: Context): HealthConnectAvailability {
        val raw = HealthConnectClient.getSdkStatus(context)
        val mapped = when (raw) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.UNAVAILABLE
        }
        val providerInstalled = try {
            context.packageManager.getPackageInfo(PROVIDER_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
        Log.i(
            TAG,
            "sdk_status raw=$raw mapped=$mapped " +
                "sdkInt=${Build.VERSION.SDK_INT} release=${Build.VERSION.RELEASE} " +
                "model=${Build.MODEL} providerPackage=$PROVIDER_PACKAGE " +
                "providerInstalled=$providerInstalled"
        )
        Log.i(TAG, "permissions_requested=$PERMISSIONS")
        return mapped
    }

    suspend fun readToday(context: Context): TodayStepsRead {
        val avail = availability(context)
        if (avail != HealthConnectAvailability.AVAILABLE) {
            val result = HealthConnectSteps.result(avail, permissionGranted = false)
            Log.i(TAG, "read_skip availability=$avail status=${result.status} steps=${result.steps}")
            return result
        }
        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            Log.e(TAG, "getOrCreate_error ${e.javaClass.simpleName}: ${e.message}", e)
            return HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = true,
                readError = true
            )
        }
        val grantedSet = try {
            client.permissionController.getGrantedPermissions()
        } catch (e: Exception) {
            Log.e(TAG, "getGrantedPermissions_error ${e.javaClass.simpleName}: ${e.message}", e)
            return HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = true,
                readError = true
            )
        }
        Log.i(TAG, "permissions_granted=$grantedSet")
        val granted = grantedSet.containsAll(PERMISSIONS)
        if (!granted) {
            val result = HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = false
            )
            Log.i(TAG, "read_skip permissionGranted=false status=${result.status} steps=${result.steps}")
            return result
        }
        return try {
            val (start, end) = HealthConnectSteps.todayRange(ZonedDateTime.now())
            Log.i(TAG, "read_request start=$start end=$end")
            if (!start.isBefore(end)) {
                Log.i(TAG, "read_skip empty_window start=$start end=$end")
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
            val total = response[StepsRecord.COUNT_TOTAL]
            val result = HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = true,
                aggregateCount = total
            )
            Log.i(
                TAG,
                "read_result COUNT_TOTAL=$total steps_returned=${result.steps} status=${result.status}"
            )
            result
        } catch (e: SecurityException) {
            Log.e(TAG, "read_security ${e.javaClass.simpleName}: ${e.message}", e)
            HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "read_error ${e.javaClass.simpleName}: ${e.message}", e)
            HealthConnectSteps.result(
                HealthConnectAvailability.AVAILABLE,
                permissionGranted = true,
                readError = true
            )
        }
    }
}
