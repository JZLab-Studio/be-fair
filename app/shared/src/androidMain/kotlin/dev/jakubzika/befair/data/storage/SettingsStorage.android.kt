package dev.jakubzika.befair.data.storage

import android.content.Context
import android.content.SharedPreferences

/** Android [SettingsStorage] backed by plain SharedPreferences (preferences, not secrets). */
actual class SettingsStorage {

    private val prefs: SharedPreferences by lazy {
        BeFairAndroidContext.application.getSharedPreferences("befair_settings_prefs", Context.MODE_PRIVATE)
    }

    actual fun getString(key: String): String? = prefs.getString(key, null)

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
