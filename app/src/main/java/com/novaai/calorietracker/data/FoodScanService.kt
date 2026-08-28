package com.novaai.calorietracker.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.Base64

/** Outcome of a food-photo scan request to the Nova AI backend. */
sealed class FoodScanOutcome {
    data class Success(val result: FoodScanResult) : FoodScanOutcome()
    data object Timeout : FoodScanOutcome()
    data object NetworkError : FoodScanOutcome()
    data object ServerError : FoodScanOutcome()
}

/**
 * Sends a compressed food photo (JPEG bytes) to the Cloudflare Worker
 * backend, which holds the OpenAI key server-side — the app never sees or
 * stores it. The Worker returns the strict scan schema; malformed responses
 * surface as [FoodScanOutcome.ServerError].
 */
object FoodScanService {

    private const val SCAN_URL = "https://nova-ai-backend.novaaicoach-4d1.workers.dev/scan-food"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 60_000

    suspend fun analyze(jpeg: ByteArray, mime: String = "image/jpeg"): FoodScanOutcome =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(SCAN_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                }

                Log.d(FoodScanCameraHandoff.LOG_TAG, "request start jpegBytes=${jpeg.size} mime=$mime")
                Log.d(
                    FoodScanCameraHandoff.LOG_TAG,
                    "request_start url=/scan-food jpegBytes=${jpeg.size}"
                )
                val payload = JSONObject()
                    .put("image", Base64.getEncoder().encodeToString(jpeg))
                    .put("mime", mime)
                    .toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

                val status = connection.responseCode
                Log.d(FoodScanCameraHandoff.LOG_TAG, "response status=$status")
                Log.d(FoodScanCameraHandoff.LOG_TAG, "http_status=$status")
                if (status !in 200..299) {
                    Log.d(FoodScanCameraHandoff.LOG_TAG, "response error status=$status")
                    val errorBody = try {
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    } catch (_: Exception) {
                        ""
                    }
                    Log.d(
                        FoodScanCameraHandoff.LOG_TAG,
                        "http_error_body=${errorBody.take(400)}"
                    )
                    return@withContext FoodScanOutcome.ServerError
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val result = FoodScanJson.parseScanResult(body)
                if (result == null) {
                    Log.d(FoodScanCameraHandoff.LOG_TAG, "response parse fail")
                    Log.d(
                        FoodScanCameraHandoff.LOG_TAG,
                        "parse_fail bodyPrefix=${body.take(400)}"
                    )
                    FoodScanOutcome.ServerError
                } else {
                    Log.d(FoodScanCameraHandoff.LOG_TAG, "response parse ok foods=${result.foods.size}")
                    Log.d(
                        FoodScanCameraHandoff.LOG_TAG,
                        "parse_ok foods=${result.foods.size} totalCalories=${result.totalCalories} confidence=${result.confidence}"
                    )
                    FoodScanOutcome.Success(result)
                }
            } catch (e: SocketTimeoutException) {
                Log.d(FoodScanCameraHandoff.LOG_TAG, "error timeout")
                FoodScanOutcome.Timeout
            } catch (e: IOException) {
                Log.d(FoodScanCameraHandoff.LOG_TAG, "error network ${e.javaClass.simpleName}")
                FoodScanOutcome.NetworkError
            } catch (e: Exception) {
                Log.d(FoodScanCameraHandoff.LOG_TAG, "error server ${e.javaClass.simpleName}")
                FoodScanOutcome.ServerError
            } finally {
                connection?.disconnect()
            }
        }
}