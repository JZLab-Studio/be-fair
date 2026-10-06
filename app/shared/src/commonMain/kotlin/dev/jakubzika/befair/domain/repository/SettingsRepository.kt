package dev.jakubzika.befair.domain.repository

import dev.jakubzika.befair.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    /** The current preferences; changes apply and persist immediately. */
    val settings: StateFlow<AppSettings>

    /** Applies [transform] to the current settings and saves the result. */
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
