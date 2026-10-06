package dev.jakubzika.befair.data.storage

/**
 * Persistent key-value store for the app-wide display preferences. Nothing here is sensitive,
 * so the OS' plain preference store is used:
 *  - Android: SharedPreferences
 *  - iOS: UserDefaults
 */
expect class SettingsStorage() {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

internal const val SETTINGS_KEY_CURRENCY = "befair_settings_currency"
internal const val SETTINGS_KEY_CUSTOM_CURRENCY = "befair_settings_custom_currency"
internal const val SETTINGS_KEY_TOOL_BASIS = "befair_settings_tool_basis"
