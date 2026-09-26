package dev.jakubzika.befair.data.network

import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.GenericResponse
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Wraps a network call in [AppResult]. The shared client sets `expectSuccess`, so any non-2xx
 * arrives here as a [ResponseException] carrying the server's [GenericResponse] body.
 */
suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: ResponseException) {
    parseError(e)
} catch (e: Exception) {
    AppResult.Error(e.message ?: "Network error. Please try again.")
}

/** Best-effort extraction of the server's GenericResponse from an error body. */
private suspend fun parseError(e: ResponseException): AppResult.Error = try {
    val body = errorJson.decodeFromString<GenericResponse>(e.response.bodyAsText())
    AppResult.Error(body.message, body.fields)
} catch (_: Exception) {
    AppResult.Error("Request failed (${e.response.status.value}).")
}
