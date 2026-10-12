package dev.jakubzika.befair.domain.repository

import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    /** The current preferences; changes apply and persist locally immediately. */
    val settings: StateFlow<AppSettings>

    /**
     * Queues [transform] to be applied to the current settings and saved locally; no network call.
     * Edits apply in call order and keep running after the caller is gone.
     */
    fun update(transform: (AppSettings) -> AppSettings)

    /**
     * Synchronizes with the server: uploads local edits the server has not accepted yet, otherwise
     * replaces the local values with the server's. On failure the local values are kept.
     */
    suspend fun sync(): AppResult<Unit>

    /**
     * Uploads pending local edits in the background, if any. Meant for leaving the settings
     * screen; it keeps running after the caller is gone.
     */
    fun pushPending()
}
