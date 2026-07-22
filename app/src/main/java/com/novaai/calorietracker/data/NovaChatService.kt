package com.novaai.calorietracker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/** Outcome of a chat request to the Nova AI backend. */
sealed class ChatResult {
    data class Success(val reply: String) : ChatResult()
    data object Timeout : ChatResult()
    data object NetworkError : ChatResult()
    data object ServerError : ChatResult()
}

/**
 * Talks to the deployed Cloudflare Worker backend. The Worker holds the
 * OpenAI key as a server-side secret; the app never sees or stores it.
 */
object NovaChatService {

    private const val CHAT_URL = "https://nova-ai-backend.novaaicoach-4d1.workers.dev/chat"
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 30_000

    suspend fun sendMessage(message: String): ChatResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(CHAT_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject().put("message", message).toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

            val status = connection.responseCode
            if (status !in 200..299) return@withContext ChatResult.ServerError

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val reply = JSONObject(body).optString("reply")
            if (reply.isBlank()) ChatResult.ServerError else ChatResult.Success(reply)
        } catch (e: SocketTimeoutException) {
            ChatResult.Timeout
        } catch (e: IOException) {
            ChatResult.NetworkError
        } catch (e: Exception) {
            // Malformed JSON or anything unexpected from the server side.
            ChatResult.ServerError
        } finally {
            connection?.disconnect()
        }
    }
}
