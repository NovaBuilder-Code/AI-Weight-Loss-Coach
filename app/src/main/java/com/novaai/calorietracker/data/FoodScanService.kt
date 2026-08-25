package com.novaai.calorietracker.data

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

                val payload = JSONObject()
                    .put("image", Base64.getEncoder().encodeToString(jpeg))
                    .put("mime", mime)
                    .toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

                val status = connection.responseCode
                if (status !in 200..299) return@withContext FoodScanOutcome.ServerError

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val result = FoodScanJson.parseScanResult(body)
                if (result == null) FoodScanOutcome.ServerError else FoodScanOutcome.Success(result)
            } catch (e: SocketTimeoutException) {
                FoodScanOutcome.Timeout
            } catch (e: IOException) {
                FoodScanOutcome.NetworkError
            } catch (e: Exception) {
                // Malformed JSON or anything unexpected from the server side.
                FoodScanOutcome.ServerError
            } finally {
                connection?.disconnect()
            }
        }
}