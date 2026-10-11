package dev.jakubzika.befair.domain.repository

import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.SettingsResponse
import kotlinx.coroutines.flow.StateFlow

/**
 * The signed-in user's settings: stored on-device for offline use and mirrored to the server so
 * they survive a reinstall. Held in the core wire DTO [SettingsResponse].
 */
interface SettingsRepository {
    /** Current settings; defaults until the first successful save or download. */
    val settings: StateFlow<SettingsResponse>

    /**
     * Brings the device and server in line: uploads local edits the server has not seen yet,
     * otherwise replaces the local values with the server's. On failure the local values are kept.
     * Run after sign-in and on every app start.
     */
    suspend fun sync(): AppResult<Unit>

    /** Stores the edit locally (marked pending) without touching the network. */
    suspend fun update(settings: SettingsResponse)

    /** Pushes pending edits to the server. Call when the settings screen is left, not on every tap. */
    suspend fun pushPending(): AppResult<Unit>

    /** Drops the cached settings, e.g. on sign-out. */
    suspend fun clear()
}
