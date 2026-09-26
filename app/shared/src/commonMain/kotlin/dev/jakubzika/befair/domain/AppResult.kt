package dev.jakubzika.befair.domain

/**
 * Minimal result wrapper for network calls so the UI can branch on success/failure
 * without dealing with exceptions directly.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>

    /** [fields] carries the server's per-field validation map when it sent one. */
    data class Error(
        val message: String,
        val fields: Map<String, String>? = null,
    ) : AppResult<Nothing>
}
